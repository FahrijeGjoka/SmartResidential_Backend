package com.smartresidential.backend.services.impl;

import com.smartresidential.backend.dto.tenant.CreateTenantRequest;
import com.smartresidential.backend.dto.tenant.CreateTenantResponse;
import com.smartresidential.backend.entities.Tenant;
import com.smartresidential.backend.repositories.TenantRepository;
import org.springframework.stereotype.Service;

@Service
public class TenantService {

    private final TenantRepository tenantRepository;
    private final TenantProvisioningService tenantProvisioningService;

    public TenantService(TenantRepository tenantRepository,
                         TenantProvisioningService tenantProvisioningService) {
        this.tenantRepository = tenantRepository;
        this.tenantProvisioningService = tenantProvisioningService;
    }

    public CreateTenantResponse createTenant(CreateTenantRequest request) {
        validateRequest(request);

        String name = request.getName().trim();
        String identifier = request.getIdentifier().trim();
        String schemaName = request.getSchemaName().trim();

        if (tenantRepository.existsByIdentifier(identifier)) {
            throw new IllegalArgumentException("Tenant identifier already exists.");
        }

        if (tenantRepository.existsBySchemaName(schemaName)) {
            throw new IllegalArgumentException("Schema name already exists.");
        }

        Tenant tenant = new Tenant();
        tenant.setName(name);
        tenant.setIdentifier(identifier);
        tenant.setSchemaName(schemaName);
        tenant.setIsActive(true);

        Tenant savedTenant = tenantRepository.saveAndFlush(tenant);

        tenantProvisioningService.provisionTenant(savedTenant.getSchemaName());

        CreateTenantResponse response = new CreateTenantResponse();
        response.setId(savedTenant.getId());
        response.setName(savedTenant.getName());
        response.setIdentifier(savedTenant.getIdentifier());
        response.setSchemaName(savedTenant.getSchemaName());
        response.setIsActive(savedTenant.getIsActive());

        return response;
    }

    private void validateRequest(CreateTenantRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request must not be null.");
        }

        if (request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("Tenant name is required.");
        }

        if (request.getIdentifier() == null || request.getIdentifier().isBlank()) {
            throw new IllegalArgumentException("Tenant identifier is required.");
        }

        if (request.getSchemaName() == null || request.getSchemaName().isBlank()) {
            throw new IllegalArgumentException("Schema name is required.");
        }
    }
}