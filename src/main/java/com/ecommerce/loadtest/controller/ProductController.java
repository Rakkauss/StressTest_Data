package com.ecommerce.loadtest.controller;

import com.ecommerce.loadtest.common.Result;
import com.ecommerce.loadtest.dto.ProductQueryDTO;
import com.ecommerce.loadtest.entity.Product;
import com.ecommerce.loadtest.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 商品控制器
 * 管理压测商品
 * 
 * @author rakkaus
 */
@RestController
@RequestMapping("/product")
public class ProductController {
    
    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);
    
    @Autowired
    private ProductService productService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @GetMapping("/infoidInquiryInterface")
    public Result<List<Long>> queryProductIdsForLoadTest(@RequestParam(name = "ptType") Integer platformType,
                                                        @RequestParam(name = "businessLineid", required = false) String businessLineIds,
                                                        @RequestParam(name = "pageSize", required = false) Integer pageSize) {
        try {
            logger.info("压测商品准备开始 - platformType: {}, businessLineIds: {}, pageSize: {}", 
                       platformType, businessLineIds, pageSize);
            
            List<Integer> businessLines = new ArrayList<>();
            if (StringUtils.hasText(businessLineIds)) {
                String[] numberStrings = businessLineIds.split(",");
                for (String number : numberStrings) {
                    try {
                        businessLines.add(Integer.parseInt(number.trim()));
                    } catch (NumberFormatException e) {
                        logger.warn("业务线ID格式错误: {}", number);
                    }
                }
            }
            
            Integer mappedPlatformType = platformType;
            if (platformType != null) {
                if (platformType.equals(2)) {
                    mappedPlatformType = 1;
                    logger.info("平台类型映射 - 平台A: {} -> {}", platformType, mappedPlatformType);
                } else if (platformType.equals(3)) {
                    mappedPlatformType = 10;
                    logger.info("平台类型映射 - 平台B: {} -> {}", platformType, mappedPlatformType);
                }
            }
            
            Result<List<Long>> result = productService.queryProductIds(
                businessLines, 
                Arrays.asList(mappedPlatformType), 
                pageSize
            );
            
            logger.info("压测商品准备完成 - 结果: {}", result.getData() != null ? result.getData().size() : 0);
            return result;
        } catch (Exception e) {
            logger.error("压测商品准备失败", e);
            return Result.fail("查询失败");
        }
    }
    
    @PostMapping("/queryProductList")
    public Result<List<Product>> queryProductList(@RequestBody ProductQueryDTO queryDTO) {
        try {
            logger.info("查询商品列表 - 查询条件: {}", objectMapper.writeValueAsString(queryDTO));
            
            if (!queryDTO.hasValidParams()) {
                return Result.fail("查询参数不能全部为空");
            }
            
            Result<List<Product>> result = productService.queryProductsForLoadTest(queryDTO);
            logger.info("查询商品列表完成 - 结果数: {}", 
                       result.getData() != null ? result.getData().size() : 0);
            
            return result;
        } catch (Exception e) {
            logger.error("查询商品列表失败", e);
            return Result.fail("查询失败");
        }
    }
    
    @GetMapping("/search")
    public Result<List<Product>> searchProducts(@RequestParam(name = "keyword") String keyword,
                                               @RequestParam(name = "platformType", required = false) Integer platformType,
                                               @RequestParam(name = "pageSize", defaultValue = "20") Integer pageSize) {
        try {
            logger.info("商品搜索 - keyword: {}, platformType: {}, pageSize: {}", 
                       keyword, platformType, pageSize);
            
            if (!StringUtils.hasText(keyword)) {
                return Result.fail("搜索关键词不能为空");
            }
            
            Result<List<Product>> result = productService.searchProducts(keyword, platformType, pageSize);
            logger.info("商品搜索完成 - 关键词: {}, 结果数: {}", 
                       keyword, result.getData() != null ? result.getData().size() : 0);
            
            return result;
        } catch (Exception e) {
            logger.error("商品搜索失败 - keyword: {}", keyword, e);
            return Result.fail("搜索失败");
        }
    }
    
    @GetMapping("/queryByBusinessLine")
    public Result<List<Product>> queryProductsByBusinessLine(@RequestParam(name = "businessLineIds") String businessLineIds) {
        try {
            logger.info("按业务线查询商品 - businessLineIds: {}", businessLineIds);
            
            if (!StringUtils.hasText(businessLineIds)) {
                return Result.fail("业务线ID不能为空");
            }
            
            List<Integer> businessLines = new ArrayList<>();
            String[] numberStrings = businessLineIds.split(",");
            for (String number : numberStrings) {
                try {
                    businessLines.add(Integer.parseInt(number.trim()));
                } catch (NumberFormatException e) {
                    logger.warn("业务线ID格式错误: {}", number);
                }
            }
            
            if (businessLines.isEmpty()) {
                return Result.fail("有效的业务线ID不能为空");
            }
            
            List<Product> products = productService.selectByBusinessLines(businessLines);
            logger.info("按业务线查询商品完成 - 结果数: {}", products.size());
            
            return Result.success("查询成功", products);
        } catch (Exception e) {
            logger.error("按业务线查询商品失败", e);
            return Result.fail("查询失败");
        }
    }
    
    @GetMapping("/queryByCategory")
    public Result<List<Product>> queryProductsByCategory(@RequestParam(name = "categoryIds") String categoryIds) {
        try {
            logger.info("按分类查询商品 - categoryIds: {}", categoryIds);
            
            if (!StringUtils.hasText(categoryIds)) {
                return Result.fail("分类ID不能为空");
            }
            
            List<Integer> categories = new ArrayList<>();
            String[] numberStrings = categoryIds.split(",");
            for (String number : numberStrings) {
                try {
                    categories.add(Integer.parseInt(number.trim()));
                } catch (NumberFormatException e) {
                    logger.warn("分类ID格式错误: {}", number);
                }
            }
            
            if (categories.isEmpty()) {
                return Result.fail("有效的分类ID不能为空");
            }
            
            List<Product> products = productService.selectByCategories(categories);
            logger.info("按分类查询商品完成 - 结果数: {}", products.size());
            
            return Result.success("查询成功", products);
        } catch (Exception e) {
            logger.error("按分类查询商品失败", e);
            return Result.fail("查询失败");
        }
    }
    
    @GetMapping("/statistics")
    public Result<Object> getProductStatistics(@RequestParam(name = "platformType", required = false) Integer platformType) {
        try {
            logger.info("获取商品统计信息 - platformType: {}", platformType);
            
            Result<Object> result = productService.getProductStatistics(platformType);
            logger.info("获取商品统计信息完成");
            
            return result;
        } catch (Exception e) {
            logger.error("获取商品统计信息失败", e);
            return Result.fail("查询失败");
        }
    }
    
    @PostMapping("/addProduct")
    public Result<Integer> addProduct(@RequestBody Product product) {
        try {
            logger.info("添加商品 - 商品名: {}", product.getProductName());
            
            if (!StringUtils.hasText(product.getProductName())) {
                return Result.fail("商品名称不能为空");
            }
            if (product.getBusinessLineId() == null) {
                return Result.fail("业务线ID不能为空");
            }
            
            int result = productService.insertSelective(product);
            if (result > 0) {
                logger.info("添加商品成功 - 商品ID: {}", product.getProductId());
                return Result.success("操作成功", result);
            } else {
                return Result.fail("添加失败");
            }
        } catch (Exception e) {
            logger.error("添加商品失败", e);
            return Result.fail("操作失败");
        }
    }
    
    @PostMapping("/updateProduct")
    public Result<Integer> updateProduct(@RequestBody Product product) {
        try {
            logger.info("更新商品 - 商品ID: {}", product.getProductId());
            
            if (product.getProductId() == null) {
                return Result.fail("商品ID不能为空");
            }
            
            int result = productService.updateByPrimaryKeySelective(product);
            if (result > 0) {
                logger.info("更新商品成功 - 商品ID: {}", product.getProductId());
                return Result.success("操作成功", result);
            } else {
                return Result.fail("更新失败，商品不存在");
            }
        } catch (Exception e) {
            logger.error("更新商品失败", e);
            return Result.fail("操作失败");
        }
    }
    
    @DeleteMapping("/deleteProduct")
    public Result<Integer> deleteProduct(@RequestParam(name = "productId") Long productId) {
        try {
            logger.info("删除商品 - 商品ID: {}", productId);
            
            if (productId == null) {
                return Result.fail("商品ID不能为空");
            }
            
            int result = productService.deleteByPrimaryKey(productId);
            if (result > 0) {
                logger.info("删除商品成功 - 商品ID: {}", productId);
                return Result.success("操作成功", result);
            } else {
                return Result.fail("删除失败，商品不存在");
            }
        } catch (Exception e) {
            logger.error("删除商品失败", e);
            return Result.fail("操作失败");
        }
    }
    
    @PostMapping("/batchUpdateStatus")
    public Result<Integer> batchUpdateStatus(@RequestParam(name = "productIds") String productIds,
                                           @RequestParam(name = "status") Integer status) {
        try {
            logger.info("批量更新商品状态 - productIds: {}, status: {}", productIds, status);
            
            if (!StringUtils.hasText(productIds)) {
                return Result.fail("商品ID不能为空");
            }
            if (status == null) {
                return Result.fail("状态不能为空");
            }
            
            List<Long> productIdList = new ArrayList<>();
            String[] idStrings = productIds.split(",");
            for (String idStr : idStrings) {
                try {
                    productIdList.add(Long.parseLong(idStr.trim()));
                } catch (NumberFormatException e) {
                    logger.warn("商品ID格式错误: {}", idStr);
                }
            }
            
            if (productIdList.isEmpty()) {
                return Result.fail("有效的商品ID不能为空");
            }
            
            int result = productService.batchUpdateStatus(productIdList, status);
            logger.info("批量更新商品状态完成 - 更新{}个商品", result);
            
            return Result.success("操作成功", result);
        } catch (Exception e) {
            logger.error("批量更新商品状态失败", e);
            return Result.fail("操作失败");
        }
    }
}
