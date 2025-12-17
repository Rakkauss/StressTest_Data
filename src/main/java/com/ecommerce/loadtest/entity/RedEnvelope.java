package com.ecommerce.loadtest.entity;

import java.util.Date;

/**
 * 红包记录实体类
 * 用于记录用户领取红包的详细信息
 * 
 * @author rakkaus
 */
public class RedEnvelope {
    
    private Long receiveId;
    private Long uid;
    private Long redEnvelopeId;
    private Long receiveTime;
    private Long batchId;
    private Date createTime;
    private Date updateTime;
    private Long amount;
    private Integer status;
    private Integer platformType;
    
    public RedEnvelope() {
    }
    
    public RedEnvelope(Long uid, Long redEnvelopeId, Long receiveTime, Long batchId) {
        this.uid = uid;
        this.redEnvelopeId = redEnvelopeId;
        this.receiveTime = receiveTime;
        this.batchId = batchId;
        this.status = 1;
        this.createTime = new Date();
    }
    
    public Long getReceiveId() {
        return receiveId;
    }
    
    public void setReceiveId(Long receiveId) {
        this.receiveId = receiveId;
    }
    
    public Long getUid() {
        return uid;
    }
    
    public void setUid(Long uid) {
        this.uid = uid;
    }
    
    public Long getRedEnvelopeId() {
        return redEnvelopeId;
    }
    
    public void setRedEnvelopeId(Long redEnvelopeId) {
        this.redEnvelopeId = redEnvelopeId;
    }
    
    public Long getReceiveTime() {
        return receiveTime;
    }
    
    public void setReceiveTime(Long receiveTime) {
        this.receiveTime = receiveTime;
    }
    
    public Long getBatchId() {
        return batchId;
    }
    
    public void setBatchId(Long batchId) {
        this.batchId = batchId;
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
    
    public Long getAmount() {
        return amount;
    }
    
    public void setAmount(Long amount) {
        this.amount = amount;
    }
    
    public Integer getStatus() {
        return status;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
    }
    
    public Integer getPlatformType() {
        return platformType;
    }
    
    public void setPlatformType(Integer platformType) {
        this.platformType = platformType;
    }
    
    @Override
    public String toString() {
        return "RedEnvelope{" +
                "receiveId=" + receiveId +
                ", uid=" + uid +
                ", redEnvelopeId=" + redEnvelopeId +
                ", receiveTime=" + receiveTime +
                ", batchId=" + batchId +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", amount=" + amount +
                ", status=" + status +
                ", platformType=" + platformType +
                '}';
    }
}
