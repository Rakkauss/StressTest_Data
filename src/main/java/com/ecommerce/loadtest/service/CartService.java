package com.ecommerce.loadtest.service;

import java.util.List;
import java.util.Map;

/**
 * 购物车服务接口
 * 
 * @author rakkaus
 */
public interface CartService {
    
    /**
     * 添加商品到购物车
     */
    boolean addToCart(Long userId, Long productId, Integer quantity);
    
    /**
     * 从购物车移除商品
     */
    boolean removeFromCart(Long userId, Long productId);
    
    /**
     * 更新购物车商品数量
     */
    boolean updateCartQuantity(Long userId, Long productId, Integer quantity);
    
    /**
     * 获取用户购物车
     */
    List<Map<String, Object>> getUserCart(Long userId);
    
    /**
     * 清空用户购物车
     */
    boolean clearCart(Long userId);
    
    /**
     * 获取购物车统计信息
     */
    Map<String, Object> getCartStatistics(Long userId);
    
    /**
     * 批量添加商品到购物车
     */
    int batchAddToCart(Long userId, List<Map<String, Object>> items);
    
    /**
     * 清理过期购物车
     */
    int cleanupExpiredCarts(Integer expireDays);
}
