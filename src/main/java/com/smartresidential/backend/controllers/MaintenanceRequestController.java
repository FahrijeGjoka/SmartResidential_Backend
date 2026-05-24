package com.smartresidential.backend.controllers;

import com.smartresidential.backend.dto.maintenanceRequest.CreateMaintenanceRequestRequest;
import com.smartresidential.backend.dto.maintenanceRequest.MaintenanceRequestResponseDTO;
import com.smartresidential.backend.services.interfaces.MaintenanceRequestService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/maintenance-requests")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF', 'ROLE_TECHNICIAN')")
public class MaintenanceRequestController {

    private final MaintenanceRequestService maintenanceRequestService;

    public MaintenanceRequestController(MaintenanceRequestService maintenanceRequestService) {
        this.maintenanceRequestService = maintenanceRequestService;
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
    public ResponseEntity<MaintenanceRequestResponseDTO> createMaintenanceRequest(
            @Valid @RequestBody CreateMaintenanceRequestRequest request
    ) {
        return ResponseEntity.ok(
                maintenanceRequestService.createMaintenanceRequest(request)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<MaintenanceRequestResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(
                maintenanceRequestService.getMaintenanceRequestById(id)
        );
    }

    @GetMapping
    public ResponseEntity<List<MaintenanceRequestResponseDTO>> getAll() {
        return ResponseEntity.ok(
                maintenanceRequestService.getAllMaintenanceRequests()
        );
    }

    @GetMapping("/exists/{issueId}")
    public ResponseEntity<Boolean> existsByIssue(@PathVariable Long issueId) {
        return ResponseEntity.ok(
                maintenanceRequestService.existsByIssueId(issueId)
        );
    }
}
