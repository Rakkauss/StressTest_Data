package com.ecommerce.loadtest.dto;

/**
 * 测试用户查询DTO
 * 
 * @author rakkaus
 */
public class TestUserQueryDTO {
    
    /**
     * 用户类型：1-平台A用户，2-平台B用户
     */
    private Integer userType;
    
    /**
     * 用户状态：0-可用，1-不可用
     */
    private Integer userStatus;
    
    /**
     * 用户ID
     */
    private Long uid;
    
    /**
     * 分页参数 - 页码
     */
    private Integer pageNum = 1;
    
    /**
     * 分页参数 - 页大小
     */
    private Integer pageSize = 10;
    
    public TestUserQueryDTO() {
    }
    
    public TestUserQueryDTO(Integer userType) {
        this.userType = userType;
    }
    
    public Integer getUserType() {
        return userType;
    }
    
    public void setUserType(Integer userType) {
        this.userType = userType;
    }
    
    public Integer getUserStatus() {
        return userStatus;
    }
    
    public void setUserStatus(Integer userStatus) {
        this.userStatus = userStatus;
    }
    
    public Long getUid() {
        return uid;
    }
    
    public void setUid(Long uid) {
        this.uid = uid;
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
    
    @Override
    public String toString() {
        return "TestUserQueryDTO{" +
                "userType=" + userType +
                ", userStatus=" + userStatus +
                ", uid=" + uid +
                ", pageNum=" + pageNum +
                ", pageSize=" + pageSize +
                '}';
    }
}
