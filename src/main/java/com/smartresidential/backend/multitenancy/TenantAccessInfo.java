package com.smartresidential.backend.multitenancy;

public record TenantAccessInfo(
        Long tenantId,
        String schemaName,
        String identifier
) {
}