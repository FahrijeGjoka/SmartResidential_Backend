package com.smartresidential.backend.dto.job;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class JobStatusResponse {

    private String jobName;
    private String description;
    private String schedule;
    @JsonProperty("isEnabled")
    private boolean isEnabled;
    private LocalDateTime lastRunTime;
    private String lastStatus;
    private String lastResult;
    private String lastErrorMessage;

    public JobStatusResponse() {
    }

    public JobStatusResponse(String jobName,
                             String description,
                             String schedule,
                             boolean isEnabled,
                             LocalDateTime lastRunTime,
                             String lastStatus,
                             String lastResult,
                             String lastErrorMessage) {
        this.jobName = jobName;
        this.description = description;
        this.schedule = schedule;
        this.isEnabled = isEnabled;
        this.lastRunTime = lastRunTime;
        this.lastStatus = lastStatus;
        this.lastResult = lastResult;
        this.lastErrorMessage = lastErrorMessage;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    @JsonProperty("isEnabled")
    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public LocalDateTime getLastRunTime() {
        return lastRunTime;
    }

    public void setLastRunTime(LocalDateTime lastRunTime) {
        this.lastRunTime = lastRunTime;
    }

    public String getLastStatus() {
        return lastStatus;
    }

    public void setLastStatus(String lastStatus) {
        this.lastStatus = lastStatus;
    }

    public String getLastResult() {
        return lastResult;
    }

    public void setLastResult(String lastResult) {
        this.lastResult = lastResult;
    }

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    public void setLastErrorMessage(String lastErrorMessage) {
        this.lastErrorMessage = lastErrorMessage;
    }
}
