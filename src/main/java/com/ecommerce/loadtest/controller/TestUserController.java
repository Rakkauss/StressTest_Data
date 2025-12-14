package com.ecommerce.loadtest.controller;

import com.ecommerce.loadtest.common.Result;
import com.ecommerce.loadtest.entity.TestUser;
import com.ecommerce.loadtest.service.TestUserService;
import com.ecommerce.loadtest.utils.AsyncExecutorUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 测试用户控制器--管理压测测试用户
 * 
 * @author rakkaus
 */
@RestController
@RequestMapping("/user")
public class TestUserController {
    
    private static final Logger logger = LoggerFactory.getLogger(TestUserController.class);
    
    @Autowired
    private TestUserService testUserService;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @GetMapping("/queryUserList")
    public Result<List<TestUser>> queryUserList(@RequestParam(name = "userType") Integer userType) {
        try {
            logger.info("查询测试用户列表 - userType: {}", userType);
            
            if (userType == null || (userType != 1 && userType != 2)) {
                return Result.fail("用户类型参数错误，只支持1(平台A)或2(平台B)");
            }
            
            List<TestUser> userList = testUserService.queryUserList(userType);
            logger.info("查询测试用户列表成功 - 共{}个用户", userList.size());
            
            return Result.success("查询成功", userList);
        } catch (Exception e) {
            logger.error("查询测试用户列表失败", e);
            return Result.fail("查询失败");
        }
    }
    
    @GetMapping("/addUser")
    public Result<Integer> addUser(TestUser testUser) {
        try {
            logger.info("添加测试用户 - 参数: {}", objectMapper.writeValueAsString(testUser));
            
            if (testUser.getUid() == null) {
                return Result.fail("用户ID不能为空");
            }
            if (testUser.getUserType() == null || (testUser.getUserType() != 1 && testUser.getUserType() != 2)) {
                return Result.fail("用户类型错误，只支持1(平台A)或2(平台B)");
            }
            
            if (testUser.getUserStatus() == null) {
                testUser.setUserStatus(0);
            }
            
            int result = testUserService.insertSelective(testUser);
            if (result > 0) {
                logger.info("添加测试用户成功 - uid: {}", testUser.getUid());
                return Result.success("操作成功", result);
            } else {
                return Result.fail("添加失败，可能用户已存在");
            }
        } catch (Exception e) {
            logger.error("添加测试用户失败", e);
            return Result.fail("插入失败");
        }
    }
    
    @GetMapping("/fillUserInfo")
    public Result<Integer> fillUserInfo(@RequestParam(name = "uid") Long uid,
                                       @RequestParam(name = "forceAddress", required = false, defaultValue = "false") Boolean forceAddress) {
        try {
            logger.info("填充用户信息 - uid: {}, forceAddress: {}", uid, forceAddress);
            
            if (uid == null) {
                return Result.fail("用户ID不能为空");
            }
            
            int result = testUserService.fillUserInfo(uid, forceAddress);
            if (result > 0) {
                logger.info("填充用户信息成功 - uid: {}", uid);
                return Result.success("操作成功", result);
            } else {
                return Result.fail("填充失败，用户不存在或信息已完整");
            }
        } catch (Exception e) {
            logger.error("填充用户信息失败 - uid: {}", uid, e);
            return Result.fail("操作失败");
        }
    }
    
    @GetMapping("/fillAllUserInfo")
    public Result<String> fillAllUserInfo(@RequestParam(name = "userType") Integer userType,
                                         @RequestParam(name = "forceAddress", required = false, defaultValue = "false") Boolean forceAddress) {
        try {
            logger.info("批量填充用户信息 - userType: {}, forceAddress: {}", userType, forceAddress);
            
            if (userType == null || (userType != 1 && userType != 2)) {
                return Result.fail("用户类型参数错误");
            }
            
            AsyncExecutorUtil.submit(() -> {
                try {
                    testUserService.fillAllUserInfo(userType, forceAddress);
                    logger.info("批量填充用户信息完成 - userType: {}", userType);
                } catch (Exception e) {
                    logger.error("批量填充用户信息异常 - userType: {}", userType, e);
                }
            });
            
            return Result.success("开始异步执行", "批量填充任务已提交，请稍后查看结果");
        } catch (Exception e) {
            logger.error("批量填充用户信息失败", e);
            return Result.fail("提交失败");
        }
    }
    
    @PostMapping("/updateUser")
    public Result<Integer> updateUser(@RequestBody TestUser testUser) {
        try {
            logger.info("更新测试用户 - 参数: {}", objectMapper.writeValueAsString(testUser));
            
            if (testUser.getUid() == null) {
                return Result.fail("用户ID不能为空");
            }
            
            int result = testUserService.updateByPrimaryKeySelective(testUser);
            if (result > 0) {
                logger.info("更新测试用户成功 - uid: {}", testUser.getUid());
                return Result.success("操作成功", result);
            } else {
                return Result.fail("更新失败，用户不存在");
            }
        } catch (Exception e) {
            logger.error("更新测试用户失败", e);
            return Result.fail("更新失败");
        }
    }
    
    @GetMapping("/deleteUser")
    public Result<Integer> deleteUser(@RequestParam(name = "uid") Long uid) {
        try {
            logger.info("删除测试用户 - uid: {}", uid);
            
            if (uid == null) {
                return Result.fail("用户ID不能为空");
            }
            
            int result = testUserService.deleteByPrimaryKey(uid);
            if (result > 0) {
                logger.info("删除测试用户成功 - uid: {}", uid);
                return Result.success("操作成功", result);
            } else {
                return Result.fail("删除失败，用户不存在");
            }
        } catch (Exception e) {
            logger.error("删除测试用户失败 - uid: {}", uid, e);
            return Result.fail("删除失败");
        }
    }
    
    @GetMapping("/poolStatus")
    public Result<String> getPoolStatus() {
        try {
            String status = AsyncExecutorUtil.getPoolStatus();
            return Result.success("获取成功", status);
        } catch (Exception e) {
            logger.error("获取线程池状态失败", e);
            return Result.fail("获取失败");
        }
    }
}
