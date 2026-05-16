package com.smartresidential.backend.dto.job;

import java.time.LocalDateTime;

public class JobExecutionResponse {

    private String jobName;
    private String triggerType;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private long durationMs;
    private boolean success;
    private String status;
    private String result;
    private String exceptionMessage;

    public JobExecutionResponse() {
    }

    public JobExecutionResponse(String jobName,
                                String triggerType,
                                LocalDateTime startTime,
                                LocalDateTime endTime,
                                long durationMs,
                                boolean success,
                                String status,
                                String result,
                                String exceptionMessage) {
        this.jobName = jobName;
        this.triggerType = triggerType;
        this.startTime = startTime;
        this.endTime = endTime;
        this.durationMs = durationMs;
        this.success = success;
        this.status = status;
        this.result = result;
        this.exceptionMessage = exceptionMessage;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(String triggerType) {
        this.triggerType = triggerType;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }
}
