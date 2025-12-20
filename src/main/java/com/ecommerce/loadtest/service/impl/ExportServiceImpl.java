package com.ecommerce.loadtest.service.impl;

import com.ecommerce.loadtest.common.Result;
import com.ecommerce.loadtest.dao.LoadTestOrderMapper;
import com.ecommerce.loadtest.dao.RedEnvelopeMapper;
import com.ecommerce.loadtest.dao.TestBatchMapper;
import com.ecommerce.loadtest.dao.TestUserMapper;
import com.ecommerce.loadtest.entity.DownloadTask;
import com.ecommerce.loadtest.service.DownloadService;
import com.ecommerce.loadtest.service.ExportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * 数据导出服务实现类
 *
 * @author rakkaus
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ExportServiceImpl implements ExportService {

    private static final Logger logger = LoggerFactory.getLogger(ExportServiceImpl.class);

    @Autowired
    private TestUserMapper testUserMapper;

    @Autowired
    private LoadTestOrderMapper loadTestOrderMapper;

    @Autowired
    private RedEnvelopeMapper redEnvelopeMapper;

    @Autowired
    private TestBatchMapper testBatchMapper;

    @Autowired
    private DownloadService downloadService;

    private static final List<String> SUPPORTED_FORMATS = Arrays.asList("excel", "csv", "json");

    @Override
    public Result<Long> exportUserData(Integer userType, Integer userStatus, String createUser, String exportFormat) {
        if (!SUPPORTED_FORMATS.contains(exportFormat.toLowerCase())) {
            return Result.fail("不支持的导出格式: " + exportFormat);
        }
        try {
            DownloadTask task = new DownloadTask();
            task.setTaskName("用户数据导出");
            task.setCreateUser(createUser);
            task.setFileName(String.format("user_export_%s_%d.%s", createUser, System.currentTimeMillis(), exportFormat));
            Long taskId = downloadService.createDownloadTask(task);

            CompletableFuture.runAsync(() -> {
                try {
                    List<Map<String, Object>> data = testUserMapper.selectUserDataForExport(userType, userStatus);
                    String fileUrl = mockGenerateFile(data, exportFormat, task.getFileName());
                    downloadService.completeTask(taskId, 1, fileUrl, null);
                } catch (Exception e) {
                    logger.error("用户数据导出失败 - taskId: {}", taskId, e);
                    downloadService.completeTask(taskId, 2, null, e.getMessage());
                }
            });

            return Result.success("导出任务创建成功", taskId);
        } catch (Exception e) {
            logger.error("创建用户数据导出任务失败", e);
            return Result.fail("创建导出任务失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Long> exportOrderData(String startDate, String endDate, Integer orderStatus,
                                       Long businessLineId, String createUser, String exportFormat) {
        if (!SUPPORTED_FORMATS.contains(exportFormat.toLowerCase())) {
            return Result.fail("不支持的导出格式: " + exportFormat);
        }
        try {
            DownloadTask task = new DownloadTask();
            task.setTaskName("订单数据导出");
            task.setCreateUser(createUser);
            task.setFileName(String.format("order_export_%s_%d.%s", createUser, System.currentTimeMillis(), exportFormat));
            Long taskId = downloadService.createDownloadTask(task);

            CompletableFuture.runAsync(() -> {
                try {
                    List<Map<String, Object>> data = loadTestOrderMapper.selectOrderDataForExport(startDate, endDate, orderStatus, businessLineId);
                    String fileUrl = mockGenerateFile(data, exportFormat, task.getFileName());
                    downloadService.completeTask(taskId, 1, fileUrl, null);
                } catch (Exception e) {
                    logger.error("订单数据导出失败 - taskId: {}", taskId, e);
                    downloadService.completeTask(taskId, 2, null, e.getMessage());
                }
            });

            return Result.success("导出任务创建成功", taskId);
        } catch (Exception e) {
            logger.error("创建订单数据导出任务失败", e);
            return Result.fail("创建导出任务失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Long> exportRedEnvelopeData(Long batchId, Integer platformType, Integer status,
                                             String createUser, String exportFormat) {
        if (!SUPPORTED_FORMATS.contains(exportFormat.toLowerCase())) {
            return Result.fail("不支持的导出格式: " + exportFormat);
        }
        try {
            DownloadTask task = new DownloadTask();
            task.setTaskName("红包数据导出");
            task.setCreateUser(createUser);
            task.setFileName(String.format("red_envelope_export_%s_%d.%s", createUser, System.currentTimeMillis(), exportFormat));
            Long taskId = downloadService.createDownloadTask(task);

            CompletableFuture.runAsync(() -> {
                try {
                    List<Map<String, Object>> data = redEnvelopeMapper.selectRedEnvelopeDataForExport(batchId, platformType, status);
                    String fileUrl = mockGenerateFile(data, exportFormat, task.getFileName());
                    downloadService.completeTask(taskId, 1, fileUrl, null);
                } catch (Exception e) {
                    logger.error("红包数据导出失败 - taskId: {}", taskId, e);
                    downloadService.completeTask(taskId, 2, null, e.getMessage());
                }
            });

            return Result.success("导出任务创建成功", taskId);
        } catch (Exception e) {
            logger.error("创建红包数据导出任务失败", e);
            return Result.fail("创建导出任务失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Long> exportBatchData(Integer batchStatus, String createUser, String requestUser, String exportFormat) {
        if (!SUPPORTED_FORMATS.contains(exportFormat.toLowerCase())) {
            return Result.fail("不支持的导出格式: " + exportFormat);
        }
        try {
            DownloadTask task = new DownloadTask();
            task.setTaskName("批次数据导出");
            task.setCreateUser(requestUser);
            task.setFileName(String.format("batch_export_%s_%d.%s", requestUser, System.currentTimeMillis(), exportFormat));
            Long taskId = downloadService.createDownloadTask(task);

            CompletableFuture.runAsync(() -> {
                try {
                    List<Map<String, Object>> data = testBatchMapper.selectBatchDataForExport(batchStatus, createUser);
                    String fileUrl = mockGenerateFile(data, exportFormat, task.getFileName());
                    downloadService.completeTask(taskId, 1, fileUrl, null);
                } catch (Exception e) {
                    logger.error("批次数据导出失败 - taskId: {}", taskId, e);
                    downloadService.completeTask(taskId, 2, null, e.getMessage());
                }
            });

            return Result.success("导出任务创建成功", taskId);
        } catch (Exception e) {
            logger.error("创建批次数据导出任务失败", e);
            return Result.fail("创建导出任务失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Long> exportCustomQuery(String queryName, Map<String, Object> queryParams,
                                         String createUser, String exportFormat) {
        if (!SUPPORTED_FORMATS.contains(exportFormat.toLowerCase())) {
            return Result.fail("不支持的导出格式: " + exportFormat);
        }
        try {
            DownloadTask task = new DownloadTask();
            task.setTaskName("自定义查询导出: " + queryName);
            task.setCreateUser(createUser);
            task.setFileName(String.format("custom_query_%s_%s_%d.%s", queryName, createUser, System.currentTimeMillis(), exportFormat));
            Long taskId = downloadService.createDownloadTask(task);

            CompletableFuture.runAsync(() -> {
                try {
                    List<Map<String, Object>> data = executeCustomQuery(queryName, queryParams);
                    String fileUrl = mockGenerateFile(data, exportFormat, task.getFileName());
                    downloadService.completeTask(taskId, 1, fileUrl, null);
                } catch (Exception e) {
                    logger.error("自定义查询导出失败 - taskId: {}", taskId, e);
                    downloadService.completeTask(taskId, 2, null, e.getMessage());
                }
            });

            return Result.success("导出任务创建成功", taskId);
        } catch (Exception e) {
            logger.error("创建自定义查询导出任务失败", e);
            return Result.fail("创建导出任务失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Result<Map<String, Object>> getExportTaskStatus(Long taskId) {
        try {
            Map<String, Object> details = downloadService.getTaskExecutionDetails(taskId);
            return Result.success("查询成功", details);
        } catch (Exception e) {
            logger.error("获取导出任务状态失败", e);
            return Result.fail("查询失败: " + e.getMessage());
        }
    }

    @Override
    public Result<String> cancelExportTask(Long taskId, String createUser) {
        try {
            DownloadTask task = downloadService.getDownloadTask(taskId);
            if (task == null) {
                return Result.fail("任务不存在");
            }
            if (!createUser.equals(task.getCreateUser())) {
                return Result.fail("无权限取消此任务");
            }
            if (task.isCompleted()) {
                return Result.fail("任务已完成，无法取消");
            }
            downloadService.completeTask(taskId, 2, null, "用户取消");
            return Result.success("任务取消成功");
        } catch (Exception e) {
            logger.error("取消导出任务失败", e);
            return Result.fail("取消任务失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Result<List<Map<String, Object>>> getUserExportTasks(String createUser, Integer status, Integer limit) {
        try {
            List<DownloadTask> tasks = downloadService.getUserDownloadTasks(createUser, status, limit);
            List<Map<String, Object>> list = new ArrayList<>();
            for (DownloadTask task : tasks) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", task.getId());
                map.put("taskName", task.getTaskName());
                map.put("fileName", task.getFileName());
                map.put("fileUrl", task.getFileUrl());
                map.put("taskStatus", task.getTaskStatus());
                map.put("totalCount", task.getTotalCount());
                map.put("processedCount", task.getProcessedCount());
                map.put("progressPercentage", task.getProgressPercentage());
                map.put("createTime", task.getCreateTime());
                map.put("completeTime", task.getCompleteTime());
                map.put("errorMessage", task.getErrorMessage());
                list.add(map);
            }
            return Result.success("查询成功", list);
        } catch (Exception e) {
            logger.error("获取用户导出任务列表失败", e);
            return Result.fail("查询失败: " + e.getMessage());
        }
    }

    @Override
    public Result<String> cleanupExpiredExports(Integer retentionDays) {
        try {
            int count = downloadService.cleanupExpiredTasks(retentionDays);
            return Result.success("清理完成，共清理" + count + "个过期任务");
        } catch (Exception e) {
            logger.error("清理过期导出任务失败", e);
            return Result.fail("清理失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Result<Map<String, Object>> getExportStatistics(String createUser) {
        try {
            Map<String, Object> statistics = downloadService.getTaskStatistics(createUser);
            return Result.success("查询成功", statistics);
        } catch (Exception e) {
            logger.error("获取导出统计信息失败", e);
            return Result.fail("查询失败: " + e.getMessage());
        }
    }

    @Override
    public Result<Long> batchExport(List<Map<String, Object>> exportRequests, String createUser) {
        try {
            DownloadTask batchTask = new DownloadTask();
            batchTask.setTaskName("批量数据导出");
            batchTask.setCreateUser(createUser);
            batchTask.setFileName(String.format("batch_export_%s_%d.zip", createUser, System.currentTimeMillis()));
            batchTask.setTotalCount((long) exportRequests.size());
            Long batchTaskId = downloadService.createDownloadTask(batchTask);

            CompletableFuture.runAsync(() -> {
                try {
                    List<String> fileUrls = new ArrayList<>();
                    int processed = 0;
                    for (Map<String, Object> request : exportRequests) {
                        try {
                            String fileUrl = processSingleExportRequest(request, createUser);
                            if (fileUrl != null) {
                                fileUrls.add(fileUrl);
                            }
                            processed++;
                            downloadService.updateTaskProgress(batchTaskId, (long) processed);
                        } catch (Exception e) {
                            logger.error("处理单个导出请求失败", e);
                        }
                    }
                    String zipUrl = mockCompressFiles(fileUrls, batchTask.getFileName());
                    downloadService.completeTask(batchTaskId, 1, zipUrl, null);
                } catch (Exception e) {
                    logger.error("批量导出失败 - batchTaskId: {}", batchTaskId, e);
                    downloadService.completeTask(batchTaskId, 2, null, e.getMessage());
                }
            });

            return Result.success("批量导出任务创建成功", batchTaskId);
        } catch (Exception e) {
            logger.error("创建批量导出任务失败", e);
            return Result.fail("创建批量导出任务失败: " + e.getMessage());
        }
    }

    @Override
    public List<String> getSupportedExportFormats() {
        return new ArrayList<>(SUPPORTED_FORMATS);
    }

    @Override
    public List<Map<String, Object>> getPredefinedQueries() {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> userStats = new HashMap<>();
        userStats.put("name", "user_statistics");
        userStats.put("displayName", "用户统计分析");
        list.add(userStats);
        return list;
    }

    private List<Map<String, Object>> executeCustomQuery(String queryName, Map<String, Object> params) {
        if ("user_statistics".equals(queryName)) {
            return testUserMapper.selectUserStatistics((Integer) params.get("userType"));
        }
        throw new IllegalArgumentException("不支持的查询: " + queryName);
    }

    private String processSingleExportRequest(Map<String, Object> request, String createUser) {
        String dataType = (String) request.get("dataType");
        String exportFormat = (String) request.get("exportFormat");
        List<Map<String, Object>> data;
        if ("user".equals(dataType)) {
            data = testUserMapper.selectUserDataForExport((Integer) request.get("userType"), (Integer) request.get("userStatus"));
        } else if ("order".equals(dataType)) {
            data = loadTestOrderMapper.selectOrderDataForExport((String) request.get("startDate"), (String) request.get("endDate"),
                    (Integer) request.get("orderStatus"), (Long) request.get("businessLineId"));
        } else {
            throw new IllegalArgumentException("不支持的数据类型: " + dataType);
        }
        String fileName = String.format("%s_export_%s_%d.%s", dataType, createUser, System.currentTimeMillis(), exportFormat);
        return mockGenerateFile(data, exportFormat, fileName);
    }

    private String mockGenerateFile(List<Map<String, Object>> data, String format, String fileName) {
        try {
            int baseDelay = 100;
            int dataDelay = Math.min(data.size() / 10, 400);
            Thread.sleep(baseDelay + dataDelay);
            return String.format("/exports/%s/%s", format, fileName);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    private String mockCompressFiles(List<String> fileUrls, String zipFileName) {
        try {
            int baseDelay = 200;
            int fileDelay = fileUrls.size() * 50;
            Thread.sleep(baseDelay + fileDelay);
            return String.format("/exports/zip/%s", zipFileName);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }
}
