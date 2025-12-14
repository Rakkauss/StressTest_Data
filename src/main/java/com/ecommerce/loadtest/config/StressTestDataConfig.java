package com.ecommerce.loadtest.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 配置类
 * 使用Spring Boot原生配置管理
 * 
 * @author rakkaus
 */
@Configuration
@ConfigurationProperties(prefix = "stresstest")
public class StressTestDataConfig {
    
    private Processing processing = new Processing();
    private File file = new File();
    private Notification notification = new Notification();
    
    public static class Processing {
        private int maxProductCount = 300;
        private int redEnvelopeQpsLimit = 300;
        private int defaultThreadPoolSize = 10;
        
        public int getMaxProductCount() {
            return maxProductCount;
        }
        
        public void setMaxProductCount(int maxProductCount) {
            this.maxProductCount = maxProductCount;
        }
        
        public int getRedEnvelopeQpsLimit() {
            return redEnvelopeQpsLimit;
        }
        
        public void setRedEnvelopeQpsLimit(int redEnvelopeQpsLimit) {
            this.redEnvelopeQpsLimit = redEnvelopeQpsLimit;
        }
        
        public int getDefaultThreadPoolSize() {
            return defaultThreadPoolSize;
        }
        
        public void setDefaultThreadPoolSize(int defaultThreadPoolSize) {
            this.defaultThreadPoolSize = defaultThreadPoolSize;
        }
    }
    
    public static class File {
        private String tempDir = "/tmp/stress-test-data";
        private int retentionDays = 7;
        
        public String getTempDir() {
            return tempDir;
        }
        
        public void setTempDir(String tempDir) {
            this.tempDir = tempDir;
        }
        
        public int getRetentionDays() {
            return retentionDays;
        }
        
        public void setRetentionDays(int retentionDays) {
            this.retentionDays = retentionDays;
        }
    }
    
    public static class Notification {
        private boolean emailEnabled = false;
        private String defaultReceiver = "admin@example.com";
        
        public boolean isEmailEnabled() {
            return emailEnabled;
        }
        
        public void setEmailEnabled(boolean emailEnabled) {
            this.emailEnabled = emailEnabled;
        }
        
        public String getDefaultReceiver() {
            return defaultReceiver;
        }
        
        public void setDefaultReceiver(String defaultReceiver) {
            this.defaultReceiver = defaultReceiver;
        }
    }
    
    public Processing getProcessing() {
        return processing;
    }
    
    public void setProcessing(Processing processing) {
        this.processing = processing;
    }
    
    public File getFile() {
        return file;
    }
    
    public void setFile(File file) {
        this.file = file;
    }
    
    public Notification getNotification() {
        return notification;
    }
    
    public void setNotification(Notification notification) {
        this.notification = notification;
    }
}
