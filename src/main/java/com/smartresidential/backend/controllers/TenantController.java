package com.smartresidential.backend.controllers;

import com.smartresidential.backend.dto.tenant.CreateTenantRequest;
import com.smartresidential.backend.dto.tenant.CreateTenantResponse;
import com.smartresidential.backend.services.TenantService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tenants")
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @PostMapping
    public ResponseEntity<CreateTenantResponse> createTenant(@RequestBody CreateTenantRequest request) {
        CreateTenantResponse response = tenantService.createTenant(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}