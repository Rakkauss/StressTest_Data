package com.ecommerce.loadtest.service.impl;

import com.ecommerce.loadtest.service.OrderService;
import com.ecommerce.loadtest.utils.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 订单服务实现类（基于Redis缓存）
 * 
 * @author rakkaus
 */
@Service
public class OrderServiceImpl implements OrderService {
    
    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);
    
    @Autowired
    private RedisUtil redisUtil;
    
    private static final String ORDER_KEY_PREFIX = "stress:order:";
    private static final String USER_ORDER_INDEX_PREFIX = "stress:user:orders:";
    private static final String ORDER_ID_GENERATOR_KEY = "stress:order:id:generator";
    
    @Override
    public Long createOrder(Long userId, List<Map<String, Object>> cartItems) {
        logger.info("创建订单 - userId: {}, 商品数: {}", userId, cartItems.size());
        
        if (cartItems == null || cartItems.isEmpty()) {
            logger.warn("购物车为空 - userId: {}", userId);
            return null;
        }
        
        try {
            Long orderId = redisUtil.incr(ORDER_ID_GENERATOR_KEY, 1);
            if (orderId == 1) {
                orderId = 100000L;
                redisUtil.set(ORDER_ID_GENERATOR_KEY, orderId);
            }
            
            long totalPrice = 0L;
            for (Map<String, Object> item : cartItems) {
                Long price = ((Number) item.get("price")).longValue();
                Integer quantity = ((Number) item.get("quantity")).intValue();
                totalPrice += price * quantity;
            }
            
            if (!mockCheckInventoryForOrder(cartItems)) {
                logger.warn("库存不足 - userId: {}", userId);
                return null;
            }
            
            Map<String, Object> orderInfo = new HashMap<>();
            orderInfo.put("orderId", orderId);
            orderInfo.put("userId", userId);
            orderInfo.put("items", new ArrayList<>(cartItems));
            orderInfo.put("totalPrice", totalPrice);
            orderInfo.put("status", 1);
            orderInfo.put("createTime", System.currentTimeMillis());
            orderInfo.put("updateTime", System.currentTimeMillis());
            orderInfo.put("shippingAddress", mockGetShippingAddress(userId));
            
            redisUtil.hmset(ORDER_KEY_PREFIX + orderId, orderInfo, 86400);
            
            String userOrderKey = USER_ORDER_INDEX_PREFIX + userId;
            redisUtil.sSet(userOrderKey, orderId);
            redisUtil.expire(userOrderKey, 86400);
            
            logger.info("订单创建成功 - orderId: {}, totalPrice: {}", orderId, totalPrice);
            return orderId;
            
        } catch (Exception e) {
            logger.error("创建订单失败 - userId: {}", userId, e);
            return null;
        }
    }
    
    @Override
    public boolean cancelOrder(Long orderId, Long userId, String reason) {
        logger.info("取消订单 - orderId: {}, userId: {}, reason: {}", orderId, userId, reason);
        
        try {
            Map<Object, Object> orderInfoMap = redisUtil.hmget(ORDER_KEY_PREFIX + orderId);
            if (orderInfoMap == null || orderInfoMap.isEmpty()) {
                logger.warn("订单不存在 - orderId: {}", orderId);
                return false;
            }
            
            Map<String, Object> orderInfo = new HashMap<>();
            orderInfoMap.forEach((k, v) -> orderInfo.put(k.toString(), v));
            
            Long orderUserId = ((Number) orderInfo.get("userId")).longValue();
            if (!userId.equals(orderUserId)) {
                logger.warn("无权限取消订单 - orderId: {}, userId: {}", orderId, userId);
                return false;
            }
            
            Integer currentStatus = (Integer) orderInfo.get("status");
            if (currentStatus != 1) {
                logger.warn("订单状态不允许取消 - orderId: {}, status: {}", orderId, currentStatus);
                return false;
            }
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) orderInfo.get("items");
            mockRestoreInventory(items);
            
            redisUtil.hset(ORDER_KEY_PREFIX + orderId, "status", 3);
            redisUtil.hset(ORDER_KEY_PREFIX + orderId, "cancelReason", reason);
            redisUtil.hset(ORDER_KEY_PREFIX + orderId, "cancelTime", System.currentTimeMillis());
            redisUtil.hset(ORDER_KEY_PREFIX + orderId, "updateTime", System.currentTimeMillis());
            
            mockSendOrderNotification(userId, orderId, "ORDER_CANCELLED");
            
            logger.info("订单取消成功 - orderId: {}", orderId);
            return true;
            
        } catch (Exception e) {
            logger.error("取消订单失败 - orderId: {}", orderId, e);
            return false;
        }
    }
    
    @Override
    public boolean payOrder(Long orderId, Long userId, String paymentMethod) {
        logger.info("支付订单 - orderId: {}, userId: {}, paymentMethod: {}", orderId, userId, paymentMethod);
        
        try {
            Map<Object, Object> orderInfoMap = redisUtil.hmget(ORDER_KEY_PREFIX + orderId);
            if (orderInfoMap == null || orderInfoMap.isEmpty()) {
                logger.warn("订单不存在 - orderId: {}", orderId);
                return false;
            }
            
            Map<String, Object> orderInfo = new HashMap<>();
            orderInfoMap.forEach((k, v) -> orderInfo.put(k.toString(), v));
            
            Long orderUserId = ((Number) orderInfo.get("userId")).longValue();
            if (!userId.equals(orderUserId)) {
                logger.warn("无权限支付订单 - orderId: {}, userId: {}", orderId, userId);
                return false;
            }
            
            Integer currentStatus = (Integer) orderInfo.get("status");
            if (currentStatus != 1) {
                logger.warn("订单状态不允许支付 - orderId: {}, status: {}", orderId, currentStatus);
                return false;
            }
            
            Long totalPrice = ((Number) orderInfo.get("totalPrice")).longValue();
            String paymentResult = mockProcessPayment(userId, orderId, totalPrice, paymentMethod);
            
            if (!"SUCCESS".equals(paymentResult)) {
                logger.warn("支付失败 - orderId: {}, result: {}", orderId, paymentResult);
                return false;
            }
            
            redisUtil.hset(ORDER_KEY_PREFIX + orderId, "status", 2);
            redisUtil.hset(ORDER_KEY_PREFIX + orderId, "paymentMethod", paymentMethod);
            redisUtil.hset(ORDER_KEY_PREFIX + orderId, "paymentTime", System.currentTimeMillis());
            redisUtil.hset(ORDER_KEY_PREFIX + orderId, "updateTime", System.currentTimeMillis());
            
            mockSendOrderNotification(userId, orderId, "ORDER_PAID");
            
            logger.info("订单支付成功 - orderId: {}", orderId);
            return true;
            
        } catch (Exception e) {
            logger.error("支付订单失败 - orderId: {}", orderId, e);
            return false;
        }
    }
    
    @Override
    public List<Map<String, Object>> getUserOrders(Long userId, Integer status, Integer limit) {
        logger.debug("获取用户订单列表 - userId: {}, status: {}, limit: {}", userId, status, limit);
        
        try {
            Set<Object> userOrderIds = redisUtil.sGet(USER_ORDER_INDEX_PREFIX + userId);
            if (userOrderIds == null || userOrderIds.isEmpty()) {
                return new ArrayList<>();
            }
            
            List<Map<String, Object>> orders = new ArrayList<>();
            for (Object orderIdObj : userOrderIds) {
                Long orderId = Long.valueOf(orderIdObj.toString());
                Map<Object, Object> orderInfoMap = redisUtil.hmget(ORDER_KEY_PREFIX + orderId);
                
                if (orderInfoMap != null && !orderInfoMap.isEmpty()) {
                    Map<String, Object> orderInfo = new HashMap<>();
                    orderInfoMap.forEach((k, v) -> orderInfo.put(k.toString(), v));
                    
                    if (status != null && !status.equals(orderInfo.get("status"))) {
                        continue;
                    }
                    
                    Map<String, Object> orderSummary = new HashMap<>();
                    orderSummary.put("orderId", orderInfo.get("orderId"));
                    orderSummary.put("totalPrice", orderInfo.get("totalPrice"));
                    orderSummary.put("status", orderInfo.get("status"));
                    orderSummary.put("createTime", orderInfo.get("createTime"));
                    orderSummary.put("updateTime", orderInfo.get("updateTime"));
                    
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> items = (List<Map<String, Object>>) orderInfo.get("items");
                    orderSummary.put("itemCount", items != null ? items.size() : 0);
                    
                    orders.add(orderSummary);
                    
                    if (limit != null && orders.size() >= limit) {
                        break;
                    }
                }
            }
            
            orders.sort((o1, o2) -> {
                Long time1 = (Long) o1.get("createTime");
                Long time2 = (Long) o2.get("createTime");
                return time2.compareTo(time1);
            });
            
            logger.debug("获取用户订单列表成功 - userId: {}, 订单数: {}", userId, orders.size());
            return orders;
            
        } catch (Exception e) {
            logger.error("获取用户订单列表失败 - userId: {}", userId, e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public Map<String, Object> getOrderDetail(Long orderId, Long userId) {
        logger.debug("获取订单详情 - orderId: {}, userId: {}", orderId, userId);
        
        try {
            Map<Object, Object> orderInfoMap = redisUtil.hmget(ORDER_KEY_PREFIX + orderId);
            if (orderInfoMap == null || orderInfoMap.isEmpty()) {
                logger.warn("订单不存在 - orderId: {}", orderId);
                return null;
            }
            
            Map<String, Object> orderInfo = new HashMap<>();
            orderInfoMap.forEach((k, v) -> orderInfo.put(k.toString(), v));
            
            Long orderUserId = ((Number) orderInfo.get("userId")).longValue();
            if (!userId.equals(orderUserId)) {
                logger.warn("无权限查看订单 - orderId: {}, userId: {}", orderId, userId);
                return null;
            }
            
            Map<String, Object> orderDetail = new HashMap<>(orderInfo);
            Integer statusValue = (Integer) orderInfo.get("status");
            orderDetail.put("statusText", getStatusText(statusValue));
            
            logger.debug("获取订单详情成功 - orderId: {}", orderId);
            return orderDetail;
            
        } catch (Exception e) {
            logger.error("获取订单详情失败 - orderId: {}", orderId, e);
            return null;
        }
    }
    
    @Override
    public int batchCreateOrders(List<Map<String, Object>> orderRequests) {
        logger.info("批量创建订单 - 请求数量: {}", orderRequests.size());
        
        int successCount = 0;
        for (Map<String, Object> request : orderRequests) {
            try {
                Long userId = ((Number) request.get("userId")).longValue();
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> cartItems = (List<Map<String, Object>>) request.get("cartItems");
                
                Long orderId = createOrder(userId, cartItems);
                if (orderId != null) {
                    successCount++;
                }
            } catch (Exception e) {
                logger.error("批量创建单个订单失败", e);
            }
        }
        
        logger.info("批量创建订单完成 - 成功: {}/{}", successCount, orderRequests.size());
        return successCount;
    }
    
    @Override
    public int batchCancelOrders(List<Long> orderIds, String reason) {
        logger.info("批量取消订单 - 订单数量: {}, reason: {}", orderIds.size(), reason);
        
        int successCount = 0;
        for (Long orderId : orderIds) {
            try {
                Map<Object, Object> orderInfoMap = redisUtil.hmget(ORDER_KEY_PREFIX + orderId);
                if (orderInfoMap != null && !orderInfoMap.isEmpty()) {
                    Map<String, Object> orderInfo = new HashMap<>();
                    orderInfoMap.forEach((k, v) -> orderInfo.put(k.toString(), v));
                    
                    Long userId = ((Number) orderInfo.get("userId")).longValue();
                    if (cancelOrder(orderId, userId, reason)) {
                        successCount++;
                    }
                }
            } catch (Exception e) {
                logger.error("批量取消单个订单失败 - orderId: {}", orderId, e);
            }
        }
        
        logger.info("批量取消订单完成 - 成功: {}/{}", successCount, orderIds.size());
        return successCount;
    }
    
    @Override
    public Map<String, Object> getOrderStatistics(Long userId) {
        logger.debug("获取订单统计信息 - userId: {}", userId);
        
        try {
            Map<String, Object> statistics = new HashMap<>();
            
            if (userId != null) {
                Set<Object> userOrderIds = redisUtil.sGet(USER_ORDER_INDEX_PREFIX + userId);
                if (userOrderIds == null) {
                    userOrderIds = new HashSet<>();
                }
                
                int totalOrders = userOrderIds.size();
                int paidOrders = 0;
                int cancelledOrders = 0;
                long totalAmount = 0L;
                
                for (Object orderIdObj : userOrderIds) {
                    Long orderId = Long.valueOf(orderIdObj.toString());
                    Map<Object, Object> orderInfoMap = redisUtil.hmget(ORDER_KEY_PREFIX + orderId);
                    
                    if (orderInfoMap != null && !orderInfoMap.isEmpty()) {
                        Integer statusValue = (Integer) orderInfoMap.get("status");
                        Long totalPrice = ((Number) orderInfoMap.get("totalPrice")).longValue();
                        
                        if (statusValue == 2) paidOrders++;
                        if (statusValue == 3) cancelledOrders++;
                        if (statusValue == 2) totalAmount += totalPrice;
                    }
                }
                
                statistics.put("totalOrders", totalOrders);
                statistics.put("paidOrders", paidOrders);
                statistics.put("cancelledOrders", cancelledOrders);
                statistics.put("pendingOrders", totalOrders - paidOrders - cancelledOrders);
                statistics.put("totalAmount", totalAmount);
                
            } else {
                statistics.put("message", "全局统计暂不支持，请指定userId");
            }
            
            return statistics;
            
        } catch (Exception e) {
            logger.error("获取订单统计信息失败", e);
            return new HashMap<>();
        }
    }
    
    @Override
    public int cleanupTestOrders(Integer days) {
        logger.info("清理测试订单 - days: {}", days);
        logger.warn("Redis模式下，订单会自动过期（TTL=24小时），无需手动清理");
        return 0;
    }
    
    private String getStatusText(Integer status) {
        switch (status) {
            case 1: return "待支付";
            case 2: return "已支付";
            case 3: return "已取消";
            default: return "未知状态";
        }
    }
    
    private boolean mockCheckInventoryForOrder(List<Map<String, Object>> cartItems) {
        return Math.random() < 0.95;
    }
    
    private Map<String, Object> mockGetShippingAddress(Long userId) {
        Map<String, Object> address = new HashMap<>();
        address.put("addressId", 10000L + userId % 1000);
        address.put("receiverName", "测试用户_" + userId);
        address.put("receiverPhone", "138****" + String.format("%04d", userId % 10000));
        address.put("province", "测试省");
        address.put("city", "测试市");
        address.put("district", "测试区");
        address.put("detail", "测试街道" + (userId % 100) + "号");
        return address;
    }
    
    private void mockRestoreInventory(List<Map<String, Object>> items) {
        logger.debug("Mock库存回滚 - 商品数: {}", items.size());
    }
    
    private String mockProcessPayment(Long userId, Long orderId, Long amount, String paymentMethod) {
        try {
            Thread.sleep(100 + (int)(Math.random() * 200));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return Math.random() < 0.95 ? "SUCCESS" : "FAILED";
    }
    
    private void mockSendOrderNotification(Long userId, Long orderId, String eventType) {
        logger.debug("Mock发送订单通知 - userId: {}, orderId: {}, eventType: {}", userId, orderId, eventType);
    }
}
