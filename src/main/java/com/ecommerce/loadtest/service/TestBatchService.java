package com.ecommerce.loadtest.service;

import com.ecommerce.loadtest.entity.TestBatch;

import java.util.List;
import java.util.Map;

/**
 * 测试批次服务接口
 * 
 * @author rakkaus
 */
public interface TestBatchService {
    
    int deleteByPrimaryKey(Long batchId);
    
    Long insertSelective(TestBatch record);
    
    TestBatch selectByPrimaryKey(Long batchId);
    
    int updateByPrimaryKeySelective(TestBatch record);
    
    List<TestBatch> selectBatchListByStatus(Integer status);
    
    Map<String, Object> getBatchStatistics();
}

