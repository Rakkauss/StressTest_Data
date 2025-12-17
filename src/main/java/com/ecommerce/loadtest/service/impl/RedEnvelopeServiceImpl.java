package com.ecommerce.loadtest.service.impl;

import com.ecommerce.loadtest.common.Result;
import com.ecommerce.loadtest.dao.RedEnvelopeMapper;
import com.ecommerce.loadtest.dto.RedEnvelopeQueryDTO;
import com.ecommerce.loadtest.entity.RedEnvelope;
import com.ecommerce.loadtest.entity.TestBatch;
import com.ecommerce.loadtest.entity.TestUser;
import com.ecommerce.loadtest.service.RedEnvelopeService;
import com.ecommerce.loadtest.service.TestBatchService;
import com.ecommerce.loadtest.service.TestUserService;
import com.ecommerce.loadtest.utils.ConfigUtil;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 红包服务实现类
 * 
 * @author rakkaus
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class RedEnvelopeServiceImpl implements RedEnvelopeService {
    
    private static final Logger logger = LoggerFactory.getLogger(RedEnvelopeServiceImpl.class);
    
    @Autowired
    private RedEnvelopeMapper redEnvelopeMapper;
    
    @Autowired
    private TestUserService testUserService;
    
    @Autowired
    private TestBatchService testBatchService;
    
    private static final Map<Long, Map<String, Object>> exportTaskStorage = new HashMap<>();
    private static final AtomicLong taskIdGenerator = new AtomicLong(2000);
    
    public static final int TIME_INTERVAL = 1;
    public static final int TIME_INTERVAL_MILLIS = TIME_INTERVAL * 1000;
    
    @Override
    public int deleteByPrimaryKey(Long receiveId) {
        logger.info("删除红包记录 - receiveId: {}", receiveId);
        try {
            return redEnvelopeMapper.deleteByPrimaryKey(receiveId);
        } catch (Exception e) {
            logger.error("删除红包记录失败 - receiveId: {}", receiveId, e);
            throw new RuntimeException("删除红包记录失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public int insertSelective(RedEnvelope record) {
        logger.info("插入红包记录 - uid: {}, redEnvelopeId: {}", record.getUid(), record.getRedEnvelopeId());
        try {
            if (record.getCreateTime() == null) {
                record.setCreateTime(new Date());
            }
            if (record.getStatus() == null) {
                record.setStatus(1);
            }
            return redEnvelopeMapper.insertSelective(record);
        } catch (Exception e) {
            logger.error("插入红包记录失败 - uid: {}", record.getUid(), e);
            throw new RuntimeException("插入红包记录失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public RedEnvelope selectByPrimaryKey(Long receiveId) {
        logger.debug("查询红包记录 - receiveId: {}", receiveId);
        try {
            return redEnvelopeMapper.selectByPrimaryKey(receiveId);
        } catch (Exception e) {
            logger.error("查询红包记录失败 - receiveId: {}", receiveId, e);
            throw new RuntimeException("查询红包记录失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public int updateByPrimaryKeySelective(RedEnvelope record) {
        logger.info("更新红包记录 - receiveId: {}", record.getReceiveId());
        try {
            record.setUpdateTime(new Date());
            return redEnvelopeMapper.updateByPrimaryKeySelective(record);
        } catch (Exception e) {
            logger.error("更新红包记录失败 - receiveId: {}", record.getReceiveId(), e);
            throw new RuntimeException("更新红包记录失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<RedEnvelope> selectRedEnvelopeList(RedEnvelopeQueryDTO queryDTO) {
        logger.info("查询红包记录列表 - 查询条件: {}", queryDTO);
        try {
            return redEnvelopeMapper.selectRedEnvelopeList(queryDTO);
        } catch (Exception e) {
            logger.error("查询红包记录列表失败", e);
            throw new RuntimeException("查询红包记录列表失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public Long sendRedEnvelope(Long batchId, Long planId, Integer userType, Integer threadNum) throws InterruptedException {
        logger.info("开始批量发放红包 - batchId: {}, planId: {}, userType: {}, threadNum: {}", 
                   batchId, planId, userType, threadNum);
        
        ForkJoinPool pool = new ForkJoinPool(Math.min(threadNum, 5));
        int redElementCount = getRedPlanElementCount(planId);
        AtomicLong totalSentCount = new AtomicLong(0);
        
        TestBatch batch = testBatchService.selectByPrimaryKey(batchId);
        if (batch == null) {
            logger.error("批次不存在 - batchId: {}", batchId);
            return 0L;
        }
        
        if (batch.getBatchStatus() == 0) {
            TestBatch updateBatch = new TestBatch();
            updateBatch.setBatchId(batchId);
            updateBatch.setBatchStatus(1);
            testBatchService.updateByPrimaryKeySelective(updateBatch);
        }
        
        try {
            Long totalCount = batch.getTotalCount();
            Long sendCount = totalCount / redElementCount;
            
            List<TestUser> userList = testUserService.queryUserList(userType);
            logger.info("获取到{}个测试用户", userList.size());
            
            if (userList.isEmpty()) {
                logger.warn("没有找到指定类型的测试用户 - userType: {}", userType);
                return 0L;
            }
            
            int averagePerUser = (int) (sendCount / userList.size());
            int remainder = (int) (sendCount % userList.size());
            
            logger.info("发放策略 - 总次数: {}, 平均每人: {}, 余数: {}", sendCount, averagePerUser, remainder);
            
            int userPartitionCapacity = Math.max(1, userList.size() / Math.min(threadNum, 5));
            List<List<TestUser>> userPartitionList = Lists.partition(userList, userPartitionCapacity);
            
            AtomicReference<CurrentLimiting> currentLimiting = new AtomicReference<>(new CurrentLimiting());
            currentLimiting.get().setRedReceive(0);
            currentLimiting.get().setStartTime(System.currentTimeMillis());
            
            CountDownLatch countDownLatch = new CountDownLatch(userList.size() * averagePerUser);
            
            userPartitionList.forEach(userGroup -> {
                pool.submit(() -> {
                    userGroup.parallelStream().forEach(user -> {
                        for (int i = 0; i < averagePerUser; i++) {
                            try {
                                int sentCount = 0;
                                if (userType == 1) {
                                    sentCount = mockPlatformARedEnvelope(batch, user, planId);
                                } else if (userType == 2) {
                                    sentCount = mockPlatformBRedEnvelope(batch, user, planId);
                                }
                                
                                totalSentCount.addAndGet(sentCount);
                                currentLimiting.set(distributeRedEnvelopes(currentLimiting.get(), pool.getPoolSize()));
                                
                            } catch (Exception e) {
                                logger.error("发放红包异常 - uid: {}", user.getUid(), e);
                            } finally {
                                countDownLatch.countDown();
                            }
                        }
                    });
                });
            });
            
            countDownLatch.await();
            
            if (remainder > 0) {
                for (int i = 0; i < remainder && i < userList.size(); i++) {
                    try {
                        TestUser user = userList.get(i);
                        int sentCount = 0;
                        if (userType == 1) {
                            sentCount = mockPlatformARedEnvelope(batch, user, planId);
                        } else if (userType == 2) {
                            sentCount = mockPlatformBRedEnvelope(batch, user, planId);
                        }
                        totalSentCount.addAndGet(sentCount);
                        currentLimiting.set(distributeRedEnvelopes(currentLimiting.get(), 1));
                    } catch (Exception e) {
                        logger.error("发放余数红包异常", e);
                    }
                }
            }
            
            TestBatch completeBatch = new TestBatch();
            completeBatch.setBatchId(batchId);
            completeBatch.setBatchStatus(2);
            completeBatch.setRealCount(totalSentCount.get());
            testBatchService.updateByPrimaryKeySelective(completeBatch);
            
            logger.info("批量发放红包完成 - 总共发放{}个红包", totalSentCount.get());
            return totalSentCount.get();
            
        } finally {
            pool.shutdown();
        }
    }
    
    @Override
    public Result<Long> exportRedEnvelope(Long batchId) {
        logger.info("导出红包数据 - batchId: {}", batchId);
        
        Long taskId = taskIdGenerator.getAndIncrement();
        
        Map<String, Object> taskInfo = new HashMap<>();
        taskInfo.put("taskId", taskId);
        taskInfo.put("batchId", batchId);
        taskInfo.put("status", 0);
        taskInfo.put("createTime", new Date());
        taskInfo.put("fileName", String.format("red_envelope_data_%d_%d.xlsx", batchId, taskId));
        
        synchronized (exportTaskStorage) {
            exportTaskStorage.put(taskId, taskInfo);
        }
        
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(2000 + new Random().nextInt(3000));
                
                List<RedEnvelope> envelopes = redEnvelopeMapper.selectByBatchId(batchId);
                
                taskInfo.put("status", 1);
                taskInfo.put("completeTime", new Date());
                taskInfo.put("totalCount", envelopes.size());
                taskInfo.put("fileUrl", "/mock/download/" + taskInfo.get("fileName"));
                
                logger.info("红包数据导出完成 - taskId: {}, 共{}条记录", taskId, envelopes.size());
                
            } catch (InterruptedException e) {
                taskInfo.put("status", 2);
                taskInfo.put("errorMessage", "导出被中断");
                logger.error("红包数据导出被中断 - taskId: {}", taskId);
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                taskInfo.put("status", 2);
                taskInfo.put("errorMessage", e.getMessage());
                logger.error("红包数据导出失败 - taskId: {}", taskId, e);
            }
        });
        
        return Result.success("导出任务创建成功", taskId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public int getRedPlanElementCount(Long planId) {
        logger.info("Mock查询红包计划元数据 - planId: {}", planId);
        return 3 + new Random().nextInt(5);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Result<Object> getRedEnvelopeStatistics(Long batchId) {
        logger.info("获取红包统计信息 - batchId: {}", batchId);
        try {
            Map<String, Object> statistics = redEnvelopeMapper.selectBatchStatistics(batchId);
            return Result.success("查询成功", statistics);
        } catch (Exception e) {
            logger.error("获取红包统计信息失败", e);
            return Result.fail("查询失败: " + e.getMessage());
        }
    }
    
    private int mockPlatformARedEnvelope(TestBatch batch, TestUser user, Long planId) throws Exception {
        logger.debug("Mock平台A红包发放 - uid: {}, planId: {}", user.getUid(), planId);
        
        Thread.sleep(10 + new Random().nextInt(20));
        
        int redCount = 1 + new Random().nextInt(3);
        
        for (int i = 0; i < redCount; i++) {
            Long redEnvelopeId = planId * 1000 + user.getUid() + i;
            RedEnvelope redEnvelope = new RedEnvelope(user.getUid(), redEnvelopeId, System.currentTimeMillis(), batch.getBatchId());
            redEnvelope.setAmount(100L + new Random().nextInt(900));
            redEnvelope.setPlatformType(1);
            
            insertSelective(redEnvelope);
        }
        
        logger.debug("平台A红包发放成功 - uid: {}, 发放{}个红包", user.getUid(), redCount);
        return redCount;
    }
    
    private int mockPlatformBRedEnvelope(TestBatch batch, TestUser user, Long planId) throws Exception {
        logger.debug("Mock平台B红包发放 - uid: {}, planId: {}", user.getUid(), planId);
        
        Thread.sleep(15 + new Random().nextInt(25));
        
        int redCount = 1 + new Random().nextInt(2);
        
        for (int i = 0; i < redCount; i++) {
            Long redEnvelopeId = planId * 2000 + user.getUid() + i;
            RedEnvelope redEnvelope = new RedEnvelope(user.getUid(), redEnvelopeId, System.currentTimeMillis(), batch.getBatchId());
            redEnvelope.setAmount(200L + new Random().nextInt(800));
            redEnvelope.setPlatformType(2);
            
            insertSelective(redEnvelope);
        }
        
        logger.debug("平台B红包发放成功 - uid: {}, 发放{}个红包", user.getUid(), redCount);
        return redCount;
    }
    
    private CurrentLimiting distributeRedEnvelopes(CurrentLimiting currentLimiting, int threadNum) {
        int redReceive = currentLimiting.getRedReceive();
        Long currentTimeMillis = currentLimiting.getStartTime();
        
        int maxRedEnvelopePerMinute = ConfigUtil.getInt("red_envelope_current_limiting", 300);
        int minuteQuantity = (maxRedEnvelopePerMinute * TIME_INTERVAL) / threadNum;
        
        if (redReceive > minuteQuantity) {
            long timeDifference = System.currentTimeMillis() - currentTimeMillis;
            
            if (timeDifference < TIME_INTERVAL_MILLIS) {
                long sleepTime = TIME_INTERVAL_MILLIS - timeDifference;
                try {
                    Thread.sleep(sleepTime);
                    redReceive = 0;
                    currentTimeMillis = System.currentTimeMillis();
                } catch (InterruptedException e) {
                    logger.error("红包发放等待失败", e);
                    Thread.currentThread().interrupt();
                }
            } else {
                redReceive = 0;
                currentTimeMillis = System.currentTimeMillis();
            }
        }
        
        CurrentLimiting currentLimit = new CurrentLimiting();
        currentLimit.setRedReceive(++redReceive);
        currentLimit.setStartTime(currentTimeMillis);
        
        return currentLimit;
    }
    
    private static class CurrentLimiting {
        private int redReceive;
        private Long startTime;
        
        public int getRedReceive() {
            return redReceive;
        }
        
        public void setRedReceive(int redReceive) {
            this.redReceive = redReceive;
        }
        
        public Long getStartTime() {
            return startTime;
        }
        
        public void setStartTime(Long startTime) {
            this.startTime = startTime;
        }
    }
}
