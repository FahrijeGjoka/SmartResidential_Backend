package com.smartresidential.backend.services.impl;

import com.smartresidential.backend.entities.Tenant;
import com.smartresidential.backend.repositories.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TenantMigrationRunner implements ApplicationRunner {

    private final TenantRepository tenantRepository;
    private final TenantProvisioningService tenantProvisioningService;

    @Override
    public void run(ApplicationArguments args) {
        tenantRepository.findAll()
                .stream()
                .filter(tenant -> Boolean.TRUE.equals(tenant.getIsActive()))
                .map(Tenant::getSchemaName)
                .filter(schemaName -> schemaName != null && !schemaName.isBlank())
                .forEach(tenantProvisioningService::migrateTenant);
    }
}
