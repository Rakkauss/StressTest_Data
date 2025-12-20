package com.ecommerce.loadtest.entity;

import java.util.Date;

/**
 * 下载任务实体类
 * 管理导出/下载任务
 *
 * @author rakkaus
 */
public class DownloadTask {

    private Long id;
    private String taskName;
    private String fileName;
    private String fileUrl;
    private Integer taskStatus;
    private Long totalCount;
    private Long processedCount;
    private String errorMessage;
    private String createUser;
    private Date createTime;
    private Date completeTime;

    public DownloadTask() {
    }

    public DownloadTask(String taskName, String createUser) {
        this.taskName = taskName;
        this.createUser = createUser;
        this.taskStatus = 0;
        this.totalCount = 0L;
        this.processedCount = 0L;
        this.createTime = new Date();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public Integer getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(Integer taskStatus) {
        this.taskStatus = taskStatus;
    }

    public Long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

    public Long getProcessedCount() {
        return processedCount;
    }

    public void setProcessedCount(Long processedCount) {
        this.processedCount = processedCount;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getCompleteTime() {
        return completeTime;
    }

    public void setCompleteTime(Date completeTime) {
        this.completeTime = completeTime;
    }

    public Double getProgressPercentage() {
        if (totalCount == null || totalCount == 0 || processedCount == null) {
            return 0.0;
        }
        return (processedCount.doubleValue() / totalCount.doubleValue()) * 100;
    }

    public boolean isCompleted() {
        return taskStatus != null && (taskStatus == 1 || taskStatus == 2);
    }

    public boolean isSuccess() {
        return taskStatus != null && taskStatus == 1;
    }

    @Override
    public String toString() {
        return "DownloadTask{" +
                "id=" + id +
                ", taskName='" + taskName + '\'' +
                ", fileName='" + fileName + '\'' +
                ", fileUrl='" + fileUrl + '\'' +
                ", taskStatus=" + taskStatus +
                ", totalCount=" + totalCount +
                ", processedCount=" + processedCount +
                ", errorMessage='" + errorMessage + '\'' +
                ", createUser='" + createUser + '\'' +
                ", createTime=" + createTime +
                ", completeTime=" + completeTime +
                '}';
    }
}
