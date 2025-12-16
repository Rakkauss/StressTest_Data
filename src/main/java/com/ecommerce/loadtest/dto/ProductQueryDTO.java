package com.ecommerce.loadtest.dto;

import java.util.List;

/**
 * 商品查询DTO
 * 
 * @author rakkaus
 */
public class ProductQueryDTO {
    
    private Integer platformType;
    private List<Integer> businessLineIds;
    private Integer pageSize;
    private List<Integer> categoryIds;
    private List<Integer> statusList;
    private Double minPrice;
    private Double maxPrice;
    private String keyword;
    private String sortField;
    private String sortDirection = "desc";
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
}
