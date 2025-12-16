package com.ecommerce.loadtest.dao;

import com.ecommerce.loadtest.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品数据访问接口
 * 
 * @author rakkaus
 */
@Mapper
public interface ProductMapper {
    
    int deleteByPrimaryKey(@Param("productId") Long productId);
    
    int insertSelective(Product record);
    
    Product selectByPrimaryKey(@Param("productId") Long productId);
    
    int updateByPrimaryKeySelective(Product record);
    
    List<Product> selectProductsByConditions(@Param("categoryId") Long categoryId,
                                           @Param("businessLineId") Long businessLineId,
                                           @Param("minPrice") Long minPrice,
                                           @Param("maxPrice") Long maxPrice,
                                           @Param("status") Integer status,
                                           @Param("keyword") String keyword,
                                           @Param("pageNo") Integer pageNo,
                                           @Param("pageSize") Integer pageSize);
    
    List<Product> selectProductsByIds(@Param("productIds") List<Long> productIds);
}
