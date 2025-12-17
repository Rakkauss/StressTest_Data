package com.ecommerce.loadtest.service;

import com.ecommerce.loadtest.common.Result;
import com.ecommerce.loadtest.dto.RedEnvelopeQueryDTO;
import com.ecommerce.loadtest.entity.RedEnvelope;

import java.util.List;

/**
 * 红包服务接口
 * 
 * @author rakkaus
 */
public interface RedEnvelopeService {
    
    int deleteByPrimaryKey(Long receiveId);
    
    int insertSelective(RedEnvelope record);
    
    RedEnvelope selectByPrimaryKey(Long receiveId);
    
    int updateByPrimaryKeySelective(RedEnvelope record);
    
    List<RedEnvelope> selectRedEnvelopeList(RedEnvelopeQueryDTO queryDTO);
    
    Long sendRedEnvelope(Long batchId, Long planId, Integer userType, Integer threadNum) throws InterruptedException;
    
    Result<Long> exportRedEnvelope(Long batchId);
    
    int getRedPlanElementCount(Long planId);
    
    Result<Object> getRedEnvelopeStatistics(Long batchId);
}
