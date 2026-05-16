package com.smartresidential.backend.dto.job;

public class JobSummaryResponse {

    private long totalJobs;
    private long enabledJobs;
    private long disabledJobs;
    private long successfulExecutions;
    private long failedExecutions;

    public JobSummaryResponse() {
    }

    public JobSummaryResponse(long totalJobs,
                              long enabledJobs,
                              long disabledJobs,
                              long successfulExecutions,
                              long failedExecutions) {
        this.totalJobs = totalJobs;
        this.enabledJobs = enabledJobs;
        this.disabledJobs = disabledJobs;
        this.successfulExecutions = successfulExecutions;
        this.failedExecutions = failedExecutions;
    }

    public long getTotalJobs() {
        return totalJobs;
    }

    public void setTotalJobs(long totalJobs) {
        this.totalJobs = totalJobs;
    }

    public long getEnabledJobs() {
        return enabledJobs;
    }

    public void setEnabledJobs(long enabledJobs) {
        this.enabledJobs = enabledJobs;
    }

    public long getDisabledJobs() {
        return disabledJobs;
    }

    public void setDisabledJobs(long disabledJobs) {
        this.disabledJobs = disabledJobs;
    }

    public long getSuccessfulExecutions() {
        return successfulExecutions;
    }

    public void setSuccessfulExecutions(long successfulExecutions) {
        this.successfulExecutions = successfulExecutions;
    }

    public long getFailedExecutions() {
        return failedExecutions;
    }

    public void setFailedExecutions(long failedExecutions) {
        this.failedExecutions = failedExecutions;
    }
}
