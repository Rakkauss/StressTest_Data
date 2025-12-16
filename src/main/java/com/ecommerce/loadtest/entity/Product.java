package com.ecommerce.loadtest.entity;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 商品实体类
 * 用于压测场景下的商品数据准备
 * 
 * @author rakkaus
 */
public class Product {
    
    private Long productId;
    private String productName;
    private Integer categoryId;
    private String categoryName;
    private Integer businessLineId;
    private BigDecimal price;
    private Integer status;
    private Integer stockCount;
    private String description;
    private String imageUrl;
    private Long sellerId;
    private String sellerName;
    private String tags;
    private Date createTime;
    private Date updateTime;
    
    public Product() {
    }
    
    public Product(Long productId, String productName, Integer businessLineId) {
        this.productId = productId;
        this.productName = productName;
        this.businessLineId = businessLineId;
        this.status = 1;
        this.createTime = new Date();
    }
    
    public Long getProductId() {
        return productId;
    }
    
    public void setProductId(Long productId) {
        this.productId = productId;
    }
    
    public String getProductName() {
        return productName;
    }
    
    public void setProductName(String productName) {
        this.productName = productName;
    }
    
    public Integer getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }
    
    public String getCategoryName() {
        return categoryName;
    }
    
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
    
    public Integer getBusinessLineId() {
        return businessLineId;
    }
    
    public void setBusinessLineId(Integer businessLineId) {
        this.businessLineId = businessLineId;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public Integer getStatus() {
        return status;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
    }
    
    public Integer getStockCount() {
        return stockCount;
    }
    
    public void setStockCount(Integer stockCount) {
        this.stockCount = stockCount;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public Long getSellerId() {
        return sellerId;
    }
    
    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }
    
    public String getSellerName() {
        return sellerName;
    }
    
    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }
    
    public String getTags() {
        return tags;
    }
    
    public void setTags(String tags) {
        this.tags = tags;
    }
    
    public Date getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
    
    public Date getUpdateTime() {
        return updateTime;
    }
    
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
    
    @Override
    public String toString() {
        return "Product{" +
                "productId=" + productId +
                ", productName='" + productName + '\'' +
                ", categoryId=" + categoryId +
                ", categoryName='" + categoryName + '\'' +
                ", businessLineId=" + businessLineId +
                ", price=" + price +
                ", status=" + status +
                ", stockCount=" + stockCount +
                ", description='" + description + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", sellerId=" + sellerId +
                ", sellerName='" + sellerName + '\'' +
                ", tags='" + tags + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }
}
