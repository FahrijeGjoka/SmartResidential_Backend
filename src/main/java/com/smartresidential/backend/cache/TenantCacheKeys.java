package com.smartresidential.backend.cache;

import com.smartresidential.backend.multitenancy.TenantContext;

public final class TenantCacheKeys {

    private TenantCacheKeys() {
    }

    public static String all(String resource) {
        return tenantPrefix() + ":" + resource + ":all";
    }

    public static String byId(String resource, Long id) {
        return tenantPrefix() + ":" + resource + ":id:" + id;
    }

    public static String byBuildingId(String resource, Long buildingId) {
        return tenantPrefix() + ":" + resource + ":building:" + buildingId;
    }

    private static String tenantPrefix() {
        return "tenant:" + TenantContext.getSchemaName();
    }
}
