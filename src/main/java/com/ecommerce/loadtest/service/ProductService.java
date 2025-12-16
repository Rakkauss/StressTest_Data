package com.ecommerce.loadtest.service;

import com.ecommerce.loadtest.common.Result;
import com.ecommerce.loadtest.dto.ProductQueryDTO;
import com.ecommerce.loadtest.entity.Product;

import java.util.List;

/**
 * 商品服务接口
 * 
 * @author rakkaus
 */
public interface ProductService {
    
    int deleteByPrimaryKey(Long productId);
    
    int insertSelective(Product record);
    
    Product selectByPrimaryKey(Long productId);
    
    int updateByPrimaryKeySelective(Product record);
    
    List<Product> selectProductList(ProductQueryDTO queryDTO);
    
    Result<List<Long>> queryProductIds(List<Integer> businessLineIds, List<Integer> platformTypes, Integer pageSize);
    
    Result<List<Product>> queryProductsForLoadTest(ProductQueryDTO queryDTO);
    
    List<Product> selectByBusinessLines(List<Integer> businessLineIds);
    
    List<Product> selectByCategories(List<Integer> categoryIds);
    
    Result<List<Product>> searchProducts(String keyword, Integer platformType, Integer pageSize);
    
    Result<Object> getProductStatistics(Integer platformType);
    
    int batchUpdateStatus(List<Long> productIds, Integer status);
}
