package com.smartresidential.backend.cache;

import com.smartresidential.backend.dto.issue.IssueFilterRequest;
import com.smartresidential.backend.multitenancy.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

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

    @Test
    void searchKeyIncludesAllFilterAndPaginationInputs() {
        TenantContext.set(1L, "tenant_alpha", "alpha", 10L, "ROLE_ADMIN");

        IssueFilterRequest filter = new IssueFilterRequest();
        filter.setCreatedById(15L);
        filter.setApartmentId(20L);
        filter.setCategoryId(25L);
        filter.setAssignedTechnicianId(30L);
        filter.setStatus(" OPEN ");
        filter.setPriority(" HIGH ");
        filter.setKeyword(" Leak ");
        filter.setTitle(" Kitchen ");
        filter.setCreatedAfter(LocalDateTime.parse("2026-01-01T10:15:30"));
        filter.setCreatedBefore(LocalDateTime.parse("2026-01-02T10:15:30"));
        filter.setUpdatedAfter(LocalDateTime.parse("2026-01-03T10:15:30"));
        filter.setUpdatedBefore(LocalDateTime.parse("2026-01-04T10:15:30"));
        filter.setPage(2);
        filter.setSize(50);
        filter.setSortBy("updatedAt");
        filter.setSortDirection("ASC");

        assertThat(IssueCacheKeys.search(filter))
                .isEqualTo("tenant:tenant_alpha:role:role_admin:issues:search"
                        + ":created-by:15"
                        + ":apartment:20"
                        + ":category:25"
                        + ":assigned-tech:30"
                        + ":status:open"
                        + ":priority:high"
                        + ":keyword:leak"
                        + ":title:kitchen"
                        + ":created-after:2026-01-01T10:15:30"
                        + ":created-before:2026-01-02T10:15:30"
                        + ":updated-after:2026-01-03T10:15:30"
                        + ":updated-before:2026-01-04T10:15:30"
                        + ":page:2"
                        + ":size:50"
                        + ":sort-by:updatedat"
                        + ":sort-direction:asc");
    }
}
