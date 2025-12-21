package com.ecommerce.loadtest.service.impl;

import com.ecommerce.loadtest.dao.ProductMapper;
import com.ecommerce.loadtest.entity.Product;
import com.ecommerce.loadtest.service.ProductFilterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 产品筛选服务实现类
 * 
 * @author rakkaus
 */
@Service
@Transactional(readOnly = true)
public class ProductFilterServiceImpl implements ProductFilterService {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductFilterServiceImpl.class);
    
    @Autowired
    private ProductMapper productMapper;
    
    @Override
    public List<Product> filterProducts(Long categoryId, Long businessLineId, Long minPrice, Long maxPrice, 
                                       Integer status, String keyword, Integer pageNo, Integer pageSize) {
        logger.info("筛选产品 - categoryId: {}, businessLineId: {}, priceRange: [{}, {}], status: {}, keyword: {}", 
                   categoryId, businessLineId, minPrice, maxPrice, status, keyword);
        
        try {
            return productMapper.selectProductsByConditions(categoryId, businessLineId, minPrice, maxPrice, 
                                                           status, keyword, pageNo, pageSize);
        } catch (Exception e) {
            logger.error("筛选产品失败", e);
            throw new RuntimeException("筛选产品失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<Product> searchProducts(String keyword, Long categoryId, Integer limit) {
        logger.info("搜索产品 - keyword: {}, categoryId: {}, limit: {}", keyword, categoryId, limit);
        
        if (keyword == null || keyword.trim().isEmpty()) {
            logger.warn("搜索关键词为空");
            return new ArrayList<>();
        }
        
        try {
            List<Long> searchResultIds = mockSearchEngine(keyword, categoryId, limit);
            
            if (searchResultIds.isEmpty()) {
                return new ArrayList<>();
            }
            
            return productMapper.selectProductsByIds(searchResultIds);
            
        } catch (Exception e) {
            logger.error("搜索产品失败 - keyword: {}", keyword, e);
            throw new RuntimeException("搜索产品失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<Product> getPopularProducts(Long businessLineId, Integer limit) {
        logger.info("获取热门产品 - businessLineId: {}, limit: {}", businessLineId, limit);
        
        try {
            return productMapper.selectPopularProducts(businessLineId, limit);
        } catch (Exception e) {
            logger.error("获取热门产品失败", e);
            throw new RuntimeException("获取热门产品失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<Product> getRecommendedProducts(Long userId, Long categoryId, Integer limit) {
        logger.info("获取推荐产品 - userId: {}, categoryId: {}, limit: {}", userId, categoryId, limit);
        
        try {
            List<Long> recommendedIds = mockRecommendationEngine(userId, categoryId, limit);
            
            if (recommendedIds.isEmpty()) {
                return getPopularProducts(null, limit);
            }
            
            return productMapper.selectProductsByIds(recommendedIds);
            
        } catch (Exception e) {
            logger.error("获取推荐产品失败 - userId: {}", userId, e);
            throw new RuntimeException("获取推荐产品失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<Product> filterProductsByPrice(Long minPrice, Long maxPrice, Long businessLineId, Integer limit) {
        logger.info("按价格筛选产品 - priceRange: [{}, {}], businessLineId: {}, limit: {}", 
                   minPrice, maxPrice, businessLineId, limit);
        
        try {
            return productMapper.selectProductsByPriceRange(minPrice, maxPrice, businessLineId, limit);
        } catch (Exception e) {
            logger.error("按价格筛选产品失败", e);
            throw new RuntimeException("按价格筛选产品失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<Product> getProductsByCategory(Long categoryId, Integer status, Integer limit) {
        logger.info("按分类获取产品 - categoryId: {}, status: {}, limit: {}", categoryId, status, limit);
        
        try {
            return productMapper.selectProductsByCategory(categoryId, status, limit);
        } catch (Exception e) {
            logger.error("按分类获取产品失败 - categoryId: {}", categoryId, e);
            throw new RuntimeException("按分类获取产品失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<Product> getProductsByBusinessLine(Long businessLineId, Integer status, Integer limit) {
        logger.info("按业务线获取产品 - businessLineId: {}, status: {}, limit: {}", businessLineId, status, limit);
        
        try {
            return productMapper.selectProductsByBusinessLine(businessLineId, status, limit);
        } catch (Exception e) {
            logger.error("按业务线获取产品失败 - businessLineId: {}", businessLineId, e);
            throw new RuntimeException("按业务线获取产品失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public Map<String, Object> getProductStatistics(Long businessLineId) {
        logger.info("获取产品统计信息 - businessLineId: {}", businessLineId);
        
        try {
            return productMapper.selectProductStatistics(businessLineId);
        } catch (Exception e) {
            logger.error("获取产品统计信息失败", e);
            throw new RuntimeException("获取产品统计信息失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<Map<String, Object>> getCategoryStatistics() {
        logger.info("获取分类统计信息");
        
        try {
            return productMapper.selectCategoryStatistics();
        } catch (Exception e) {
            logger.error("获取分类统计信息失败", e);
            throw new RuntimeException("获取分类统计信息失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<Map<String, Object>> getPriceDistribution(Long businessLineId) {
        logger.info("获取价格分布统计 - businessLineId: {}", businessLineId);
        
        try {
            return productMapper.selectPriceDistribution(businessLineId);
        } catch (Exception e) {
            logger.error("获取价格分布统计失败", e);
            throw new RuntimeException("获取价格分布统计失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<Long> batchFilterProductIds(List<Long> categoryIds, List<Long> businessLineIds, 
                                           Integer status, Integer limit) {
        logger.info("批量筛选产品ID - categoryIds: {}, businessLineIds: {}, status: {}, limit: {}", 
                   categoryIds, businessLineIds, status, limit);
        
        if ((categoryIds == null || categoryIds.isEmpty()) && 
            (businessLineIds == null || businessLineIds.isEmpty())) {
            logger.warn("批量筛选条件为空");
            return new ArrayList<>();
        }
        
        try {
            return productMapper.selectProductIdsByBatch(categoryIds, businessLineIds, status, limit);
        } catch (Exception e) {
            logger.error("批量筛选产品ID失败", e);
            throw new RuntimeException("批量筛选产品ID失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<Product> getSimilarProducts(Long productId, Integer limit) {
        logger.info("获取相似产品 - productId: {}, limit: {}", productId, limit);
        
        try {
            Product targetProduct = productMapper.selectByPrimaryKey(productId);
            if (targetProduct == null) {
                logger.warn("目标产品不存在 - productId: {}", productId);
                return new ArrayList<>();
            }
            
            List<Long> similarIds = mockSimilarityEngine(targetProduct, limit);
            
            if (similarIds.isEmpty()) {
                return getProductsByCategory(targetProduct.getCategoryId().longValue(), Integer.valueOf(1), limit);
            }
            
            return productMapper.selectProductsByIds(similarIds);
            
        } catch (Exception e) {
            logger.error("获取相似产品失败 - productId: {}", productId, e);
            throw new RuntimeException("获取相似产品失败: " + e.getMessage(), e);
        }
    }
    
    private List<Long> mockSearchEngine(String keyword, Long categoryId, Integer limit) {
        logger.debug("Mock搜索引擎 - keyword: {}, categoryId: {}", keyword, categoryId);
        
        try {
            Thread.sleep(20 + (int)(Math.random() * 80));
            
            List<Long> searchResults = new ArrayList<>();
            int resultCount = Math.min(limit, keyword.length() > 2 ? 10 + (int)(Math.random() * 20) : 5);
            
            for (int i = 0; i < resultCount; i++) {
                Long productId = 1000L + (long)(Math.random() * 9000);
                if (categoryId != null) {
                    productId = categoryId * 1000 + (long)(Math.random() * 1000);
                }
                searchResults.add(productId);
            }
            
            logger.debug("Mock搜索引擎返回{}个结果", searchResults.size());
            return searchResults;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Mock搜索引擎被中断");
            return new ArrayList<>();
        }
    }
    
    private List<Long> mockRecommendationEngine(Long userId, Long categoryId, Integer limit) {
        logger.debug("Mock推荐算法 - userId: {}, categoryId: {}", userId, categoryId);
        
        try {
            Thread.sleep(50 + (int)(Math.random() * 150));
            
            List<Long> recommendations = new ArrayList<>();
            int userPreference = (int)(userId % 5);
            
            for (int i = 0; i < limit; i++) {
                Long productId = 2000L + userPreference * 1000 + (long)(Math.random() * 1000);
                if (categoryId != null) {
                    productId = categoryId * 1000 + userPreference * 100 + (long)(Math.random() * 100);
                }
                recommendations.add(productId);
            }
            
            logger.debug("Mock推荐算法返回{}个推荐", recommendations.size());
            return recommendations;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Mock推荐算法被中断");
            return new ArrayList<>();
        }
    }
    
    private List<Long> mockSimilarityEngine(Product targetProduct, Integer limit) {
        logger.debug("Mock相似度计算 - targetProductId: {}", targetProduct.getProductId());
        
        try {
            Thread.sleep(30 + (int)(Math.random() * 90));
            
            List<Long> similarProducts = new ArrayList<>();
            Long categoryBase = targetProduct.getCategoryId() * 1000L;
            Long priceRange = targetProduct.getPrice() != null ? 
                targetProduct.getPrice().divide(new java.math.BigDecimal(1000)).longValue() : 0L;
            
            for (int i = 0; i < limit; i++) {
                Long similarId = categoryBase + priceRange * 100 + (long)(Math.random() * 100);
                
                if (!similarId.equals(targetProduct.getProductId())) {
                    similarProducts.add(similarId);
                }
            }
            
            logger.debug("Mock相似度计算返回{}个相似产品", similarProducts.size());
            return similarProducts;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Mock相似度计算被中断");
            return new ArrayList<>();
        }
    }
}
