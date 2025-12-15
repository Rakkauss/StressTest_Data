package com.ecommerce.loadtest.entity;

import java.util.Date;

/**
 * 压测订单实体类
 * 用于管理压测订单数据
 * 
 * @author rakkaus
 */
public class LoadTestOrder {
    
    private Long id;
    private Long orderId;
    private Long buyerId;
    private Byte currentStatus;
    private Date createTime;
    private Date updateTime;
    private Long businessLineId;
    
    public LoadTestOrder() {
    }
    
    public LoadTestOrder(Long orderId, Long buyerId, Long businessLineId, Byte currentStatus) {
        this.orderId = orderId;
        this.buyerId = buyerId;
        this.businessLineId = businessLineId;
        this.currentStatus = currentStatus;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getOrderId() {
        return orderId;
    }
    
    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }
    
    public Long getBuyerId() {
        return buyerId;
    }
    
    public void setBuyerId(Long buyerId) {
        this.buyerId = buyerId;
    }
    
    public Byte getCurrentStatus() {
        return currentStatus;
    }
    
    public void setCurrentStatus(Byte currentStatus) {
        this.currentStatus = currentStatus;
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
    
    public Long getBusinessLineId() {
        return businessLineId;
    }
    
    public void setBusinessLineId(Long businessLineId) {
        this.businessLineId = businessLineId;
    }
    
    @Override
    public String toString() {
        return "LoadTestOrder{" +
                "id=" + id +
                ", orderId=" + orderId +
                ", buyerId=" + buyerId +
                ", currentStatus=" + currentStatus +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", businessLineId=" + businessLineId +
                '}';
    }
}
