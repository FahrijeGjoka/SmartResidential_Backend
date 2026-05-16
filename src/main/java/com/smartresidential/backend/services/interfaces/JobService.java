package com.smartresidential.backend.services.interfaces;

import com.smartresidential.backend.dto.job.JobExecutionResponse;
import com.smartresidential.backend.dto.job.JobStatusResponse;
import com.smartresidential.backend.dto.job.JobSummaryResponse;

import java.util.List;

public interface JobService {

    List<JobStatusResponse> getAllJobs();

    JobStatusResponse getJob(String jobName);

    JobExecutionResponse runJob(String jobName);

    JobStatusResponse enableJob(String jobName);

    JobStatusResponse disableJob(String jobName);

    JobSummaryResponse getSummary();

    List<JobExecutionResponse> getHistory(String jobName);

    void runScheduledJob(String jobName, Runnable jobRunner);
}
