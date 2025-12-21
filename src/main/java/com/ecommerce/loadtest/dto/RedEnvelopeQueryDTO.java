package com.ecommerce.loadtest.dto;

/**
 * 红包查询DTO
 * 
 * @author rakkaus
 */
public class RedEnvelopeQueryDTO {
    
    /**
     * 批次ID
     */
    private Long batchId;
    
    /**
     * 红包ID
     */
    private Long redEnvelopeId;
    
    /**
     * 用户ID
     */
    private Long uid;
    
    /**
     * 红包状态：0-待领取，1-已领取，2-已过期
     */
    private Integer status;
    
    /**
     * 平台类型：1-平台A，2-平台B
     */
    private Integer platformType;
    
    /**
     * 开始时间（时间戳）
     */
    private Long startTime;
    
    /**
     * 结束时间（时间戳）
     */
    private Long endTime;
    
    /**
     * 分页参数 - 页码
     */
    private Integer pageNum = 1;
    
    /**
     * 分页参数 - 页大小
     */
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
    
    @Override
    public String toString() {
        return "RedEnvelopeQueryDTO{" +
                "batchId=" + batchId +
                ", redEnvelopeId=" + redEnvelopeId +
                ", uid=" + uid +
                ", status=" + status +
                ", platformType=" + platformType +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", pageNum=" + pageNum +
                ", pageSize=" + pageSize +
                '}';
    }
}
