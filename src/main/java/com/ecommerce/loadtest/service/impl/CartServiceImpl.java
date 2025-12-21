package com.ecommerce.loadtest.service.impl;

import com.ecommerce.loadtest.service.CartService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 购物车服务实现类
 * 
 * @author rakkaus
 */
@Service
public class CartServiceImpl implements CartService {
    
    private static final Logger logger = LoggerFactory.getLogger(CartServiceImpl.class);
    
    private static final Map<Long, Map<Long, Map<String, Object>>> cartStorage = new ConcurrentHashMap<>();
    
    @Override
    public boolean addToCart(Long userId, Long productId, Integer quantity) {
        logger.info("添加商品到购物车 - userId: {}, productId: {}, quantity: {}", userId, productId, quantity);
        
        try {
            if (!mockCheckInventory(productId, quantity)) {
                logger.warn("库存不足 - productId: {}, quantity: {}", productId, quantity);
                return false;
            }
            
            Long price = mockGetProductPrice(productId);
            
            Map<Long, Map<String, Object>> userCart = cartStorage.computeIfAbsent(userId, k -> new ConcurrentHashMap<>());
            
            Map<String, Object> item = userCart.computeIfAbsent(productId, k -> new HashMap<>());
            Integer currentQuantity = (Integer) item.getOrDefault("quantity", 0);
            
            item.put("quantity", currentQuantity + quantity);
            item.put("price", price);
            item.put("addTime", System.currentTimeMillis());
            item.put("updateTime", System.currentTimeMillis());
            
            logger.info("商品添加成功 - userId: {}, productId: {}, 新数量: {}", userId, productId, currentQuantity + quantity);
            return true;
            
        } catch (Exception e) {
            logger.error("添加商品到购物车失败", e);
            return false;
        }
    }
    
    @Override
    public boolean removeFromCart(Long userId, Long productId) {
        logger.info("从购物车移除商品 - userId: {}, productId: {}", userId, productId);
        
        try {
            Map<Long, Map<String, Object>> userCart = cartStorage.get(userId);
            if (userCart == null) {
                logger.warn("用户购物车不存在 - userId: {}", userId);
                return false;
            }
            
            Map<String, Object> removedItem = userCart.remove(productId);
            if (removedItem == null) {
                logger.warn("商品不在购物车中 - userId: {}, productId: {}", userId, productId);
                return false;
            }
            
            logger.info("商品移除成功 - userId: {}, productId: {}", userId, productId);
            return true;
            
        } catch (Exception e) {
            logger.error("从购物车移除商品失败", e);
            return false;
        }
    }
    
    @Override
    public boolean updateCartQuantity(Long userId, Long productId, Integer quantity) {
        logger.info("更新购物车商品数量 - userId: {}, productId: {}, quantity: {}", userId, productId, quantity);
        
        if (quantity <= 0) {
            return removeFromCart(userId, productId);
        }
        
        try {
            Map<Long, Map<String, Object>> userCart = cartStorage.get(userId);
            if (userCart == null) {
                logger.warn("用户购物车不存在 - userId: {}", userId);
                return false;
            }
            
            Map<String, Object> item = userCart.get(productId);
            if (item == null) {
                logger.warn("商品不在购物车中 - userId: {}, productId: {}", userId, productId);
                return false;
            }
            
            if (!mockCheckInventory(productId, quantity)) {
                logger.warn("库存不足 - productId: {}, quantity: {}", productId, quantity);
                return false;
            }
            
            item.put("quantity", quantity);
            item.put("updateTime", System.currentTimeMillis());
            
            logger.info("商品数量更新成功 - userId: {}, productId: {}, 新数量: {}", userId, productId, quantity);
            return true;
            
        } catch (Exception e) {
            logger.error("更新购物车商品数量失败", e);
            return false;
        }
    }
    
    @Override
    public List<Map<String, Object>> getUserCart(Long userId) {
        logger.debug("获取用户购物车 - userId: {}", userId);
        
        try {
            Map<Long, Map<String, Object>> userCart = cartStorage.get(userId);
            if (userCart == null || userCart.isEmpty()) {
                return new ArrayList<>();
            }
            
            List<Map<String, Object>> cartItems = new ArrayList<>();
            for (Map.Entry<Long, Map<String, Object>> entry : userCart.entrySet()) {
                Map<String, Object> item = new HashMap<>(entry.getValue());
                item.put("productId", entry.getKey());
                item.put("productName", "Mock商品_" + entry.getKey());
                item.put("productImage", "/images/product_" + entry.getKey() + ".jpg");
                
                cartItems.add(item);
            }
            
            logger.debug("获取用户购物车成功 - userId: {}, 商品数: {}", userId, cartItems.size());
            return cartItems;
            
        } catch (Exception e) {
            logger.error("获取用户购物车失败 - userId: {}", userId, e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public boolean clearCart(Long userId) {
        logger.info("清空用户购物车 - userId: {}", userId);
        
        try {
            Map<Long, Map<String, Object>> userCart = cartStorage.remove(userId);
            boolean success = userCart != null;
            
            if (success) {
                logger.info("购物车清空成功 - userId: {}, 清理商品数: {}", userId, userCart.size());
            } else {
                logger.warn("用户购物车不存在 - userId: {}", userId);
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("清空用户购物车失败 - userId: {}", userId, e);
            return false;
        }
    }
    
    @Override
    public Map<String, Object> getCartStatistics(Long userId) {
        logger.debug("获取购物车统计信息 - userId: {}", userId);
        
        try {
            Map<Long, Map<String, Object>> userCart = cartStorage.get(userId);
            Map<String, Object> statistics = new HashMap<>();
            
            if (userCart == null || userCart.isEmpty()) {
                statistics.put("totalItems", 0);
                statistics.put("totalQuantity", 0);
                statistics.put("totalPrice", 0L);
                statistics.put("uniqueProducts", 0);
                return statistics;
            }
            
            int totalQuantity = 0;
            long totalPrice = 0L;
            
            for (Map<String, Object> item : userCart.values()) {
                Integer quantity = (Integer) item.get("quantity");
                Long price = (Long) item.get("price");
                
                totalQuantity += quantity;
                totalPrice += price * quantity;
            }
            
            statistics.put("totalItems", userCart.size());
            statistics.put("totalQuantity", totalQuantity);
            statistics.put("totalPrice", totalPrice);
            statistics.put("uniqueProducts", userCart.size());
            
            Long discountAmount = mockCalculateDiscount(userId, totalPrice);
            statistics.put("discountAmount", discountAmount);
            statistics.put("finalPrice", totalPrice - discountAmount);
            
            logger.debug("购物车统计完成 - userId: {}, 商品数: {}, 总价: {}", userId, userCart.size(), totalPrice);
            return statistics;
            
        } catch (Exception e) {
            logger.error("获取购物车统计信息失败 - userId: {}", userId, e);
            return new HashMap<>();
        }
    }
    
    @Override
    public int batchAddToCart(Long userId, List<Map<String, Object>> items) {
        logger.info("批量添加商品到购物车 - userId: {}, 商品数: {}", userId, items.size());
        
        int successCount = 0;
        for (Map<String, Object> item : items) {
            try {
                Long productId = ((Number) item.get("productId")).longValue();
                Integer quantity = ((Number) item.get("quantity")).intValue();
                
                if (addToCart(userId, productId, quantity)) {
                    successCount++;
                }
            } catch (Exception e) {
                logger.error("批量添加单个商品失败", e);
            }
        }
        
        logger.info("批量添加完成 - userId: {}, 成功: {}/{}", userId, successCount, items.size());
        return successCount;
    }
    
    @Override
    public int cleanupExpiredCarts(Integer expireDays) {
        logger.info("清理过期购物车 - expireDays: {}", expireDays);
        
        long expireTime = System.currentTimeMillis() - (expireDays * 24L * 60 * 60 * 1000);
        int cleanedCount = 0;
        
        try {
            Iterator<Map.Entry<Long, Map<Long, Map<String, Object>>>> userIterator = cartStorage.entrySet().iterator();
            
            while (userIterator.hasNext()) {
                Map.Entry<Long, Map<Long, Map<String, Object>>> userEntry = userIterator.next();
                Map<Long, Map<String, Object>> userCart = userEntry.getValue();
                
                Iterator<Map.Entry<Long, Map<String, Object>>> itemIterator = userCart.entrySet().iterator();
                while (itemIterator.hasNext()) {
                    Map.Entry<Long, Map<String, Object>> itemEntry = itemIterator.next();
                    Map<String, Object> item = itemEntry.getValue();
                    
                    Long updateTime = (Long) item.get("updateTime");
                    if (updateTime != null && updateTime < expireTime) {
                        itemIterator.remove();
                        cleanedCount++;
                    }
                }
                
                if (userCart.isEmpty()) {
                    userIterator.remove();
                }
            }
            
            logger.info("清理过期购物车完成 - 清理商品数: {}", cleanedCount);
            return cleanedCount;
            
        } catch (Exception e) {
            logger.error("清理过期购物车失败", e);
            return 0;
        }
    }
    
    private boolean mockCheckInventory(Long productId, Integer quantity) {
        return Math.random() < 0.9;
    }
    
    private Long mockGetProductPrice(Long productId) {
        return 1000L + (long)(Math.random() * 99000);
    }
    
    private Long mockCalculateDiscount(Long userId, Long totalPrice) {
        double discountRate = 0.05 + Math.random() * 0.1;
        return (long)(totalPrice * discountRate);
    }
}
