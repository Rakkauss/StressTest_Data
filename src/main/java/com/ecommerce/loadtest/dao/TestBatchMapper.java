package com.ecommerce.loadtest.dao;

import com.ecommerce.loadtest.entity.TestBatch;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 测试批次数据访问接口
 * 
 * @author rakkaus
 */
@Mapper
public interface TestBatchMapper {
    
    int deleteByPrimaryKey(@Param("batchId") Long batchId);
    
    Long insertSelective(TestBatch record);
    
    TestBatch selectByPrimaryKey(@Param("batchId") Long batchId);
    
    int updateByPrimaryKeySelective(TestBatch record);
    
    List<TestBatch> selectBatchListByStatus(@Param("status") Integer status);
    
    Map<String, Object> selectBatchStatistics(@Param("createUser") String createUser);
}

