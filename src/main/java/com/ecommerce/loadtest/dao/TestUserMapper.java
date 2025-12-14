package com.ecommerce.loadtest.dao;

import com.ecommerce.loadtest.entity.TestUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 测试用户数据访问接口
 * 
 * @author rakkaus
 */
@Mapper
public interface TestUserMapper {
    
    int deleteByPrimaryKey(@Param("uid") Long uid);
    
    int insertSelective(TestUser record);
    
    TestUser selectByPrimaryKey(@Param("uid") Long uid);
    
    int updateByPrimaryKeySelective(TestUser record);
    
    List<TestUser> queryUserList(@Param("userType") Integer userType);
    
    List<TestUser> selectUsersNeedFill(@Param("userType") Integer userType);
}
