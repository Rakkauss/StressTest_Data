package com.ecommerce.loadtest.utils;

import com.ecommerce.loadtest.config.StressTestDataConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 配置工具类
 * 提供统一的配置访问接口
 * 
 * @author rakkaus
 */
@Component
public class ConfigUtil {
    
    private static StressTestDataConfig stressTestDataConfig;
    
    @Autowired
    public void setStressTestDataConfig(StressTestDataConfig stressTestDataConfig) {
        ConfigUtil.stressTestDataConfig = stressTestDataConfig;
    }
    
    public static int getInt(String key, int defaultValue) {
        if (stressTestDataConfig == null) {
            return defaultValue;
        }
        
        switch (key) {
            case "maximum_quantity_of_products":
                return stressTestDataConfig.getProcessing().getMaxProductCount();
            case "red_envelope_current_limiting":
                return stressTestDataConfig.getProcessing().getRedEnvelopeQpsLimit();
            case "default_thread_pool_size":
                return stressTestDataConfig.getProcessing().getDefaultThreadPoolSize();
            default:
                return defaultValue;
        }
    }
    
    public static String getValue(String key, String defaultValue) {
        if (stressTestDataConfig == null) {
            return defaultValue;
        }
        
        switch (key) {
            case "temp_dir":
                return stressTestDataConfig.getFile().getTempDir();
            case "default_receiver":
                return stressTestDataConfig.getNotification().getDefaultReceiver();
            default:
                return defaultValue;
        }
    }
    
    public static boolean getBoolean(String key, boolean defaultValue) {
        if (stressTestDataConfig == null) {
            return defaultValue;
        }
        
        switch (key) {
            case "email_enabled":
                return stressTestDataConfig.getNotification().isEmailEnabled();
            default:
                return defaultValue;
        }
    }
}
