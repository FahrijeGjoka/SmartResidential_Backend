package com.smartresidential.backend.controllers;

import com.smartresidential.backend.dto.dashboard.ResidentDashboardResponse;
import com.smartresidential.backend.services.interfaces.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @PreAuthorize("hasAuthority('ROLE_RESIDENT')")
    @GetMapping("/resident")
    public ResponseEntity<ResidentDashboardResponse> getResidentDashboard() {
        return ResponseEntity.ok(dashboardService.getResidentDashboard());
    }
}
