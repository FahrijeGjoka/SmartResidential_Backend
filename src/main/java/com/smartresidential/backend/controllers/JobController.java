package com.smartresidential.backend.controllers;

import com.smartresidential.backend.dto.job.JobExecutionResponse;
import com.smartresidential.backend.dto.job.JobStatusResponse;
import com.smartresidential.backend.dto.job.JobSummaryResponse;
import com.smartresidential.backend.services.interfaces.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@Tag(name = "Background Jobs", description = "Monitor and manage SmartResidential background jobs")
@SecurityRequirement(name = "bearerAuth")
@SecurityRequirement(name = "X-Tenant-Identifier")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
    @Operation(summary = "List all background jobs")
    public ResponseEntity<List<JobStatusResponse>> getJobs() {
        return ResponseEntity.ok(jobService.getAllJobs());
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
    @Operation(summary = "Get background job summary")
    public ResponseEntity<JobSummaryResponse> getSummary() {
        return ResponseEntity.ok(jobService.getSummary());
    }

    @GetMapping("/{jobName}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
    @Operation(summary = "Get background job details")
    public ResponseEntity<JobStatusResponse> getJob(@PathVariable String jobName) {
        return ResponseEntity.ok(jobService.getJob(jobName));
    }

    @GetMapping("/history/{jobName}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
    @Operation(summary = "Get background job execution history")
    public ResponseEntity<List<JobExecutionResponse>> getJobHistory(@PathVariable String jobName) {
        return ResponseEntity.ok(jobService.getHistory(jobName));
    }

    @PostMapping("/{jobName}/run")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Manually trigger a background job")
    public ResponseEntity<JobExecutionResponse> runJob(@PathVariable String jobName) {
        return ResponseEntity.ok(jobService.runJob(jobName));
    }

    @PatchMapping("/{jobName}/enable")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Enable a background job")
    public ResponseEntity<JobStatusResponse> enableJob(@PathVariable String jobName) {
        return ResponseEntity.ok(jobService.enableJob(jobName));
    }

    @PatchMapping("/{jobName}/disable")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @Operation(summary = "Disable a background job")
    public ResponseEntity<JobStatusResponse> disableJob(@PathVariable String jobName) {
        return ResponseEntity.ok(jobService.disableJob(jobName));
    }
}
