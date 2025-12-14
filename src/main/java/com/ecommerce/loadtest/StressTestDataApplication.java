package com.ecommerce.loadtest;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

/**
 * Stress Test Data 压测数据处理平台
 * 
 * 核心功能:
 * 1. 高并发数据处理
 * 2. 异步任务调度
 * 3. 批量数据导出
 * 4. 压测数据管理
 * 
 * @author rakkaus
 * @version 1.0.0
 */
@MapperScan("com.ecommerce.loadtest.dao")
@SpringBootApplication
public class StressTestDataApplication {
    
    public static void main(String[] args) {
        try {
            new SpringApplicationBuilder()
                .sources(StressTestDataApplication.class)
                .web(WebApplicationType.SERVLET)
                .run(args);
            System.out.println("=== Stress Test Data 启动成功 ===");
            System.out.println("=== 压测数据处理平台已就绪 ===");
        } catch (Throwable e) {
            System.err.println("服务启动异常: " + e.getMessage());
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
