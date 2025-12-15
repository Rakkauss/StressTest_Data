package com.ecommerce.loadtest.service;

import com.ecommerce.loadtest.common.Result;
import com.ecommerce.loadtest.entity.LoadTestOrder;

import java.util.List;

/**
 * 压测订单服务接口
 * 
 * @author rakkaus
 */
public interface LoadTestOrderService {
    
    int add(LoadTestOrder order);
    
    int updatePressureOrder(LoadTestOrder order);
    
    int updateOrderStatus(Long orderId, Integer status);
    
    List<Long> selectOrderIdsByBuyerId(Long buyerId);
    
    List<LoadTestOrder> getOrderList(Long uid, List<Integer> statusList);
    
    int cancelOrderList(Integer userType, Integer threadNum);
    
    Result<String> clearTable();
    
    Result<String> deleteByDate(String dateStr);
    
    Result<Long> exportOrder(Integer orderType, Long size);
    
    Result<Object> getTask(Long taskId);
    
    List<LoadTestOrder> queryOrderList(Integer userType, Integer pageNum, Integer pageSize);
}
