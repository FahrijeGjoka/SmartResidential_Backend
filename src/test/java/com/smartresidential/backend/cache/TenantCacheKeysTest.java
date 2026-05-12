package com.smartresidential.backend.cache;

import com.smartresidential.backend.multitenancy.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TenantCacheKeysTest {

    @AfterEach
    void clearTenantContext() {
        TenantContext.clear();
    }

    @Test
    void keyIncludesCurrentTenantSchema() {
        TenantContext.set(1L, "tenant_alpha", "alpha", 10L, "ROLE_ADMIN");

        assertThat(TenantCacheKeys.byId("buildings", 7L))
                .isEqualTo("tenant:tenant_alpha:buildings:id:7");
    }

    @Test
    void keyFallsBackToPublicSchemaWhenNoTenantIsSet() {
        assertThat(TenantCacheKeys.all("issue-categories"))
                .isEqualTo("tenant:public:issue-categories:all");
    }
}
