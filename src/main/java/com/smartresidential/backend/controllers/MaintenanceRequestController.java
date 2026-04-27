package com.smartresidential.backend.controllers;

import com.smartresidential.backend.dto.maintenanceRequest.CreateMaintenanceRequestRequest;
import com.smartresidential.backend.dto.maintenanceRequest.MaintenanceRequestResponseDTO;
import com.smartresidential.backend.services.interfaces.MaintenanceRequestService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/maintenance-requests")
public class MaintenanceRequestController {

    private final MaintenanceRequestService maintenanceRequestService;

    public MaintenanceRequestController(MaintenanceRequestService maintenanceRequestService) {
        this.maintenanceRequestService = maintenanceRequestService;
    }

    // ✅ CREATE
    @PostMapping
    public ResponseEntity<MaintenanceRequestResponseDTO> createMaintenanceRequest(
            @RequestBody CreateMaintenanceRequestRequest request
    ) {
        return ResponseEntity.ok(
                maintenanceRequestService.createMaintenanceRequest(request)
        );
    }

    // ✅ GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<MaintenanceRequestResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(
                maintenanceRequestService.getMaintenanceRequestById(id)
        );
    }

    // ✅ GET ALL
    @GetMapping
    public ResponseEntity<List<MaintenanceRequestResponseDTO>> getAll() {
        return ResponseEntity.ok(
                maintenanceRequestService.getAllMaintenanceRequests()
        );
    }

    // ✅ CHECK IF EXISTS BY ISSUE
    @GetMapping("/exists/{issueId}")
    public ResponseEntity<Boolean> existsByIssue(@PathVariable Long issueId) {
        return ResponseEntity.ok(
                maintenanceRequestService.existsByIssueId(issueId)
        );
    }
}