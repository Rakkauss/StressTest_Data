package com.ecommerce.loadtest.dao;

import com.ecommerce.loadtest.dto.RedEnvelopeQueryDTO;
import com.ecommerce.loadtest.entity.RedEnvelope;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 红包记录数据访问接口
 * 
 * @author rakkaus
 */
@Mapper
public interface RedEnvelopeMapper {
    
    int deleteByPrimaryKey(@Param("receiveId") Long receiveId);
    
    int insertSelective(RedEnvelope record);
    
    RedEnvelope selectByPrimaryKey(@Param("receiveId") Long receiveId);
    
    int updateByPrimaryKeySelective(RedEnvelope record);
    
    List<RedEnvelope> selectRedEnvelopeList(RedEnvelopeQueryDTO queryDTO);
    
    List<RedEnvelope> selectByBatchId(@Param("batchId") Long batchId);
    
    List<RedEnvelope> selectByUid(@Param("uid") Long uid);
    
    int batchInsert(@Param("records") List<RedEnvelope> records);
    
    Map<String, Object> selectBatchStatistics(@Param("batchId") Long batchId);
    
    List<java.util.Map<String, Object>> selectRedEnvelopeDataForExport(@Param("batchId") Long batchId,
                                                                       @Param("status") Integer status,
                                                                       @Param("platformType") Integer platformType);
}
