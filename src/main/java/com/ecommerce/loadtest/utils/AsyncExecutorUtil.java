package com.ecommerce.loadtest.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 异步执行工具类
 * 提供线程池管理，用于异步任务处理
 * 
 * @author rakkaus
 */
@Component
public class AsyncExecutorUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(AsyncExecutorUtil.class);
    
    private static ExecutorService executorService;
    
    private static final int CORE_POOL_SIZE = 5;
    private static final int MAXIMUM_POOL_SIZE = 20;
    private static final long KEEP_ALIVE_TIME = 60L;
    private static final int QUEUE_CAPACITY = 1000;
    
    @PostConstruct
    public void init() {
        logger.info("初始化异步执行器 - 核心线程数:{}, 最大线程数:{}, 队列容量:{}", 
                   CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, QUEUE_CAPACITY);
        
        executorService = new ThreadPoolExecutor(
            CORE_POOL_SIZE,
            MAXIMUM_POOL_SIZE,
            KEEP_ALIVE_TIME,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(QUEUE_CAPACITY),
            r -> {
                Thread thread = new Thread(r);
                thread.setName("stress-test-async-" + thread.getId());
                thread.setDaemon(true);
                return thread;
            },
            new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }
    
    public static ExecutorService getExecutorService() {
        if (executorService == null) {
            throw new IllegalStateException("AsyncExecutorUtil 未初始化，请确保Spring容器已启动");
        }
        return executorService;
    }
    
    public static void submit(Runnable task) {
        try {
            getExecutorService().submit(task);
            logger.debug("异步任务提交成功");
        } catch (Exception e) {
            logger.error("异步任务提交失败", e);
            throw new RuntimeException("异步任务提交失败", e);
        }
    }
    
    public static String getPoolStatus() {
        if (executorService instanceof ThreadPoolExecutor) {
            ThreadPoolExecutor tpe = (ThreadPoolExecutor) executorService;
            return String.format(
                "线程池状态 - 活跃线程:%d, 核心线程:%d, 最大线程:%d, 队列大小:%d, 已完成任务:%d",
                tpe.getActiveCount(),
                tpe.getCorePoolSize(),
                tpe.getMaximumPoolSize(),
                tpe.getQueue().size(),
                tpe.getCompletedTaskCount()
            );
        }
        return "无法获取线程池状态";
    }
    
    @PreDestroy
    public void destroy() {
        if (executorService != null && !executorService.isShutdown()) {
            logger.info("开始关闭异步执行器...");
            
            executorService.shutdown();
            
            try {
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    logger.warn("等待任务完成超时，强制关闭线程池");
                    executorService.shutdownNow();
                    
                    if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                        logger.error("无法关闭线程池");
                    }
                }
                logger.info("异步执行器关闭完成");
            } catch (InterruptedException e) {
                logger.error("关闭异步执行器时被中断", e);
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
