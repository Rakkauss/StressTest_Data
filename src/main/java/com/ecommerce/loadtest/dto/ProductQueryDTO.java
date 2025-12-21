package com.ecommerce.loadtest.dto;

import java.util.List;

/**
 * 商品查询DTO
 * 
 * @author rakkaus
 */
public class ProductQueryDTO {
    
    /**
     * 平台类型：1-平台A，2-平台B
     */
    private Integer platformType;
    
    /**
     * 业务线ID列表
     */
    private List<Integer> businessLineIds;
    
    /**
     * 页面大小（商品数量上限）
     */
    private Integer pageSize;
    
    /**
     * 分类ID列表
     */
    private List<Integer> categoryIds;
    
    /**
     * 商品状态列表：1-正常，0-下架，2-售罄
     */
    private List<Integer> statusList;
    
    /**
     * 最小价格
     */
    private Double minPrice;
    
    /**
     * 最大价格
     */
    private Double maxPrice;
    
    /**
     * 关键词搜索
     */
    private String keyword;
    
    /**
     * 排序字段：price-价格，createTime-创建时间，stockCount-库存
     */
    private String sortField;
    
    /**
     * 排序方向：asc-升序，desc-降序
     */
    private String sortDirection = "desc";
    
    /**
     * 是否包含库存为0的商品
     */
    private Boolean includeOutOfStock = false;
    
    public ProductQueryDTO() {
    }
    
    public Integer getPlatformType() {
        return platformType;
    }
    
    public void setPlatformType(Integer platformType) {
        this.platformType = platformType;
    }
    
    public List<Integer> getBusinessLineIds() {
        return businessLineIds;
    }
    
    public void setBusinessLineIds(List<Integer> businessLineIds) {
        this.businessLineIds = businessLineIds;
    }
    
    public Integer getPageSize() {
        return pageSize;
    }
    
    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
    
    public List<Integer> getCategoryIds() {
        return categoryIds;
    }
    
    public void setCategoryIds(List<Integer> categoryIds) {
        this.categoryIds = categoryIds;
    }
    
    public List<Integer> getStatusList() {
        return statusList;
    }
    
    public void setStatusList(List<Integer> statusList) {
        this.statusList = statusList;
    }
    
    public Double getMinPrice() {
        return minPrice;
    }
    
    public void setMinPrice(Double minPrice) {
        this.minPrice = minPrice;
    }
    
    public Double getMaxPrice() {
        return maxPrice;
    }
    
    public void setMaxPrice(Double maxPrice) {
        this.maxPrice = maxPrice;
    }
    
    public String getKeyword() {
        return keyword;
    }
    
    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
    
    public String getSortField() {
        return sortField;
    }
    
    public void setSortField(String sortField) {
        this.sortField = sortField;
    }
    
    public String getSortDirection() {
        return sortDirection;
    }
    
    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }
    
    public Boolean getIncludeOutOfStock() {
        return includeOutOfStock;
    }
    
    public void setIncludeOutOfStock(Boolean includeOutOfStock) {
        this.includeOutOfStock = includeOutOfStock;
    }
    
    public boolean hasValidParams() {
        return platformType != null || 
               (businessLineIds != null && !businessLineIds.isEmpty()) ||
               (categoryIds != null && !categoryIds.isEmpty()) ||
               keyword != null;
    }
    
    public int getEffectivePageSize(int defaultSize, int maxSize) {
        if (pageSize == null || pageSize <= 0) {
            return defaultSize;
        }
        return Math.min(pageSize, maxSize);
    }
    
    @Override
    public String toString() {
        return "ProductQueryDTO{" +
                "platformType=" + platformType +
                ", businessLineIds=" + businessLineIds +
                ", pageSize=" + pageSize +
                ", categoryIds=" + categoryIds +
                ", statusList=" + statusList +
                ", minPrice=" + minPrice +
                ", maxPrice=" + maxPrice +
                ", keyword='" + keyword + '\'' +
                ", sortField='" + sortField + '\'' +
                ", sortDirection='" + sortDirection + '\'' +
                ", includeOutOfStock=" + includeOutOfStock +
                '}';
    }
}
