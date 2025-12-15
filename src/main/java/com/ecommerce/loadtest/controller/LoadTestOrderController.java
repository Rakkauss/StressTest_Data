package com.ecommerce.loadtest.controller;

import com.ecommerce.loadtest.common.Result;
import com.ecommerce.loadtest.entity.LoadTestOrder;
import com.ecommerce.loadtest.entity.TestUser;
import com.ecommerce.loadtest.service.LoadTestOrderService;
import com.ecommerce.loadtest.service.TestUserService;
import com.ecommerce.loadtest.utils.AsyncExecutorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 压测订单控制器
 * 管理压测订单
 * 
 * @author rakkaus
 */
@RestController
@RequestMapping("/order")
public class LoadTestOrderController {
    
    private static final Logger logger = LoggerFactory.getLogger(LoadTestOrderController.class);
    
    @Autowired
    private LoadTestOrderService loadTestOrderService;
    
    @Autowired
    private TestUserService testUserService;
    
    @GetMapping("/cancelOrder")
    public Result<Integer> cancelOrder(@RequestParam(name = "userType") Integer userType,
                                      @RequestParam(name = "threadNum", required = false, defaultValue = "5") Integer threadNum) {
        try {
            logger.info("批量取消订单 - userType: {}, threadNum: {}", userType, threadNum);
            
            if (userType == null || (userType != 1 && userType != 2)) {
                return Result.fail("用户类型参数错误，只支持1(平台A)或2(平台B)");
            }
            
            if (threadNum <= 0 || threadNum > 20) {
                threadNum = 5;
                logger.warn("线程数参数异常，使用默认值: {}", threadNum);
            }
            
            final Integer finalThreadNum = threadNum;
            AsyncExecutorUtil.submit(() -> {
                try {
                    int cancelCount = loadTestOrderService.cancelOrderList(userType, finalThreadNum);
                    logger.info("批量取消订单完成 - 共取消{}个订单", cancelCount);
                } catch (Exception e) {
                    logger.error("批量取消订单异常 - userType: {}", userType, e);
                }
            });
            
            return Result.success("取消订单提交成功，异步执行", 0);
        } catch (Exception e) {
            logger.error("批量取消订单失败", e);
            return Result.fail("取消订单失败");
        }
    }
    
    @GetMapping("/recordOrder")
    public Result<Integer> recordOrder(@RequestParam(name = "userType") Integer userType,
                                      @RequestParam(name = "orderStatus", defaultValue = "1") String orderStatus) {
        try {
            logger.info("记录压测订单 - userType: {}, orderStatus: {}", userType, orderStatus);
            
            if (userType == null || (userType != 1 && userType != 2)) {
                return Result.fail("用户类型参数错误");
            }
            
            List<Integer> orderStatusList;
            try {
                orderStatusList = Arrays.asList(orderStatus.split(","))
                    .stream()
                    .map(String::trim)
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
            } catch (NumberFormatException e) {
                return Result.fail("订单状态参数格式错误");
            }
            
            AsyncExecutorUtil.submit(() -> {
                try {
                    List<TestUser> testUsers = testUserService.queryUserList(userType);
                    logger.info("获取测试用户列表 - 共{}个用户", testUsers.size());
                    
                    testUsers.parallelStream().forEach(user -> {
                        try {
                            Long uid = user.getUid();
                            
                            List<Long> existingOrderIds = loadTestOrderService.selectOrderIdsByBuyerId(uid);
                            Set<Long> existingOrderIdSet = new HashSet<>(existingOrderIds);
                            
                            List<LoadTestOrder> orderList = loadTestOrderService.getOrderList(uid, orderStatusList);
                            
                            orderList.parallelStream().forEach(order -> {
                                Long orderId = order.getOrderId();
                                if (existingOrderIdSet.contains(orderId)) {
                                    logger.debug("压测订单已存在 - orderId: {}", orderId);
                                } else {
                                    LoadTestOrder newOrder = new LoadTestOrder();
                                    newOrder.setOrderId(orderId);
                                    newOrder.setBuyerId(uid);
                                    newOrder.setBusinessLineId(order.getBusinessLineId());
                                    newOrder.setCurrentStatus(order.getCurrentStatus());
                                    
                                    loadTestOrderService.add(newOrder);
                                    logger.debug("新增压测订单 - orderId: {}", orderId);
                                }
                            });
                        } catch (Exception e) {
                            logger.error("处理用户订单异常 - uid: {}", user.getUid(), e);
                        }
                    });
                    
                    logger.info("记录压测订单完成 - userType: {}", userType);
                } catch (Exception e) {
                    logger.error("记录压测订单异常 - userType: {}", userType, e);
                }
            });
            
            return Result.success("获取订单信息成功", 1);
        } catch (Exception e) {
            logger.error("记录压测订单失败", e);
            return Result.fail("记录订单失败");
        }
    }
    
    @GetMapping("/clearTable")
    public Result<String> clearTable() {
        try {
            logger.warn("清空压测订单表操作");
            Result<String> result = loadTestOrderService.clearTable();
            logger.info("清空压测订单表完成 - result: {}", result.getMessage());
            return result;
        } catch (Exception e) {
            logger.error("清空压测订单表失败", e);
            return Result.fail("清空失败");
        }
    }
    
    @GetMapping("/delOrder")
    public Result<String> deleteOrder(@RequestParam(name = "needDelDateStr") String needDelDateStr) {
        try {
            logger.info("删除指定日期的压测订单 - date: {}", needDelDateStr);
            
            if (needDelDateStr == null || needDelDateStr.trim().isEmpty()) {
                return Result.fail("日期参数不能为空");
            }
            
            Result<String> result = loadTestOrderService.deleteByDate(needDelDateStr);
            logger.info("删除指定日期的压测订单完成 - date: {}, result: {}", needDelDateStr, result.getMessage());
            return result;
        } catch (Exception e) {
            logger.error("删除指定日期的压测订单失败 - date: {}", needDelDateStr, e);
            return Result.fail("删除失败");
        }
    }
    
    @GetMapping("/getOrder")
    public Result<Long> getOrder(@RequestParam(name = "orderType") Integer orderType,
                                @RequestParam(name = "size") Long size) {
        try {
            logger.info("导出压测订单 - orderType: {}, size: {}", orderType, size);
            
            if (orderType == null || (orderType != 1 && orderType != 2)) {
                return Result.fail("订单类型参数错误，只支持1(平台A)或2(平台B)");
            }
            
            if (size == null || size <= 0) {
                return Result.fail("导出数量必须大于0");
            }
            
            if (size > 100000) {
                return Result.fail("单次导出数量不能超过10万条");
            }
            
            Result<Long> result = loadTestOrderService.exportOrder(orderType, size);
            logger.info("导出压测订单提交成功 - taskId: {}", result.getData());
            return result;
        } catch (Exception e) {
            logger.error("导出压测订单失败 - orderType: {}, size: {}", orderType, size, e);
            return Result.fail("导出失败");
        }
    }
    
    @GetMapping("/getTaskById")
    public Result<Object> getOrderTaskById(@RequestParam(name = "taskId") Long taskId) {
        try {
            logger.info("查询导出任务状态 - taskId: {}", taskId);
            
            if (taskId == null) {
                return Result.fail("任务ID不能为空");
            }
            
            Result<Object> result = loadTestOrderService.getTask(taskId);
            logger.info("查询导出任务状态完成 - taskId: {}, status: {}", taskId, 
                       result.isSuccess() ? "成功" : "失败");
            return result;
        } catch (Exception e) {
            logger.error("查询导出任务状态失败 - taskId: {}", taskId, e);
            return Result.fail("查询失败");
        }
    }
    
    @GetMapping("/list")
    public Result<List<LoadTestOrder>> getOrderList(@RequestParam(name = "userType", required = false) Integer userType,
                                                   @RequestParam(name = "pageNum", defaultValue = "1") Integer pageNum,
                                                   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        try {
            logger.info("查询压测订单列表 - userType: {}, pageNum: {}, pageSize: {}", userType, pageNum, pageSize);
            
            if (pageNum <= 0) pageNum = 1;
            if (pageSize <= 0 || pageSize > 100) pageSize = 10;
            
            List<LoadTestOrder> orderList = loadTestOrderService.queryOrderList(userType, pageNum, pageSize);
            logger.info("查询压测订单列表完成 - 共{}条记录", orderList.size());
            
            return Result.success("查询成功", orderList);
        } catch (Exception e) {
            logger.error("查询压测订单列表失败", e);
            return Result.fail("查询失败");
        }
    }
}
