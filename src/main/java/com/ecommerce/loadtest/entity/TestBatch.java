package com.ecommerce.loadtest.entity;

/**
 * 测试批次实体类
 * 用于管理压测批次数据
 * 
 * @author rakkaus
 */
public class TestBatch {
    
    private Long batchId;
    private String createUser;
    private Long createTime;
    private Long totalCount;
    private Long realCount;
    private Integer batchStatus;
    private Long updateTime;
    
    public TestBatch() {
    }
    
    public TestBatch(Long batchId, String createUser, Long createTime, Long totalCount, Long realCount, Integer batchStatus) {
        this.batchId = batchId;
        this.createUser = createUser;
        this.createTime = createTime;
        this.totalCount = totalCount;
        this.realCount = realCount;
        this.batchStatus = batchStatus;
    }
    
    public Long getBatchId() {
        return batchId;
    }
    
    public void setBatchId(Long batchId) {
        this.batchId = batchId;
    }
    
    public String getCreateUser() {
        return createUser;
    }
    
    public void setCreateUser(String createUser) {
        this.createUser = createUser == null ? null : createUser.trim();
    }
    
    public Long getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }
    
    public Long getTotalCount() {
        return totalCount;
    }
    
    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }
    
    public Long getRealCount() {
        return realCount;
    }
    
    public void setRealCount(Long realCount) {
        this.realCount = realCount;
    }
    
    public Integer getBatchStatus() {
        return batchStatus;
    }
    
    public void setBatchStatus(Integer batchStatus) {
        this.batchStatus = batchStatus;
    }
    
    public Long getUpdateTime() {
        return updateTime;
    }
    
    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }
    
    @Override
    public String toString() {
        return "TestBatch{" +
                "batchId=" + batchId +
                ", createUser='" + createUser + '\'' +
                ", createTime=" + createTime +
                ", totalCount=" + totalCount +
                ", realCount=" + realCount +
                ", batchStatus=" + batchStatus +
                '}';
    }
}

