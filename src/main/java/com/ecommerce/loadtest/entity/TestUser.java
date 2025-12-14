package com.ecommerce.loadtest.entity;

import java.util.Date;

/**
 * 测试用户实体类
 * 用于管理压测用户数据
 * 
 * @author rakkaus
 */
public class TestUser {
    
    private Long uid;
    private Integer userStatus;
    private Integer userType;
    private Long addressId;
    private String ppu;
    private Long platformBUid;
    private Date createTime;
    private Date updateTime;
    
    public TestUser() {
    }
    
    public TestUser(Long uid, Integer userStatus, Integer userType, Long addressId, String ppu) {
        this.uid = uid;
        this.userStatus = userStatus;
        this.userType = userType;
        this.addressId = addressId;
        this.ppu = ppu;
    }
    
    public Long getUid() {
        return uid;
    }
    
    public void setUid(Long uid) {
        this.uid = uid;
    }
    
    public Integer getUserStatus() {
        return userStatus;
    }
    
    public void setUserStatus(Integer userStatus) {
        this.userStatus = userStatus;
    }
    
    public Integer getUserType() {
        return userType;
    }
    
    public void setUserType(Integer userType) {
        this.userType = userType;
    }
    
    public Long getAddressId() {
        return addressId;
    }
    
    public void setAddressId(Long addressId) {
        this.addressId = addressId;
    }
    
    public String getPpu() {
        return ppu;
    }
    
    public void setPpu(String ppu) {
        this.ppu = ppu == null ? null : ppu.trim();
    }
    
    public Long getPlatformBUid() {
        return platformBUid;
    }
    
    public void setPlatformBUid(Long platformBUid) {
        this.platformBUid = platformBUid;
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
        return "TestUser{" +
                "uid=" + uid +
                ", userStatus=" + userStatus +
                ", userType=" + userType +
                ", addressId=" + addressId +
                ", ppu='" + ppu + '\'' +
                ", platformBUid=" + platformBUid +
                '}';
    }
}
