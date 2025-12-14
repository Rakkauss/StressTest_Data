package com.ecommerce.loadtest.service;

import com.ecommerce.loadtest.entity.TestUser;

import java.util.List;

/**
 * 测试用户服务接口
 * 
 * @author rakkaus
 */
public interface TestUserService {
    
    int deleteByPrimaryKey(Long uid);
    
    int insertSelective(TestUser record);
    
    TestUser selectByPrimaryKey(Long uid);
    
    int updateByPrimaryKeySelective(TestUser record);
    
    List<TestUser> queryUserList(Integer userType);
    
    int fillUserInfo(Long uid, Boolean forceAddress);
    
    int fillUserInfo(TestUser testUser, Boolean forceAddress);
    
    void fillAllUserInfo(Integer userType, Boolean forceAddress);
}
