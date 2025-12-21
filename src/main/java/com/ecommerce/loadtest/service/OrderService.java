package com.ecommerce.loadtest.service;

import java.util.List;
import java.util.Map;

/**
 * 订单服务接口
 * 
 * @author rakkaus
 */
public interface OrderService {
    
    /**
     * 创建订单
     */
    Long createOrder(Long userId, List<Map<String, Object>> cartItems);
    
    /**
     * 取消订单
     */
    boolean cancelOrder(Long orderId, Long userId, String reason);
    
    /**
     * 支付订单
     */
    boolean payOrder(Long orderId, Long userId, String paymentMethod);
    
    /**
     * 获取用户订单列表
     */
    List<Map<String, Object>> getUserOrders(Long userId, Integer status, Integer limit);
    
    /**
     * 获取订单详情
     */
    Map<String, Object> getOrderDetail(Long orderId, Long userId);
    
    /**
     * 批量创建订单
     */
    int batchCreateOrders(List<Map<String, Object>> orderRequests);
    
    /**
     * 批量取消订单
     */
    int batchCancelOrders(List<Long> orderIds, String reason);
    
    /**
     * 获取订单统计信息
     */
    Map<String, Object> getOrderStatistics(Long userId);
    
    /**
     * 清理测试订单
     */
    int cleanupTestOrders(Integer days);
}
