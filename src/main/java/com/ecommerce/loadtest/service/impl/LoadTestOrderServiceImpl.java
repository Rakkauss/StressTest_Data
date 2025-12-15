package com.ecommerce.loadtest.service.impl;

import com.ecommerce.loadtest.common.Result;
import com.ecommerce.loadtest.dao.LoadTestOrderMapper;
import com.ecommerce.loadtest.entity.LoadTestOrder;
import com.ecommerce.loadtest.entity.TestUser;
import com.ecommerce.loadtest.service.LoadTestOrderService;
import com.ecommerce.loadtest.service.TestUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 压测订单服务实现类
 * 
 * @author rakkaus
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class LoadTestOrderServiceImpl implements LoadTestOrderService {
    
    private static final Logger logger = LoggerFactory.getLogger(LoadTestOrderServiceImpl.class);
    
    @Autowired
    private LoadTestOrderMapper loadTestOrderMapper;
    
    @Autowired
    private TestUserService testUserService;
    
    private static final Map<Long, Map<String, Object>> exportTaskStorage = new HashMap<>();
    private static final AtomicLong taskIdGenerator = new AtomicLong(1000);
    
    @Override
    public int add(LoadTestOrder order) {
        logger.info("添加压测订单 - orderId: {}, buyerId: {}", order.getOrderId(), order.getBuyerId());
        try {
            if (order.getCreateTime() == null) {
                order.setCreateTime(new Date());
            }
            return loadTestOrderMapper.insertSelective(order);
        } catch (Exception e) {
            logger.error("添加订单失败 - orderId: {}", order.getOrderId(), e);
            throw new RuntimeException("添加订单失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public int updatePressureOrder(LoadTestOrder order) {
        logger.info("更新压测订单 - id: {}", order.getId());
        try {
            order.setUpdateTime(new Date());
            return loadTestOrderMapper.updateByPrimaryKeySelective(order);
        } catch (Exception e) {
            logger.error("更新订单失败 - id: {}", order.getId(), e);
            throw new RuntimeException("更新订单失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public int updateOrderStatus(Long orderId, Integer currentStatus) {
        logger.info("更新订单状态 - orderId: {}, status: {}", orderId, currentStatus);
        try {
            return loadTestOrderMapper.updateOrderStatus(orderId, currentStatus);
        } catch (Exception e) {
            logger.error("更新订单状态失败 - orderId: {}", orderId, e);
            throw new RuntimeException("更新订单状态失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Long> selectOrderIdsByBuyerId(Long buyerId) {
        logger.debug("查询买家订单ID列表 - buyerId: {}", buyerId);
        try {
            return loadTestOrderMapper.selectOrderIdsByBuyerId(buyerId);
        } catch (Exception e) {
            logger.error("查询买家订单失败 - buyerId: {}", buyerId, e);
            throw new RuntimeException("查询买家订单失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<LoadTestOrder> getOrderList(Long uid, List<Integer> orderStatusList) {
        logger.info("获取订单列表 - uid: {}, statusList: {}", uid, orderStatusList);
        try {
            Integer status = null;
            if (orderStatusList != null && !orderStatusList.isEmpty()) {
                status = orderStatusList.get(0);
            }
            return loadTestOrderMapper.selectByConditions(uid, status, null, null, null);
        } catch (Exception e) {
            logger.error("获取订单列表失败 - uid: {}", uid, e);
            throw new RuntimeException("获取订单列表失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public int cancelOrderList(Integer userType, Integer threadNum) {
        logger.info("批量取消订单 - userType: {}, threadNum: {}", userType, threadNum);
        try {
            List<TestUser> userList = testUserService.queryUserList(userType);
            logger.info("获取到{}个测试用户", userList.size());
            
            if (userList.isEmpty()) {
                return 0;
            }
            
            ForkJoinPool pool = new ForkJoinPool(Math.min(threadNum, 10));
            AtomicLong cancelCount = new AtomicLong(0);
            
            try {
                pool.submit(() -> {
                    userList.parallelStream().forEach(user -> {
                        try {
                            List<Long> orderIds = selectOrderIdsByBuyerId(user.getUid());
                            for (Long orderId : orderIds) {
                                boolean success = mockCancelOrder(orderId, user.getUid());
                                if (success) {
                                    updateOrderStatus(orderId, 3);
                                    cancelCount.incrementAndGet();
                                }
                            }
                        } catch (Exception e) {
                            logger.error("取消用户订单失败 - uid: {}", user.getUid(), e);
                        }
                    });
                }).get();
            } finally {
                pool.shutdown();
            }
            
            logger.info("批量取消订单完成 - 共取消{}个订单", cancelCount.get());
            return cancelCount.intValue();
        } catch (Exception e) {
            logger.error("批量取消订单失败", e);
            throw new RuntimeException("批量取消订单失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public Result<String> clearTable() {
        logger.info("清空压测订单表");
        try {
            int result = loadTestOrderMapper.clearTable();
            logger.info("清空订单表完成 - 共清理{}条记录", result);
            return Result.success("清空成功，共清理" + result + "条记录");
        } catch (Exception e) {
            logger.error("清空订单表失败", e);
            return Result.fail("清空失败：" + e.getMessage());
        }
    }
    
    @Override
    public Result<String> deleteByDate(String dateStr) {
        logger.info("按日期删除订单 - dateStr: {}", dateStr);
        try {
            int result = loadTestOrderMapper.deleteByDateString(dateStr);
            logger.info("按日期删除订单完成 - 共删除{}条记录", result);
            return Result.success("删除成功，共删除" + result + "条记录");
        } catch (Exception e) {
            logger.error("按日期删除订单失败", e);
            return Result.fail("删除失败：" + e.getMessage());
        }
    }
    
    @Override
    public Result<Long> exportOrder(Integer orderType, Long size) {
        logger.info("导出订单数据 - orderType: {}, size: {}", orderType, size);
        
        Long taskId = taskIdGenerator.getAndIncrement();
        
        Map<String, Object> taskInfo = new HashMap<>();
        taskInfo.put("taskId", taskId);
        taskInfo.put("orderType", orderType);
        taskInfo.put("size", size);
        taskInfo.put("status", 0);
        taskInfo.put("createTime", new Date());
        taskInfo.put("fileName", String.format("load_test_order_data_%d_%d.xlsx", orderType, taskId));
        
        synchronized (exportTaskStorage) {
            exportTaskStorage.put(taskId, taskInfo);
        }
        
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(1000 + new Random().nextInt(2000));
                
                List<LoadTestOrder> orders = loadTestOrderMapper.selectByConditions(
                    null, orderType, null, null, null);
                
                if (size != null && orders.size() > size) {
                    orders = orders.subList(0, size.intValue());
                }
                
                taskInfo.put("status", 1);
                taskInfo.put("completeTime", new Date());
                taskInfo.put("totalCount", orders.size());
                taskInfo.put("fileUrl", "/mock/download/" + taskInfo.get("fileName"));
                
                logger.info("订单数据导出完成 - taskId: {}, 共{}条记录", taskId, orders.size());
            } catch (InterruptedException e) {
                taskInfo.put("status", 2);
                taskInfo.put("errorMessage", "导出被中断");
                logger.error("订单数据导出被中断 - taskId: {}", taskId);
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                taskInfo.put("status", 2);
                taskInfo.put("errorMessage", e.getMessage());
                logger.error("订单数据导出失败 - taskId: {}", taskId, e);
            }
        });
        
        return Result.success("导出任务创建成功", taskId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Result<Object> getTask(Long downloadId) {
        logger.info("获取导出任务 - taskId: {}", downloadId);
        
        Map<String, Object> taskInfo;
        synchronized (exportTaskStorage) {
            taskInfo = exportTaskStorage.get(downloadId);
        }
        
        if (taskInfo == null) {
            return Result.fail("任务不存在");
        }
        
        Map<String, Object> downloadPO = new HashMap<>();
        downloadPO.put("id", taskInfo.get("taskId"));
        downloadPO.put("fileName", taskInfo.get("fileName"));
        downloadPO.put("fileUrl", taskInfo.get("fileUrl"));
        downloadPO.put("status", taskInfo.get("status"));
        downloadPO.put("totalCount", taskInfo.get("totalCount"));
        downloadPO.put("createTime", taskInfo.get("createTime"));
        downloadPO.put("completeTime", taskInfo.get("completeTime"));
        downloadPO.put("errorMessage", taskInfo.get("errorMessage"));
        
        return Result.success("查询成功", downloadPO);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<LoadTestOrder> queryOrderList(Integer userType, Integer pageNum, Integer pageSize) {
        logger.info("查询订单列表 - userType: {}, pageNum: {}, pageSize: {}", userType, pageNum, pageSize);
        try {
            return loadTestOrderMapper.selectByConditions(null, null, null, null, null);
        } catch (Exception e) {
            logger.error("查询订单列表失败", e);
            throw new RuntimeException("查询订单列表失败: " + e.getMessage(), e);
        }
    }
    
    private boolean mockCancelOrder(Long orderId, Long uid) {
        logger.debug("Mock取消订单 - orderId: {}, uid: {}", orderId, uid);
        try {
            Thread.sleep(20 + new Random().nextInt(50));
            return new Random().nextInt(100) < 95;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
