package com.ecommerce.loadtest.dao;

import com.ecommerce.loadtest.entity.LoadTestOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 压测订单数据访问接口
 * 
 * @author rakkaus
 */
@Mapper
public interface LoadTestOrderMapper {
    
    int insertSelective(LoadTestOrder record);
    
    LoadTestOrder selectByPrimaryKey(@Param("id") Long id);
    
    LoadTestOrder selectByOrderId(@Param("orderId") Long orderId);
    
    int updateByPrimaryKeySelective(LoadTestOrder record);
    
    int updateOrderStatus(@Param("orderId") Long orderId,
                         @Param("currentStatus") Integer currentStatus);
    
    List<Long> selectOrderIdsByBuyerId(@Param("buyerId") Long buyerId);
    
    List<LoadTestOrder> selectByBuyerId(@Param("buyerId") Long buyerId);
    
    List<LoadTestOrder> selectByConditions(@Param("buyerId") Long buyerId,
                                          @Param("currentStatus") Integer currentStatus,
                                          @Param("businessLineId") Integer businessLineId,
                                          @Param("startTime") Date startTime,
                                          @Param("endTime") Date endTime);
    
    int clearTable();
    
    int deleteByDateString(@Param("dateStr") String dateStr);
    
    int batchInsert(@Param("orders") List<LoadTestOrder> orders);
}
