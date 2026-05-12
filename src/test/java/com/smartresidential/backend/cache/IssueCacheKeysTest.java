package com.smartresidential.backend.cache;

import com.smartresidential.backend.multitenancy.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IssueCacheKeysTest {

    @AfterEach
    void clearTenantContext() {
        TenantContext.clear();
    }

    @Test
    void issueQueryKeysIncludeTenantSchemaAndRole() {
        TenantContext.set(1L, "tenant_alpha", "alpha", 10L, "ROLE_STAFF");

        assertThat(IssueCacheKeys.byStatus(" OPEN "))
                .isEqualTo("tenant:tenant_alpha:role:role_staff:issues:status:open");
    }

    @Test
    void issueQueryKeysNormalizeMissingRole() {
        TenantContext.set(1L, "tenant_alpha", "alpha", 10L, null);

        assertThat(IssueCacheKeys.all())
                .isEqualTo("tenant:tenant_alpha:role:none:issues:all");
    }
}
