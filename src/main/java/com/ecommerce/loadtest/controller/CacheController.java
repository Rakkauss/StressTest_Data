package com.ecommerce.loadtest.controller;

import com.ecommerce.loadtest.common.Result;
import com.ecommerce.loadtest.utils.RedisUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * 缓存监控控制器
 * 
 * @author rakkaus
 */
@RestController
@RequestMapping("/cache")
public class CacheController {
    
    private static final Logger logger = LoggerFactory.getLogger(CacheController.class);
    
    @Autowired
    private RedisUtil redisUtil;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired(required = false)
    private CacheManager cacheManager;
    
    /**
     * 获取缓存统计信息
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getCacheStatistics() {
        logger.info("获取缓存统计信息");
        
        try {
            Map<String, Object> statistics = new HashMap<>();
            
            RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
            Properties info = connection.info();
            Properties keyspaceInfo = connection.info("keyspace");
            
            statistics.put("redis_version", info.getProperty("redis_version"));
            statistics.put("connected_clients", info.getProperty("connected_clients"));
            statistics.put("used_memory_human", info.getProperty("used_memory_human"));
            statistics.put("total_commands_processed", info.getProperty("total_commands_processed"));
            
            if (keyspaceInfo != null && !keyspaceInfo.isEmpty()) {
                String db0Info = keyspaceInfo.getProperty("db0");
                if (db0Info != null) {
                    String[] parts = db0Info.split(",");
                    for (String part : parts) {
                        if (part.startsWith("keys=")) {
                            statistics.put("total_keys", part.substring(5));
                        }
                    }
                }
            }
            
            connection.close();
            
            statistics.put("order_key_prefix", "stress:order:");
            statistics.put("cart_key_prefix", "stress:cart:");
            statistics.put("user_key_prefix", "stress:user:");
            
            return Result.success(statistics);
            
        } catch (Exception e) {
            logger.error("获取缓存统计信息失败", e);
            return Result.fail("获取缓存统计信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 清理指定前缀的缓存
     */
    @GetMapping("/clear")
    public Result<String> clearCache(@RequestParam(required = false) String prefix) {
        logger.info("清理缓存 - prefix: {}", prefix);
        
        try {
            if (prefix == null || prefix.isEmpty()) {
                return Result.fail("请指定要清理的缓存前缀");
            }
            
            Set<String> keys = redisTemplate.keys(prefix + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                logger.info("清理缓存成功 - prefix: {}, 数量: {}", prefix, keys.size());
                return Result.success("清理成功，删除" + keys.size() + "个缓存");
            } else {
                return Result.success("没有找到匹配的缓存");
            }
            
        } catch (Exception e) {
            logger.error("清理缓存失败", e);
            return Result.fail("清理缓存失败: " + e.getMessage());
        }
    }
    
    /**
     * 查看指定key的缓存内容
     */
    @GetMapping("/get")
    public Result<Object> getCache(@RequestParam String key) {
        logger.info("查看缓存 - key: {}", key);
        
        try {
            if (!redisUtil.hasKey(key)) {
                return Result.fail("缓存不存在");
            }
            
            Object value = redisUtil.get(key);
            long expire = redisUtil.getExpire(key);
            
            Map<String, Object> result = new HashMap<>();
            result.put("key", key);
            result.put("value", value);
            result.put("expire", expire);
            result.put("expireText", expire > 0 ? expire + "秒" : "永久");
            
            return Result.success(result);
            
        } catch (Exception e) {
            logger.error("查看缓存失败", e);
            return Result.fail("查看缓存失败: " + e.getMessage());
        }
    }
    
    /**
     * 查看所有缓存key列表
     */
    @GetMapping("/keys")
    public Result<List<String>> getCacheKeys(@RequestParam(required = false) String pattern) {
        logger.info("查看缓存key列表 - pattern: {}", pattern);
        
        try {
            String searchPattern = (pattern != null && !pattern.isEmpty()) ? pattern : "*";
            Set<String> keys = redisTemplate.keys(searchPattern);
            
            List<String> keyList = new ArrayList<>();
            if (keys != null) {
                keyList.addAll(keys);
                keyList.sort(String::compareTo);
            }
            
            return Result.success(keyList);
            
        } catch (Exception e) {
            logger.error("查看缓存key列表失败", e);
            return Result.fail("查看缓存key列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 删除指定key
     */
    @GetMapping("/delete")
    public Result<String> deleteCache(@RequestParam String key) {
        logger.info("删除缓存 - key: {}", key);
        
        try {
            if (!redisUtil.hasKey(key)) {
                return Result.fail("缓存不存在");
            }
            
            redisUtil.del(key);
            return Result.success("删除成功");
            
        } catch (Exception e) {
            logger.error("删除缓存失败", e);
            return Result.fail("删除缓存失败: " + e.getMessage());
        }
    }
    
    /**
     * 设置key的过期时间
     */
    @GetMapping("/expire")
    public Result<String> setExpire(@RequestParam String key, @RequestParam Long seconds) {
        logger.info("设置过期时间 - key: {}, seconds: {}", key, seconds);
        
        try {
            if (!redisUtil.hasKey(key)) {
                return Result.fail("缓存不存在");
            }
            
            boolean success = redisUtil.expire(key, seconds);
            if (success) {
                return Result.success("设置成功");
            } else {
                return Result.fail("设置失败");
            }
            
        } catch (Exception e) {
            logger.error("设置过期时间失败", e);
            return Result.fail("设置过期时间失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取Redis服务器信息
     */
    @GetMapping("/info")
    public Result<Map<String, String>> getRedisInfo() {
        logger.info("获取Redis服务器信息");
        
        try {
            RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
            Properties info = connection.info();
            connection.close();
            
            Map<String, String> result = new HashMap<>();
            info.forEach((key, value) -> result.put(key.toString(), value.toString()));
            
            return Result.success(result);
            
        } catch (Exception e) {
            logger.error("获取Redis服务器信息失败", e);
            return Result.fail("获取Redis服务器信息失败: " + e.getMessage());
        }
    }
    
    /**
     * 测试Redis连接
     */
    @GetMapping("/ping")
    public Result<String> ping() {
        logger.info("测试Redis连接");
        
        try {
            RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();
            String pong = connection.ping();
            connection.close();
            
            return Result.success("Redis连接正常: " + pong);
            
        } catch (Exception e) {
            logger.error("Redis连接测试失败", e);
            return Result.fail("Redis连接失败: " + e.getMessage());
        }
    }
}
