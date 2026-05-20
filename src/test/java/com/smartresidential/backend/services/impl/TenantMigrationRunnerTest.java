package com.smartresidential.backend.services.impl;

import com.smartresidential.backend.config.TenantMigrationRunner;
import com.smartresidential.backend.entities.Tenant;
import com.smartresidential.backend.repositories.TenantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TenantMigrationRunnerTest {

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private TenantProvisioningService tenantProvisioningService;

    @Test
    void migratesActiveTenantSchemasOnStartup() {
        Tenant activeTenant = tenant(1L, "tenant_test", true);
        Tenant inactiveTenant = tenant(2L, "tenant_inactive", false);
        Tenant blankSchemaTenant = tenant(3L, " ", true);
        TenantMigrationRunner runner =
                new TenantMigrationRunner(tenantRepository, tenantProvisioningService);

        when(tenantRepository.findAll()).thenReturn(List.of(activeTenant, inactiveTenant, blankSchemaTenant));

        runner.run(null);

        verify(tenantProvisioningService).runTenantMigrations("tenant_test");
        verify(tenantProvisioningService, never()).runTenantMigrations("tenant_inactive");
        verify(tenantProvisioningService, never()).runTenantMigrations(" ");
    }

    private Tenant tenant(Long id, String schemaName, Boolean active) {
        Tenant tenant = new Tenant();
        tenant.setId(id);
        tenant.setName("Tenant " + id);
        tenant.setIdentifier("tenant-" + id);
        tenant.setSchemaName(schemaName);
        tenant.setIsActive(active);
        return tenant;
    }
}
