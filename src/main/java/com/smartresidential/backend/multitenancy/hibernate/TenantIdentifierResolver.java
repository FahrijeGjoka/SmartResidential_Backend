package com.smartresidential.backend.multitenancy.hibernate;

import com.smartresidential.backend.multitenancy.TenantContext;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;

@Component
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver<String> {

    private static final String DEFAULT_SCHEMA = "public";

    @Override
    public String resolveCurrentTenantIdentifier() {
        String schema = TenantContext.getSchemaName();
        if (schema == null || schema.isBlank()) {
            return DEFAULT_SCHEMA;
        }
        return schema;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}