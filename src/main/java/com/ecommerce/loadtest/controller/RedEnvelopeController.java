package com.ecommerce.loadtest.controller;

import com.ecommerce.loadtest.common.Result;
import com.ecommerce.loadtest.dto.RedEnvelopeQueryDTO;
import com.ecommerce.loadtest.entity.RedEnvelope;
import com.ecommerce.loadtest.service.RedEnvelopeService;
import com.ecommerce.loadtest.utils.AsyncExecutorUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 红包控制器
 * 管理红包发放和记录
 * 
 * @author rakkaus
 */
@RestController
@RequestMapping("/redEnvelope")
public class RedEnvelopeController {
    
    private static final Logger logger = LoggerFactory.getLogger(RedEnvelopeController.class);
    
    @Autowired
    private RedEnvelopeService redEnvelopeService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @GetMapping("/queryRedList")
    public Result<List<RedEnvelope>> queryRedEnvelopeList(RedEnvelopeQueryDTO queryDTO) {
        try {
            logger.info("查询红包记录列表 - 查询条件: {}", objectMapper.writeValueAsString(queryDTO));
            
            if (!queryDTO.hasValidParams()) {
                return Result.fail("参数错误，查询参数不能均为空");
            }
            
            List<RedEnvelope> redEnvelopeList = redEnvelopeService.selectRedEnvelopeList(queryDTO);
            logger.info("查询红包记录列表完成 - 共{}条记录", redEnvelopeList.size());
            
            return Result.success("查询成功", redEnvelopeList);
        } catch (Exception e) {
            logger.error("查询红包记录列表失败", e);
            return Result.fail("查询失败");
        }
    }
    
    @PostMapping("/addRedReceive")
    public Result<Integer> addRedEnvelope(@RequestBody RedEnvelope redEnvelope) {
        try {
            logger.info("添加红包记录 - 参数: {}", objectMapper.writeValueAsString(redEnvelope));
            
            if (redEnvelope.getUid() == null) {
                return Result.fail("用户ID不能为空");
            }
            if (redEnvelope.getRedEnvelopeId() == null) {
                return Result.fail("红包ID不能为空");
            }
            
            int result = redEnvelopeService.insertSelective(redEnvelope);
            if (result > 0) {
                logger.info("添加红包记录成功 - uid: {}, redEnvelopeId: {}", 
                           redEnvelope.getUid(), redEnvelope.getRedEnvelopeId());
                return Result.success("操作成功", result);
            } else {
                return Result.fail("添加失败");
            }
        } catch (Exception e) {
            logger.error("添加红包记录失败", e);
            return Result.fail("操作失败");
        }
    }
    
    @GetMapping("/sendRedReceive")
    public Result<Integer> sendRedEnvelope(@RequestParam(name = "batchId") Long batchId,
                                          @RequestParam(name = "planId") Long planId,
                                          @RequestParam(name = "userType") Integer userType,
                                          @RequestParam(name = "threadNum", defaultValue = "4") Integer threadNum) {
        try {
            logger.info("批量发放红包 - batchId: {}, planId: {}, userType: {}, threadNum: {}", 
                       batchId, planId, userType, threadNum);
            
            if (batchId == null) {
                return Result.fail("批次ID不能为空");
            }
            if (planId == null) {
                return Result.fail("红包计划ID不能为空");
            }
            if (userType == null || (userType != 1 && userType != 2)) {
                return Result.fail("用户类型参数错误，只支持1(平台A)或2(平台B)");
            }
            if (threadNum <= 0 || threadNum > 20) {
                threadNum = 4;
                logger.warn("线程数参数异常，使用默认值: {}", threadNum);
            }
            
            final Integer finalThreadNum = threadNum;
            AsyncExecutorUtil.submit(() -> {
                try {
                    Long totalSent = redEnvelopeService.sendRedEnvelope(batchId, planId, userType, finalThreadNum);
                    logger.info("批量发放红包完成 - 总共发放{}个红包", totalSent);
                } catch (Exception e) {
                    logger.error("批量发放红包异常 - batchId: {}, planId: {}", batchId, planId, e);
                }
            });
            
            return Result.success("提交成功，开始异步发放", 1);
        } catch (Exception e) {
            logger.error("批量发放红包失败", e);
            return Result.fail("提交失败");
        }
    }
    
    @GetMapping("/getRedData")
    public Result<Long> exportRedEnvelopeData(@RequestParam(name = "batchId") Long batchId) {
        try {
            logger.info("导出红包数据 - batchId: {}", batchId);
            
            if (batchId == null) {
                return Result.fail("批次ID不能为空");
            }
            
            Result<Long> result = redEnvelopeService.exportRedEnvelope(batchId);
            logger.info("导出红包数据提交成功 - batchId: {}, taskId: {}", batchId, result.getData());
            
            return result;
        } catch (Exception e) {
            logger.error("导出红包数据失败 - batchId: {}", batchId, e);
            return Result.fail("导出失败");
        }
    }
    
    @GetMapping("/statistics")
    public Result<Object> getRedEnvelopeStatistics(@RequestParam(name = "batchId") Long batchId) {
        try {
            logger.info("获取红包统计信息 - batchId: {}", batchId);
            
            if (batchId == null) {
                return Result.fail("批次ID不能为空");
            }
            
            Result<Object> result = redEnvelopeService.getRedEnvelopeStatistics(batchId);
            logger.info("获取红包统计信息完成 - batchId: {}", batchId);
            
            return result;
        } catch (Exception e) {
            logger.error("获取红包统计信息失败 - batchId: {}", batchId, e);
            return Result.fail("查询失败");
        }
    }
    
    @GetMapping("/planDetail")
    public Result<Object> getRedPlanDetail(@RequestParam(name = "planId") Long planId) {
        try {
            logger.info("查询红包计划详情 - planId: {}", planId);
            
            if (planId == null) {
                return Result.fail("计划ID不能为空");
            }
            
            int elementCount = redEnvelopeService.getRedPlanElementCount(planId);
            
            Map<String, Object> planDetail = new HashMap<>();
            planDetail.put("planId", planId);
            planDetail.put("elementCount", elementCount);
            planDetail.put("status", "active");
            planDetail.put("createTime", System.currentTimeMillis());
            
            logger.info("查询红包计划详情完成 - planId: {}, elementCount: {}", planId, elementCount);
            return Result.success("查询成功", planDetail);
        } catch (Exception e) {
            logger.error("查询红包计划详情失败 - planId: {}", planId, e);
            return Result.fail("查询失败");
        }
    }
    
    @DeleteMapping("/deleteRedReceive")
    public Result<Integer> deleteRedEnvelope(@RequestParam(name = "receiveId") Long receiveId) {
        try {
            logger.info("删除红包记录 - receiveId: {}", receiveId);
            
            if (receiveId == null) {
                return Result.fail("记录ID不能为空");
            }
            
            int result = redEnvelopeService.deleteByPrimaryKey(receiveId);
            if (result > 0) {
                logger.info("删除红包记录成功 - receiveId: {}", receiveId);
                return Result.success("操作成功", result);
            } else {
                return Result.fail("删除失败，记录不存在");
            }
        } catch (Exception e) {
            logger.error("删除红包记录失败 - receiveId: {}", receiveId, e);
            return Result.fail("操作失败");
        }
    }
}
