package com.ecommerce.loadtest.service.impl;

import com.ecommerce.loadtest.dao.DownloadTaskMapper;
import com.ecommerce.loadtest.entity.DownloadTask;
import com.ecommerce.loadtest.service.DownloadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 下载任务服务实现类
 *
 * @author rakkaus
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class DownloadServiceImpl implements DownloadService {

    private static final Logger logger = LoggerFactory.getLogger(DownloadServiceImpl.class);

    @Autowired
    private DownloadTaskMapper downloadTaskMapper;

    @Override
    public Long createDownloadTask(DownloadTask task) {
        logger.info("创建下载任务 - taskName: {}, createUser: {}", task.getTaskName(), task.getCreateUser());
        try {
            if (task.getCreateTime() == null) {
                task.setCreateTime(new Date());
            }
            if (task.getTaskStatus() == null) {
                task.setTaskStatus(0);
            }
            if (task.getTotalCount() == null) {
                task.setTotalCount(0L);
            }
            if (task.getProcessedCount() == null) {
                task.setProcessedCount(0L);
            }
            downloadTaskMapper.insertSelective(task);
            return task.getId();
        } catch (Exception e) {
            logger.error("创建下载任务失败", e);
            throw new RuntimeException("创建下载任务失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public DownloadTask getDownloadTask(Long taskId) {
        logger.debug("查询下载任务 - taskId: {}", taskId);
        try {
            return downloadTaskMapper.selectByPrimaryKey(taskId);
        } catch (Exception e) {
            logger.error("查询下载任务失败", e);
            throw new RuntimeException("查询下载任务失败: " + e.getMessage(), e);
        }
    }

    @Override
    public int updateDownloadTask(DownloadTask task) {
        logger.info("更新下载任务 - taskId: {}", task.getId());
        try {
            return downloadTaskMapper.updateByPrimaryKeySelective(task);
        } catch (Exception e) {
            logger.error("更新下载任务失败", e);
            throw new RuntimeException("更新下载任务失败: " + e.getMessage(), e);
        }
    }

    @Override
    public int deleteDownloadTask(Long taskId) {
        logger.info("删除下载任务 - taskId: {}", taskId);
        try {
            DownloadTask task = downloadTaskMapper.selectByPrimaryKey(taskId);
            if (task != null && task.getFileUrl() != null) {
                mockDeleteFile(task.getFileUrl());
            }
            return downloadTaskMapper.deleteByPrimaryKey(taskId);
        } catch (Exception e) {
            logger.error("删除下载任务失败", e);
            throw new RuntimeException("删除下载任务失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DownloadTask> getUserDownloadTasks(String createUser, Integer status, Integer limit) {
        logger.info("查询用户下载任务 - createUser: {}, status: {}, limit: {}", createUser, status, limit);
        try {
            return downloadTaskMapper.selectUserDownloadTasks(createUser, status, limit);
        } catch (Exception e) {
            logger.error("查询用户下载任务失败", e);
            throw new RuntimeException("查询用户下载任务失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DownloadTask> getPendingTasks(Integer limit) {
        logger.info("查询待处理任务 - limit: {}", limit);
        try {
            return downloadTaskMapper.selectPendingTasks(limit);
        } catch (Exception e) {
            logger.error("查询待处理任务失败", e);
            throw new RuntimeException("查询待处理任务失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DownloadTask> getTimeoutTasks(Integer timeoutMinutes) {
        logger.info("查询超时任务 - timeoutMinutes: {}", timeoutMinutes);
        try {
            return downloadTaskMapper.selectTimeoutTasks(timeoutMinutes);
        } catch (Exception e) {
            logger.error("查询超时任务失败", e);
            throw new RuntimeException("查询超时任务失败: " + e.getMessage(), e);
        }
    }

    @Override
    public int batchUpdateTaskStatus(List<Long> taskIds, Integer status) {
        logger.info("批量更新任务状态 - size: {}, status: {}", taskIds.size(), status);
        if (taskIds == null || taskIds.isEmpty()) {
            return 0;
        }
        try {
            return downloadTaskMapper.batchUpdateTaskStatus(taskIds, status);
        } catch (Exception e) {
            logger.error("批量更新任务状态失败", e);
            throw new RuntimeException("批量更新任务状态失败: " + e.getMessage(), e);
        }
    }

    @Override
    public int cleanupExpiredTasks(Integer retentionDays) {
        logger.info("清理过期任务 - retentionDays: {}", retentionDays);
        try {
            List<DownloadTask> tasks = downloadTaskMapper.selectUserDownloadTasks(null, null, null);
            for (DownloadTask task : tasks) {
                if (task.getFileUrl() != null && task.isCompleted()) {
                    mockDeleteFile(task.getFileUrl());
                }
            }
            return downloadTaskMapper.cleanupExpiredTasks(retentionDays);
        } catch (Exception e) {
            logger.error("清理过期任务失败", e);
            throw new RuntimeException("清理过期任务失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getTaskStatistics(String createUser) {
        logger.info("获取任务统计信息 - createUser: {}", createUser);
        try {
            return downloadTaskMapper.selectTaskStatistics(createUser);
        } catch (Exception e) {
            logger.error("获取任务统计信息失败", e);
            throw new RuntimeException("获取任务统计信息失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getTaskExecutionDetails(Long taskId) {
        logger.info("获取任务执行详情 - taskId: {}", taskId);
        try {
            return downloadTaskMapper.selectTaskExecutionDetails(taskId);
        } catch (Exception e) {
            logger.error("获取任务执行详情失败", e);
            throw new RuntimeException("获取任务执行详情失败: " + e.getMessage(), e);
        }
    }

    @Override
    public int updateTaskProgress(Long taskId, Long processedCount) {
        logger.debug("更新任务进度 - taskId: {}, processedCount: {}", taskId, processedCount);
        try {
            return downloadTaskMapper.updateTaskProgress(taskId, processedCount);
        } catch (Exception e) {
            logger.error("更新任务进度失败", e);
            throw new RuntimeException("更新任务进度失败: " + e.getMessage(), e);
        }
    }

    @Override
    public int completeTask(Long taskId, Integer status, String fileUrl, String errorMessage) {
        logger.info("完成任务 - taskId: {}, status: {}", taskId, status);
        try {
            int result = downloadTaskMapper.completeTask(taskId, status, fileUrl, errorMessage);
            if (result > 0) {
                DownloadTask task = downloadTaskMapper.selectByPrimaryKey(taskId);
                if (task != null) {
                    mockSendTaskCompleteNotification(task, status != null && status == 1);
                }
            }
            return result;
        } catch (Exception e) {
            logger.error("完成任务失败", e);
            throw new RuntimeException("完成任务失败: " + e.getMessage(), e);
        }
    }

    private boolean mockDeleteFile(String fileUrl) {
        logger.debug("Mock删除文件 - fileUrl: {}", fileUrl);
        try {
            Thread.sleep(10 + (int) (Math.random() * 40));
            return Math.random() < 0.95;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Mock删除文件被中断", e);
            return false;
        }
    }

    private void mockSendTaskCompleteNotification(DownloadTask task, boolean success) {
        logger.debug("Mock任务完成通知 - taskId: {}, success: {}", task.getId(), success);
        try {
            Thread.sleep(20 + (int) (Math.random() * 80));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Mock通知发送被中断", e);
        }
    }
}
