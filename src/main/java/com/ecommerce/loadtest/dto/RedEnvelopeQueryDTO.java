package com.ecommerce.loadtest.dto;

/**
 * 红包查询DTO
 * 
 * @author rakkaus
 */
public class RedEnvelopeQueryDTO {
    
    private Long batchId;
    private Long redEnvelopeId;
    private Long uid;
    private Integer status;
    private Integer platformType;
    private Long startTime;
    private Long endTime;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    
    public RedEnvelopeQueryDTO() {
    }
    
    public Long getBatchId() {
        return batchId;
    }
    
    public void setBatchId(Long batchId) {
        this.batchId = batchId;
    }
    
    public Long getRedEnvelopeId() {
        return redEnvelopeId;
    }
    
    public void setRedEnvelopeId(Long redEnvelopeId) {
        this.redEnvelopeId = redEnvelopeId;
    }
    
    public Long getUid() {
        return uid;
    }
    
    public void setUid(Long uid) {
        this.uid = uid;
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
    
    public Long getStartTime() {
        return startTime;
    }
    
    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }
    
    public Long getEndTime() {
        return endTime;
    }
    
    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }
    
    public Integer getPageNum() {
        return pageNum;
    }
    
    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }
    
    public Integer getPageSize() {
        return pageSize;
    }
    
    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
    
    public boolean hasValidParams() {
        return batchId != null || redEnvelopeId != null || uid != null;
    }
}
