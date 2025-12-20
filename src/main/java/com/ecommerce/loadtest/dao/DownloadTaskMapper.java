package com.ecommerce.loadtest.dao;

import com.ecommerce.loadtest.entity.DownloadTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 下载任务数据访问接口
 *
 * @author rakkaus
 */
@Mapper
public interface DownloadTaskMapper {

    int deleteByPrimaryKey(Long id);

    int insertSelective(DownloadTask record);

    DownloadTask selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(DownloadTask record);

    List<DownloadTask> selectUserDownloadTasks(@Param("createUser") String createUser,
                                              @Param("status") Integer status,
                                              @Param("limit") Integer limit);

    List<DownloadTask> selectPendingTasks(@Param("limit") Integer limit);

    List<DownloadTask> selectTimeoutTasks(@Param("timeoutMinutes") Integer timeoutMinutes);

    int batchUpdateTaskStatus(@Param("taskIds") List<Long> taskIds, @Param("status") Integer status);

    int cleanupExpiredTasks(@Param("retentionDays") Integer retentionDays);

    Map<String, Object> selectTaskStatistics(@Param("createUser") String createUser);

    Map<String, Object> selectTaskExecutionDetails(@Param("taskId") Long taskId);

    int batchInsert(@Param("list") List<DownloadTask> tasks);

    int updateTaskProgress(@Param("taskId") Long taskId, @Param("processedCount") Long processedCount);

    int completeTask(@Param("taskId") Long taskId,
                    @Param("status") Integer status,
                    @Param("fileUrl") String fileUrl,
                    @Param("errorMessage") String errorMessage);

    List<DownloadTask> selectRecentTasks(@Param("limit") Integer limit);

    List<Map<String, Object>> countTasksByStatus();

    List<DownloadTask> selectLongRunningTasks(@Param("minutes") Integer minutes);
}
