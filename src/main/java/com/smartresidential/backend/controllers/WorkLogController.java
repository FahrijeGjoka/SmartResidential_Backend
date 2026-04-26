package com.smartresidential.backend.controllers;

import com.smartresidential.backend.dto.workLog.CreateWorkLogRequest;
import com.smartresidential.backend.dto.workLog.WorkLogResponseDTO;
import com.smartresidential.backend.services.interfaces.WorkLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/work-logs")
@Tag(name = "Work Logs", description = "Operations related to work logs")
public class WorkLogController {

    private final WorkLogService workLogService;

    public WorkLogController(WorkLogService workLogService) {
        this.workLogService = workLogService;
    }

    @PostMapping
    @Operation(summary = "Create a new work log")
    public WorkLogResponseDTO createWorkLog(@RequestBody CreateWorkLogRequest request) {
        return workLogService.createWorkLog(request);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get work log by ID")
    public WorkLogResponseDTO getWorkLogById(@PathVariable Long id) {
        return workLogService.getWorkLogById(id);
    }

    @GetMapping
    @Operation(summary = "Get all work logs")
    public List<WorkLogResponseDTO> getAllWorkLogs() {
        return workLogService.getAllWorkLogs();
    }

    @GetMapping("/issue/{issueId}")
    @Operation(summary = "Get work logs by issue ID")
    public List<WorkLogResponseDTO> getWorkLogsByIssueId(@PathVariable Long issueId) {
        return workLogService.getWorkLogsByIssueId(issueId);
    }

    @GetMapping("/technician/{technicianId}")
    @Operation(summary = "Get work logs by technician ID")
    public List<WorkLogResponseDTO> getWorkLogsByTechnicianId(@PathVariable Long technicianId) {
        return workLogService.getWorkLogsByTechnicianId(technicianId);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete work log by ID")
    public void deleteWorkLog(@PathVariable Long id) {
        workLogService.deleteWorkLog(id);
    }
}