package com.ecommerce.loadtest.controller;

import com.ecommerce.loadtest.common.Result;
import com.ecommerce.loadtest.entity.TestBatch;
import com.ecommerce.loadtest.service.TestBatchService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 测试批次控制器
 * 管理压测批次
 * 
 * @author rakkaus
 */
@RestController
@RequestMapping("/batch")
public class TestBatchController {
    
    private static final Logger logger = LoggerFactory.getLogger(TestBatchController.class);
    
    @Autowired
    private TestBatchService testBatchService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @GetMapping("/queryBatchListByStatus")
    public Result<List<TestBatch>> queryBatchListByStatus(@RequestParam(name = "status", defaultValue = "0") Integer status) {
        try {
            logger.info("查询压测批次列表 - status: {}", status);
            
            if (status == null || status < 0 || status > 2) {
                return Result.fail("批次状态参数错误，只支持0(新建)、1(进行中)、2(已完成)");
            }
            
            List<TestBatch> batchList = testBatchService.selectBatchListByStatus(status);
            logger.info("查询压测批次列表完成 - status: {}, 共{}个批次", status, batchList.size());
            
            return Result.success("查询成功", batchList);
        } catch (Exception e) {
            logger.error("查询压测批次列表失败 - status: {}", status, e);
            return Result.fail("数据异常");
        }
    }
    
    @GetMapping("/queryBatchById")
    public Result<TestBatch> queryBatchById(@RequestParam(name = "batchId") Long batchId) {
        try {
            logger.info("查询压测批次详情 - batchId: {}", batchId);
            
            if (batchId == null) {
                return Result.fail("批次ID不能为空");
            }
            
            TestBatch batch = testBatchService.selectByPrimaryKey(batchId);
            if (batch == null) {
                logger.warn("批次不存在 - batchId: {}", batchId);
                return Result.fail("批次不存在");
            }
            
            logger.info("查询压测批次详情完成 - batchId: {}, createUser: {}", batchId, batch.getCreateUser());
            return Result.success("查询成功", batch);
        } catch (Exception e) {
            logger.error("查询压测批次详情失败 - batchId: {}", batchId, e);
            return Result.fail("数据异常");
        }
    }
    
    @GetMapping("/addBatch")
    public Result<Long> addBatch(@RequestParam(name = "createUser") String createUser,
                                @RequestParam(name = "totalCount", defaultValue = "10000") Long totalCount) {
        try {
            logger.info("创建压测批次 - createUser: {}, totalCount: {}", createUser, totalCount);
            
            if (createUser == null || createUser.trim().isEmpty()) {
                return Result.fail("创建用户不能为空");
            }
            
            if (totalCount == null || totalCount <= 0) {
                return Result.fail("计划总数必须大于0");
            }
            
            if (totalCount > 1000000) {
                return Result.fail("单批次计划总数不能超过100万");
            }
            
            TestBatch batch = new TestBatch();
            batch.setCreateUser(createUser.trim());
            batch.setTotalCount(totalCount);
            batch.setCreateTime(System.currentTimeMillis());
            batch.setBatchStatus(0);
            batch.setRealCount(0L);
            
            Long batchId = testBatchService.insertSelective(batch);
            if (batchId != null && batchId > 0) {
                logger.info("创建压测批次成功 - batchId: {}, createUser: {}", batchId, createUser);
                return Result.success("操作成功", batchId);
            } else {
                return Result.fail("创建失败");
            }
        } catch (Exception e) {
            logger.error("创建压测批次失败 - createUser: {}, totalCount: {}", createUser, totalCount, e);
            return Result.fail("操作失败");
        }
    }
    
    @PostMapping("/updateBatch")
    public Result<Integer> updateBatch(@RequestBody TestBatch batch) {
        try {
            logger.info("更新压测批次 - 参数: {}", objectMapper.writeValueAsString(batch));
            
            if (batch.getBatchId() == null) {
                return Result.fail("批次ID不能为空");
            }
            
            if (batch.getBatchStatus() != null && (batch.getBatchStatus() < 0 || batch.getBatchStatus() > 2)) {
                return Result.fail("批次状态参数错误");
            }
            
            if (batch.getTotalCount() != null && batch.getTotalCount() <= 0) {
                return Result.fail("计划总数必须大于0");
            }
            
            if (batch.getRealCount() != null && batch.getRealCount() < 0) {
                return Result.fail("实际执行数不能小于0");
            }
            
            int result = testBatchService.updateByPrimaryKeySelective(batch);
            if (result > 0) {
                logger.info("更新压测批次成功 - batchId: {}", batch.getBatchId());
                return Result.success("操作成功", result);
            } else {
                return Result.fail("更新失败，批次不存在");
            }
        } catch (Exception e) {
            logger.error("更新压测批次失败", e);
            return Result.fail("操作失败");
        }
    }
    
    @DeleteMapping("/deleteBatch")
    public Result<Integer> deleteBatch(@RequestParam(name = "batchId") Long batchId) {
        try {
            logger.info("删除压测批次 - batchId: {}", batchId);
            
            if (batchId == null) {
                return Result.fail("批次ID不能为空");
            }
            
            TestBatch batch = testBatchService.selectByPrimaryKey(batchId);
            if (batch == null) {
                return Result.fail("批次不存在");
            }
            
            if (batch.getBatchStatus() != 0) {
                return Result.fail("只能删除新建状态的批次");
            }
            
            int result = testBatchService.deleteByPrimaryKey(batchId);
            if (result > 0) {
                logger.info("删除压测批次成功 - batchId: {}", batchId);
                return Result.success("操作成功", result);
            } else {
                return Result.fail("删除失败");
            }
        } catch (Exception e) {
            logger.error("删除压测批次失败 - batchId: {}", batchId, e);
            return Result.fail("操作失败");
        }
    }
    
    @PostMapping("/startBatch")
    public Result<String> startBatch(@RequestParam(name = "batchId") Long batchId) {
        try {
            logger.info("启动压测批次 - batchId: {}", batchId);
            
            if (batchId == null) {
                return Result.fail("批次ID不能为空");
            }
            
            TestBatch batch = testBatchService.selectByPrimaryKey(batchId);
            if (batch == null) {
                return Result.fail("批次不存在");
            }
            
            if (batch.getBatchStatus() != 0) {
                return Result.fail("只能启动新建状态的批次");
            }
            
            TestBatch updateBatch = new TestBatch();
            updateBatch.setBatchId(batchId);
            updateBatch.setBatchStatus(1);
            
            int result = testBatchService.updateByPrimaryKeySelective(updateBatch);
            if (result > 0) {
                logger.info("启动压测批次成功 - batchId: {}", batchId);
                return Result.success("批次启动成功");
            } else {
                return Result.fail("启动失败");
            }
        } catch (Exception e) {
            logger.error("启动压测批次失败 - batchId: {}", batchId, e);
            return Result.fail("操作失败");
        }
    }
    
    @PostMapping("/completeBatch")
    public Result<String> completeBatch(@RequestParam(name = "batchId") Long batchId,
                                       @RequestParam(name = "realCount") Long realCount) {
        try {
            logger.info("完成压测批次 - batchId: {}, realCount: {}", batchId, realCount);
            
            if (batchId == null) {
                return Result.fail("批次ID不能为空");
            }
            
            if (realCount == null || realCount < 0) {
                return Result.fail("实际执行数不能小于0");
            }
            
            TestBatch batch = testBatchService.selectByPrimaryKey(batchId);
            if (batch == null) {
                return Result.fail("批次不存在");
            }
            
            if (batch.getBatchStatus() != 1) {
                return Result.fail("只能完成进行中状态的批次");
            }
            
            TestBatch updateBatch = new TestBatch();
            updateBatch.setBatchId(batchId);
            updateBatch.setBatchStatus(2);
            updateBatch.setRealCount(realCount);
            
            int result = testBatchService.updateByPrimaryKeySelective(updateBatch);
            if (result > 0) {
                logger.info("完成压测批次成功 - batchId: {}, realCount: {}", batchId, realCount);
                return Result.success("批次完成成功");
            } else {
                return Result.fail("完成失败");
            }
        } catch (Exception e) {
            logger.error("完成压测批次失败 - batchId: {}, realCount: {}", batchId, realCount, e);
            return Result.fail("操作失败");
        }
    }
    
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getBatchStatistics() {
        try {
            logger.info("获取批次统计信息");
            
            Map<String, Object> statistics = testBatchService.getBatchStatistics();
            logger.info("获取批次统计信息完成 - {}", statistics);
            
            return Result.success("查询成功", statistics);
        } catch (Exception e) {
            logger.error("获取批次统计信息失败", e);
            return Result.fail("查询失败");
        }
    }
}

