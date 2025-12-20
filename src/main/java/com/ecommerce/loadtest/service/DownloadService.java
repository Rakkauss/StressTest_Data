package com.ecommerce.loadtest.service;

import com.ecommerce.loadtest.entity.DownloadTask;

import java.util.List;
import java.util.Map;

/**
 * 下载任务服务接口
 *
 * @author rakkaus
 */
public interface DownloadService {

    Long createDownloadTask(DownloadTask task);

    DownloadTask getDownloadTask(Long taskId);

    int updateDownloadTask(DownloadTask task);

    int deleteDownloadTask(Long taskId);

    List<DownloadTask> getUserDownloadTasks(String createUser, Integer status, Integer limit);

    List<DownloadTask> getPendingTasks(Integer limit);

    List<DownloadTask> getTimeoutTasks(Integer timeoutMinutes);

    int batchUpdateTaskStatus(List<Long> taskIds, Integer status);

    int cleanupExpiredTasks(Integer retentionDays);

    Map<String, Object> getTaskStatistics(String createUser);

    Map<String, Object> getTaskExecutionDetails(Long taskId);

    int updateTaskProgress(Long taskId, Long processedCount);

    int completeTask(Long taskId, Integer status, String fileUrl, String errorMessage);
}
