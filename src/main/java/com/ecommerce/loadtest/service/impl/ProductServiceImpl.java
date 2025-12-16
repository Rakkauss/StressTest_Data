package com.ecommerce.loadtest.service.impl;

import com.ecommerce.loadtest.common.Result;
import com.ecommerce.loadtest.dao.ProductMapper;
import com.ecommerce.loadtest.dto.ProductQueryDTO;
import com.ecommerce.loadtest.entity.Product;
import com.ecommerce.loadtest.service.ProductService;
import com.ecommerce.loadtest.utils.ConfigUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 商品服务实现类
 * 
 * @author rakkaus
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ProductServiceImpl implements ProductService {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);
    
    @Autowired
    private ProductMapper productMapper;
    
    private static final Map<Integer, Integer> PLATFORM_TYPE_MAPPING = new HashMap<>();
    
    static {
        PLATFORM_TYPE_MAPPING.put(2, 1);
        PLATFORM_TYPE_MAPPING.put(3, 10);
    }
    
    @Override
    public int deleteByPrimaryKey(Long productId) {
        logger.info("删除商品 - productId: {}", productId);
        try {
            return productMapper.deleteByPrimaryKey(productId);
        } catch (Exception e) {
            logger.error("删除商品失败 - productId: {}", productId, e);
            throw new RuntimeException("删除商品失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public int insertSelective(Product record) {
        logger.info("插入商品 - productName: {}, businessLineId: {}", 
                   record.getProductName(), record.getBusinessLineId());
        try {
            if (record.getCreateTime() == null) {
                record.setCreateTime(new Date());
            }
            if (record.getStatus() == null) {
                record.setStatus(1);
            }
            return productMapper.insertSelective(record);
        } catch (Exception e) {
            logger.error("插入商品失败 - productName: {}", record.getProductName(), e);
            throw new RuntimeException("插入商品失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Product selectByPrimaryKey(Long productId) {
        logger.debug("查询商品 - productId: {}", productId);
        try {
            return productMapper.selectByPrimaryKey(productId);
        } catch (Exception e) {
            logger.error("查询商品失败 - productId: {}", productId, e);
            throw new RuntimeException("查询商品失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public int updateByPrimaryKeySelective(Product record) {
        logger.info("更新商品 - productId: {}", record.getProductId());
        try {
            record.setUpdateTime(new Date());
            return productMapper.updateByPrimaryKeySelective(record);
        } catch (Exception e) {
            logger.error("更新商品失败 - productId: {}", record.getProductId(), e);
            throw new RuntimeException("更新商品失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Product> selectProductList(ProductQueryDTO queryDTO) {
        logger.info("查询商品列表 - 查询条件: {}", queryDTO);
        try {
            Long categoryId = null;
            if (queryDTO.getCategoryIds() != null && !queryDTO.getCategoryIds().isEmpty()) {
                categoryId = queryDTO.getCategoryIds().get(0).longValue();
            }
            
            Long businessLineId = null;
            if (queryDTO.getBusinessLineIds() != null && !queryDTO.getBusinessLineIds().isEmpty()) {
                businessLineId = queryDTO.getBusinessLineIds().get(0).longValue();
            }
            
            Long minPrice = queryDTO.getMinPrice() != null ? queryDTO.getMinPrice().longValue() : null;
            Long maxPrice = queryDTO.getMaxPrice() != null ? queryDTO.getMaxPrice().longValue() : null;
            
            Integer status = null;
            if (queryDTO.getStatusList() != null && !queryDTO.getStatusList().isEmpty()) {
                status = queryDTO.getStatusList().get(0);
            }
            
            Integer pageNo = null;
            Integer pageSize = queryDTO.getPageSize();
            
            return productMapper.selectProductsByConditions(categoryId, businessLineId, minPrice, maxPrice, 
                                                           status, queryDTO.getKeyword(), pageNo, pageSize);
        } catch (Exception e) {
            logger.error("查询商品列表失败", e);
            throw new RuntimeException("查询商品列表失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Result<List<Long>> queryProductIds(List<Integer> businessLineIds, List<Integer> platformTypes, Integer pageSize) {
        logger.info("查询商品ID列表 - businessLineIds: {}, platformTypes: {}, pageSize: {}", 
                   businessLineIds, platformTypes, pageSize);
        try {
            int maxProductCount = ConfigUtil.getInt("maximum_quantity_of_products", 300);
            int effectivePageSize = pageSize != null ? Math.min(pageSize, maxProductCount) : maxProductCount;
            
            ProductQueryDTO queryDTO = new ProductQueryDTO();
            queryDTO.setBusinessLineIds(businessLineIds);
            queryDTO.setPageSize(effectivePageSize);
            
            if (platformTypes != null && !platformTypes.isEmpty()) {
                queryDTO.setPlatformType(platformTypes.get(0));
            }
            
            List<Product> products = selectProductList(queryDTO);
            List<Long> productIds = products.stream()
                .map(Product::getProductId)
                .collect(Collectors.toList());
            
            logger.info("查询商品ID列表完成 - 共{}个商品", productIds.size());
            return Result.success("查询成功", productIds);
        } catch (Exception e) {
            logger.error("查询商品ID列表失败", e);
            return Result.fail("查询失败");
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Result<List<Product>> queryProductsForLoadTest(ProductQueryDTO queryDTO) {
        logger.info("压测商品查询 - 查询条件: {}", queryDTO);
        try {
            if (queryDTO.getPlatformType() != null) {
                Integer mappedType = PLATFORM_TYPE_MAPPING.get(queryDTO.getPlatformType());
                if (mappedType != null) {
                    if (mappedType == 1) {
                        queryDTO.setBusinessLineIds(Arrays.asList(1));
                    } else if (mappedType == 10) {
                        queryDTO.setBusinessLineIds(Arrays.asList(2));
                    }
                }
            }
            
            if (queryDTO.getPageSize() == null) {
                int defaultPageSize = ConfigUtil.getInt("maximum_quantity_of_products", 300);
                queryDTO.setPageSize(defaultPageSize);
            }
            
            if (queryDTO.getStatusList() == null) {
                queryDTO.setStatusList(Arrays.asList(1));
            }
            
            List<Product> products = selectProductList(queryDTO);
            logger.info("压测商品查询完成 - 共{}个商品", products.size());
            
            return Result.success("查询成功", products);
        } catch (Exception e) {
            logger.error("压测商品查询失败", e);
            return Result.fail("查询失败");
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Product> selectByBusinessLines(List<Integer> businessLineIds) {
        logger.info("按业务线查询商品 - businessLineIds: {}", businessLineIds);
        try {
            if (businessLineIds == null || businessLineIds.isEmpty()) {
                return Collections.emptyList();
            }
            Long businessLineId = businessLineIds.get(0).longValue();
            return productMapper.selectProductsByConditions(null, businessLineId, null, null, null, null, null, null);
        } catch (Exception e) {
            logger.error("按业务线查询商品失败", e);
            throw new RuntimeException("按业务线查询商品失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Product> selectByCategories(List<Integer> categoryIds) {
        logger.info("按分类查询商品 - categoryIds: {}", categoryIds);
        try {
            if (categoryIds == null || categoryIds.isEmpty()) {
                return Collections.emptyList();
            }
            Long categoryId = categoryIds.get(0).longValue();
            return productMapper.selectProductsByConditions(categoryId, null, null, null, null, null, null, null);
        } catch (Exception e) {
            logger.error("按分类查询商品失败", e);
            throw new RuntimeException("按分类查询商品失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Result<List<Product>> searchProducts(String keyword, Integer platformType, Integer pageSize) {
        logger.info("商品搜索 - keyword: {}, platformType: {}, pageSize: {}", keyword, platformType, pageSize);
        try {
            ProductQueryDTO queryDTO = new ProductQueryDTO();
            queryDTO.setKeyword(keyword);
            queryDTO.setPlatformType(platformType);
            queryDTO.setPageSize(pageSize != null ? pageSize : 50);
            queryDTO.setSortField("createTime");
            queryDTO.setSortDirection("desc");
            
            List<Product> products = selectProductList(queryDTO);
            logger.info("商品搜索完成 - 关键词: {}, 结果数: {}", keyword, products.size());
            
            return Result.success("搜索成功", products);
        } catch (Exception e) {
            logger.error("商品搜索失败 - keyword: {}", keyword, e);
            return Result.fail("搜索失败");
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Result<Object> getProductStatistics(Integer platformType) {
        logger.info("获取商品统计信息 - platformType: {}", platformType);
        try {
            List<Product> allProducts = new ArrayList<>();
            
            if (platformType != null) {
                Integer mappedType = PLATFORM_TYPE_MAPPING.get(platformType);
                if (mappedType != null) {
                    int targetBusinessLine = mappedType == 1 ? 1 : 2;
                    Long businessLineId = (long) targetBusinessLine;
                    allProducts = productMapper.selectProductsByConditions(null, businessLineId, null, null, null, null, null, null);
                }
            } else {
                allProducts = productMapper.selectProductsByConditions(null, null, null, null, null, null, null, null);
            }
            
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalCount", allProducts.size());
            statistics.put("normalCount", allProducts.stream().mapToLong(p -> p.getStatus() == 1 ? 1 : 0).sum());
            statistics.put("offlineCount", allProducts.stream().mapToLong(p -> p.getStatus() == 0 ? 1 : 0).sum());
            statistics.put("soldOutCount", allProducts.stream().mapToLong(p -> p.getStatus() == 2 ? 1 : 0).sum());
            statistics.put("totalStock", allProducts.stream().mapToLong(p -> p.getStockCount() != null ? p.getStockCount() : 0).sum());
            
            Map<String, Long> categoryStats = allProducts.stream()
                .collect(Collectors.groupingBy(
                    p -> p.getCategoryName() != null ? p.getCategoryName() : "未分类",
                    Collectors.counting()
                ));
            statistics.put("categoryStats", categoryStats);
            
            return Result.success("查询成功", statistics);
        } catch (Exception e) {
            logger.error("获取商品统计信息失败", e);
            return Result.fail("查询失败");
        }
    }
    
    @Override
    public int batchUpdateStatus(List<Long> productIds, Integer status) {
        logger.info("批量更新商品状态 - productIds: {}, status: {}", productIds, status);
        try {
            int updateCount = 0;
            for (Long productId : productIds) {
                Product product = productMapper.selectByPrimaryKey(productId);
                if (product != null) {
                    product.setStatus(status);
                    product.setUpdateTime(new Date());
                    productMapper.updateByPrimaryKeySelective(product);
                    updateCount++;
                }
            }
            logger.info("批量更新商品状态完成 - 更新{}个商品", updateCount);
            return updateCount;
        } catch (Exception e) {
            logger.error("批量更新商品状态失败", e);
            throw new RuntimeException("批量更新商品状态失败: " + e.getMessage(), e);
        }
    }
}
