package com.smartresidential.backend.config;

import com.smartresidential.backend.entities.Tenant;
import com.smartresidential.backend.repositories.TenantRepository;
import com.smartresidential.backend.services.impl.TenantProvisioningService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class TenantMigrationRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(TenantMigrationRunner.class);

    private final TenantRepository tenantRepository;
    private final TenantProvisioningService tenantProvisioningService;

    public TenantMigrationRunner(
            TenantRepository tenantRepository,
            TenantProvisioningService tenantProvisioningService
    ) {
        this.tenantRepository = tenantRepository;
        this.tenantProvisioningService = tenantProvisioningService;
    }

    @Override
    public void run(ApplicationArguments args) {
        for (Tenant tenant : tenantRepository.findAll()) {
            if (!Boolean.TRUE.equals(tenant.getIsActive())) {
                continue;
            }

            String schemaName = tenant.getSchemaName();
            if (schemaName == null || schemaName.isBlank()) {
                continue;
            }

            log.info("Running tenant migrations for schema {}", schemaName);
            tenantProvisioningService.runTenantMigrations(schemaName);
        }
    }
}
