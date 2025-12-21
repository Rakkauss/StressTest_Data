package com.ecommerce.loadtest.service;

import com.ecommerce.loadtest.entity.Product;

import java.util.List;
import java.util.Map;

/**
 * 产品筛选服务接口
 * 
 * @author rakkaus
 */
public interface ProductFilterService {
    
    /**
     * 根据条件筛选产品
     */
    List<Product> filterProducts(Long categoryId, Long businessLineId, Long minPrice, Long maxPrice, 
                                Integer status, String keyword, Integer pageNo, Integer pageSize);
    
    /**
     * 按关键词搜索产品
     */
    List<Product> searchProducts(String keyword, Long categoryId, Integer limit);
    
    /**
     * 获取热门产品
     */
    List<Product> getPopularProducts(Long businessLineId, Integer limit);
    
    /**
     * 获取推荐产品
     */
    List<Product> getRecommendedProducts(Long userId, Long categoryId, Integer limit);
    
    /**
     * 按价格区间筛选产品
     */
    List<Product> filterProductsByPrice(Long minPrice, Long maxPrice, Long businessLineId, Integer limit);
    
    /**
     * 按分类获取产品
     */
    List<Product> getProductsByCategory(Long categoryId, Integer status, Integer limit);
    
    /**
     * 按业务线获取产品
     */
    List<Product> getProductsByBusinessLine(Long businessLineId, Integer status, Integer limit);
    
    /**
     * 获取产品统计信息
     */
    Map<String, Object> getProductStatistics(Long businessLineId);
    
    /**
     * 获取分类统计信息
     */
    List<Map<String, Object>> getCategoryStatistics();
    
    /**
     * 获取价格分布统计
     */
    List<Map<String, Object>> getPriceDistribution(Long businessLineId);
    
    /**
     * 批量筛选产品ID
     */
    List<Long> batchFilterProductIds(List<Long> categoryIds, List<Long> businessLineIds, 
                                    Integer status, Integer limit);
    
    /**
     * 获取相似产品
     */
    List<Product> getSimilarProducts(Long productId, Integer limit);
}
