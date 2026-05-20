package com.smartresidential.backend.services.impl;

import com.smartresidential.backend.dto.job.JobExecutionResponse;
import com.smartresidential.backend.dto.job.JobStatusResponse;
import com.smartresidential.backend.dto.job.JobSummaryResponse;
import com.smartresidential.backend.jobs.AIClassificationLogCleanupJob;
import com.smartresidential.backend.jobs.AuditLogCleanupJob;
import com.smartresidential.backend.jobs.IssueEscalationJob;
import com.smartresidential.backend.jobs.MaintenanceRequestEscalationJob;
import com.smartresidential.backend.jobs.NotificationCleanupJob;
import com.smartresidential.backend.jobs.VerificationTokenCleanupJob;
import com.smartresidential.backend.multitenancy.TenantContext;
import com.smartresidential.backend.services.interfaces.JobService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
public class JobServiceImpl implements JobService {

    private static final String STATUS_NEVER_RUN = "NEVER_RUN";
    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_FAILED = "FAILED";
    private static final String TRIGGER_MANUAL = "MANUAL";
    private static final String TRIGGER_SCHEDULED = "SCHEDULED";
    private static final int MAX_HISTORY_PER_JOB = 100;

    private final Map<String, JobDefinition> jobs = new LinkedHashMap<>();
    private final Map<String, Boolean> enabledJobs = new ConcurrentHashMap<>();
    private final Map<String, CopyOnWriteArrayList<JobExecutionResponse>> history = new ConcurrentHashMap<>();

    @PersistenceContext
    private EntityManager entityManager;

    public JobServiceImpl(
            ObjectProvider<AIClassificationLogCleanupJob> aiClassificationLogCleanupJob,
            ObjectProvider<AuditLogCleanupJob> auditLogCleanupJob,
            ObjectProvider<IssueEscalationJob> issueEscalationJob,
            ObjectProvider<MaintenanceRequestEscalationJob> maintenanceRequestEscalationJob,
            ObjectProvider<NotificationCleanupJob> notificationCleanupJob,
            ObjectProvider<VerificationTokenCleanupJob> verificationTokenCleanupJob
    ) {
        register(new JobDefinition(
                "AIClassificationLogCleanupJob",
                "Deletes AI classification logs older than 90 days.",
                "0 30 4 * * *",
                () -> aiClassificationLogCleanupJob.getObject().executeNow()
        ));
        register(new JobDefinition(
                "AuditLogCleanupJob",
                "Deletes audit logs older than 180 days.",
                "0 0 4 * * *",
                () -> auditLogCleanupJob.getObject().executeNow()
        ));
        register(new JobDefinition(
                "IssueEscalationJob",
                "Finds old open issues that need escalation.",
                "0 0 */6 * * *",
                () -> issueEscalationJob.getObject().executeNow()
        ));
        register(new JobDefinition(
                "MaintenanceRequestEscalationJob",
                "Finds old maintenance requests and sends escalation notifications.",
                "0 0 */8 * * *",
                () -> maintenanceRequestEscalationJob.getObject().executeNow()
        ));
        register(new JobDefinition(
                "NotificationCleanupJob",
                "Deletes read notifications older than 30 days.",
                "0 0 3 * * *",
                () -> notificationCleanupJob.getObject().executeNow()
        ));
        register(new JobDefinition(
                "NotificationJob",
                "Async notification dispatcher used by issue and maintenance workflows.",
                "Async, event-driven",
                null
        ));
        register(new JobDefinition(
                "VerificationTokenCleanupJob",
                "Deletes expired and used email verification tokens.",
                "0 0 2 * * *",
                () -> verificationTokenCleanupJob.getObject().executeNow()
        ));
    }

    @Override
    public List<JobStatusResponse> getAllJobs() {
        return jobs.keySet().stream()
                .map(this::getJob)
                .sorted(Comparator.comparing(JobStatusResponse::getJobName))
                .toList();
    }

    @Override
    public JobStatusResponse getJob(String jobName) {
        JobDefinition job = getJobDefinition(jobName);
        JobExecutionResponse lastExecution = getLastExecution(job.name());

        return new JobStatusResponse(
                job.name(),
                job.description(),
                job.schedule(),
                enabledJobs.getOrDefault(job.name(), true),
                lastExecution == null ? null : lastExecution.getStartTime(),
                lastExecution == null ? STATUS_NEVER_RUN : lastExecution.getStatus(),
                lastExecution == null ? null : lastExecution.getResult(),
                lastExecution == null ? null : lastExecution.getExceptionMessage()
        );
    }

    @Override
    @Transactional
    public JobExecutionResponse runJob(String jobName) {
        JobDefinition job = getJobDefinition(jobName);
        if (job.manualRunner() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Manual trigger is not supported for job: " + job.name()
            );
        }

        if (!enabledJobs.getOrDefault(job.name(), true)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Job is disabled: " + job.name()
            );
        }

        log.info("Manual background job trigger requested: {}", job.name());
        return execute(job.name(), TRIGGER_MANUAL, job.manualRunner());
    }

    @Override
    public JobStatusResponse enableJob(String jobName) {
        JobDefinition job = getJobDefinition(jobName);
        enabledJobs.put(job.name(), true);
        log.info("Background job enabled: {}", job.name());
        return getJob(job.name());
    }

    @Override
    public JobStatusResponse disableJob(String jobName) {
        JobDefinition job = getJobDefinition(jobName);
        enabledJobs.put(job.name(), false);
        log.info("Background job disabled: {}", job.name());
        return getJob(job.name());
    }

    @Override
    public JobSummaryResponse getSummary() {
        long enabledCount = jobs.keySet().stream()
                .filter(jobName -> enabledJobs.getOrDefault(jobName, true))
                .count();
        long successCount = history.values().stream()
                .flatMap(List::stream)
                .filter(JobExecutionResponse::isSuccess)
                .count();
        long failedCount = history.values().stream()
                .flatMap(List::stream)
                .filter(execution -> !execution.isSuccess())
                .count();

        return new JobSummaryResponse(
                jobs.size(),
                enabledCount,
                jobs.size() - enabledCount,
                successCount,
                failedCount
        );
    }

    @Override
    public List<JobExecutionResponse> getHistory(String jobName) {
        JobDefinition job = getJobDefinition(jobName);
        return List.copyOf(history.getOrDefault(job.name(), new CopyOnWriteArrayList<>()));
    }

    @Override
    @Transactional
    public void runScheduledJob(String jobName, Runnable jobRunner) {
        JobDefinition job = getJobDefinition(jobName);
        if (!enabledJobs.getOrDefault(job.name(), true)) {
            log.info("Skipping disabled background job: {}", job.name());
            return;
        }

        execute(job.name(), TRIGGER_SCHEDULED, jobRunner);
    }

    private void register(JobDefinition job) {
        jobs.put(job.name(), job);
        enabledJobs.put(job.name(), true);
        history.put(job.name(), new CopyOnWriteArrayList<>());
    }

    private JobDefinition getJobDefinition(String jobName) {
        if (jobName == null || jobName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Job name is required.");
        }

        JobDefinition job = jobs.get(jobName.trim());
        if (job == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found: " + jobName);
        }

        return job;
    }

    private JobExecutionResponse getLastExecution(String jobName) {
        List<JobExecutionResponse> executions = history.getOrDefault(jobName, new CopyOnWriteArrayList<>());
        if (executions.isEmpty()) {
            return null;
        }
        return executions.get(0);
    }

    private JobExecutionResponse execute(String jobName, String triggerType, Runnable jobRunner) {
        LocalDateTime startTime = LocalDateTime.now();
        boolean hadTenant = TenantContext.hasTenant();
        TenantContext.TenantInfo tenantInfo = TenantContext.get();

        try {
            TenantContext.set(tenantInfo);
            setSearchPath(tenantInfo.schemaName());
            jobRunner.run();

            JobExecutionResponse response = buildExecution(
                    jobName,
                    triggerType,
                    startTime,
                    true,
                    "Completed successfully.",
                    null
            );
            saveExecution(response);
            log.info("Background job completed: {} trigger={} durationMs={}",
                    jobName,
                    triggerType,
                    response.getDurationMs()
            );
            return response;
        } catch (RuntimeException e) {
            JobExecutionResponse response = buildExecution(
                    jobName,
                    triggerType,
                    startTime,
                    false,
                    "Failed.",
                    e.getMessage()
            );
            saveExecution(response);
            log.error("Background job failed: {} trigger={} durationMs={} error={}",
                    jobName,
                    triggerType,
                    response.getDurationMs(),
                    e.getMessage(),
                    e
            );
            throw e;
        } finally {
            if (hadTenant) {
                TenantContext.set(tenantInfo);
            } else {
                TenantContext.clear();
            }
        }
    }

    private void setSearchPath(String schemaName) {
        if (schemaName == null || schemaName.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tenant schema is required to run jobs.");
        }

        entityManager.createNativeQuery(
                "SET search_path TO " + quoteIdentifier(schemaName)
        ).executeUpdate();
    }

    private String quoteIdentifier(String identifier) {
        return "\"" + identifier.replace("\"", "\"\"") + "\"";
    }

    private JobExecutionResponse buildExecution(String jobName,
                                                String triggerType,
                                                LocalDateTime startTime,
                                                boolean success,
                                                String result,
                                                String exceptionMessage) {
        LocalDateTime endTime = LocalDateTime.now();
        long durationMs = Duration.between(startTime, endTime).toMillis();

        return new JobExecutionResponse(
                jobName,
                triggerType,
                startTime,
                endTime,
                durationMs,
                success,
                success ? STATUS_SUCCESS : STATUS_FAILED,
                result,
                exceptionMessage
        );
    }

    private void saveExecution(JobExecutionResponse response) {
        CopyOnWriteArrayList<JobExecutionResponse> executions =
                history.computeIfAbsent(response.getJobName(), ignored -> new CopyOnWriteArrayList<>());
        executions.add(0, response);

        if (executions.size() > MAX_HISTORY_PER_JOB) {
            List<JobExecutionResponse> trimmed = new ArrayList<>(executions.subList(0, MAX_HISTORY_PER_JOB));
            executions.clear();
            executions.addAll(trimmed);
        }
    }

    private record JobDefinition(
            String name,
            String description,
            String schedule,
            Runnable manualRunner
    ) {
    }
}
