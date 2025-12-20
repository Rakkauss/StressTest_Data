package com.ecommerce.loadtest.service;

import com.ecommerce.loadtest.common.Result;

import java.util.List;
import java.util.Map;

/**
 * 数据导出服务接口
 *
 * @author rakkaus
 */
public interface ExportService {

    Result<Long> exportUserData(Integer userType, Integer userStatus, String createUser, String exportFormat);

    Result<Long> exportOrderData(String startDate, String endDate, Integer orderStatus,
                                Long businessLineId, String createUser, String exportFormat);

    Result<Long> exportRedEnvelopeData(Long batchId, Integer platformType, Integer status,
                                      String createUser, String exportFormat);

    Result<Long> exportBatchData(Integer batchStatus, String createUser, String requestUser, String exportFormat);

    Result<Long> exportCustomQuery(String queryName, Map<String, Object> queryParams,
                                  String createUser, String exportFormat);

    Result<Map<String, Object>> getExportTaskStatus(Long taskId);

    Result<String> cancelExportTask(Long taskId, String createUser);

    Result<List<Map<String, Object>>> getUserExportTasks(String createUser, Integer status, Integer limit);

    Result<String> cleanupExpiredExports(Integer retentionDays);

    Result<Map<String, Object>> getExportStatistics(String createUser);

    Result<Long> batchExport(List<Map<String, Object>> exportRequests, String createUser);

    List<String> getSupportedExportFormats();

    List<Map<String, Object>> getPredefinedQueries();
}
