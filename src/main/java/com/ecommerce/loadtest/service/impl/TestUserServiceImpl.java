package com.ecommerce.loadtest.service.impl;

import com.ecommerce.loadtest.dao.TestUserMapper;
import com.ecommerce.loadtest.entity.TestUser;
import com.ecommerce.loadtest.service.TestUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * 测试用户服务实现类
 * 
 * @author rakkaus
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class TestUserServiceImpl implements TestUserService {
    
    private static final Logger logger = LoggerFactory.getLogger(TestUserServiceImpl.class);
    
    @Autowired
    private TestUserMapper testUserMapper;
    
    @Override
    public int deleteByPrimaryKey(Long uid) {
        logger.info("删除测试用户 - uid: {}", uid);
        try {
            return testUserMapper.deleteByPrimaryKey(uid);
        } catch (Exception e) {
            logger.error("删除用户失败 - uid: {}", uid, e);
            throw new RuntimeException("删除用户失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public int insertSelective(TestUser record) {
        logger.info("插入测试用户 - uid: {}, userType: {}", record.getUid(), record.getUserType());
        try {
            if (record.getCreateTime() == null) {
                record.setCreateTime(new Date());
            }
            if (record.getUserStatus() == null) {
                record.setUserStatus(0);
            }
            return testUserMapper.insertSelective(record);
        } catch (Exception e) {
            logger.error("插入用户失败 - uid: {}", record.getUid(), e);
            throw new RuntimeException("插入用户失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public TestUser selectByPrimaryKey(Long uid) {
        logger.debug("查询测试用户 - uid: {}", uid);
        try {
            return testUserMapper.selectByPrimaryKey(uid);
        } catch (Exception e) {
            logger.error("查询用户失败 - uid: {}", uid, e);
            throw new RuntimeException("查询用户失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public int updateByPrimaryKeySelective(TestUser record) {
        logger.info("更新测试用户 - uid: {}", record.getUid());
        try {
            record.setUpdateTime(new Date());
            return testUserMapper.updateByPrimaryKeySelective(record);
        } catch (Exception e) {
            logger.error("更新用户失败 - uid: {}", record.getUid(), e);
            throw new RuntimeException("更新用户失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TestUser> queryUserList(Integer userType) {
        logger.info("查询用户列表 - userType: {}", userType);
        try {
            return testUserMapper.queryUserList(userType);
        } catch (Exception e) {
            logger.error("查询用户列表失败 - userType: {}", userType, e);
            throw new RuntimeException("查询用户列表失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public int fillUserInfo(Long uid, Boolean forceAddress) {
        logger.info("填充用户信息 - uid: {}, forceAddress: {}", uid, forceAddress);
        TestUser user = selectByPrimaryKey(uid);
        if (user == null) {
            logger.warn("用户不存在 - uid: {}", uid);
            return 0;
        }
        return fillUserInfo(user, forceAddress);
    }
    
    @Override
    public int fillUserInfo(TestUser user, Boolean forceAddress) {
        logger.info("填充用户信息 - uid: {}, forceAddress: {}", user.getUid(), forceAddress);
        boolean needUpdate = false;
        
        try {
            if (user.getPpu() == null || user.getPpu().isEmpty()) {
                String mockPpu = mockGeneratePPU(user.getUid(), user.getUserType());
                user.setPpu(mockPpu);
                needUpdate = true;
                logger.info("生成PPU成功 - uid: {}, ppu: {}", user.getUid(), mockPpu);
            }
            
            if (user.getAddressId() == null || forceAddress) {
                Long mockAddressId = mockBindAddress(user.getUid(), user.getUserType());
                user.setAddressId(mockAddressId);
                needUpdate = true;
                logger.info("绑定地址成功 - uid: {}, addressId: {}", user.getUid(), mockAddressId);
            }
            
            if (user.getUserType() == 2 && user.getPlatformBUid() == null) {
                Long mockPlatformBUid = mockCreatePlatformBUser(user.getUid());
                user.setPlatformBUid(mockPlatformBUid);
                needUpdate = true;
                logger.info("创建平台B用户成功 - uid: {}, platformBUid: {}", user.getUid(), mockPlatformBUid);
            }
            
            if (needUpdate) {
                return updateByPrimaryKeySelective(user);
            } else {
                logger.info("用户信息无需更新 - uid: {}", user.getUid());
                return 0;
            }
        } catch (Exception e) {
            logger.error("填充用户信息失败 - uid: {}", user.getUid(), e);
            throw new RuntimeException("填充用户信息失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void fillAllUserInfo(Integer userType, Boolean forceAddress) {
        logger.info("批量填充用户信息 - userType: {}, forceAddress: {}", userType, forceAddress);
        try {
            List<TestUser> userList = queryUserList(userType);
            logger.info("获取到{}个用户需要填充信息", userList.size());
            
            if (userList.isEmpty()) {
                logger.info("没有用户需要填充信息");
                return;
            }
            
            userList.parallelStream().forEach(user -> {
                try {
                    fillUserInfo(user, forceAddress);
                } catch (Exception e) {
                    logger.error("填充用户信息失败 - uid: {}", user.getUid(), e);
                }
            });
            
            logger.info("批量填充用户信息完成");
        } catch (Exception e) {
            logger.error("批量填充用户信息失败", e);
            throw new RuntimeException("批量填充用户信息失败: " + e.getMessage(), e);
        }
    }
    
    private String mockGeneratePPU(Long uid, Integer userType) {
        logger.debug("Mock生成PPU - uid: {}, userType: {}", uid, userType);
        try {
            Thread.sleep(50 + new Random().nextInt(100));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        String prefix = userType == 1 ? "PA" : "PB";
        return String.format("%s_%d_%d", prefix, uid, System.currentTimeMillis() % 100000);
    }
    
    private Long mockBindAddress(Long uid, Integer userType) {
        logger.debug("Mock绑定地址 - uid: {}, userType: {}", uid, userType);
        try {
            Thread.sleep(30 + new Random().nextInt(70));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        Long baseAddressId = userType == 1 ? 10000L : 20000L;
        return baseAddressId + (uid % 1000);
    }
    
    private Long mockCreatePlatformBUser(Long uid) {
        logger.debug("Mock创建平台B用户 - uid: {}", uid);
        try {
            Thread.sleep(100 + new Random().nextInt(200));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return 2000L + (uid % 10000);
    }
}
