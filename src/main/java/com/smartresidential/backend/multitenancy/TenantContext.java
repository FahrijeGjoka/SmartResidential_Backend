package com.smartresidential.backend.multitenancy;

import java.util.Objects;

public final class TenantContext {

    private static final ThreadLocal<TenantInfo> CURRENT = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void set(TenantInfo tenantInfo) {
        if (tenantInfo == null) {
            throw new IllegalArgumentException("TenantInfo must not be null.");
        }

        if (tenantInfo.schemaName() == null || tenantInfo.schemaName().isBlank()) {
            throw new IllegalArgumentException("Schema name must not be null or blank.");
        }

        CURRENT.set(tenantInfo);
    }

    public static void set(Long tenantId,
                           String schemaName,
                           String identifier,
                           Long userId,
                           String roleName) {
        set(new TenantInfo(tenantId, schemaName, identifier, userId, roleName));
    }

    public static TenantInfo get() {
        TenantInfo info = CURRENT.get();
        if (info == null) {
            return TenantInfo.publicTenant();
        }
        return info;
    }

    public static Long getTenantId() {
        return get().tenantId();
    }

    public static String getSchemaName() {
        return get().schemaName();
    }

    public static String getIdentifier() {
        return get().identifier();
    }

    public static Long getUserId() {
        return get().userId();
    }

    public static String getRoleName() {
        return get().roleName();
    }

    public static boolean hasTenant() {
        return CURRENT.get() != null;
    }

    public static boolean isPublicSchema() {
        return "public".equalsIgnoreCase(getSchemaName());
    }

    public static void clear() {
        CURRENT.remove();
    }

    public record TenantInfo(
            Long tenantId,
            String schemaName,
            String identifier,
            Long userId,
            String roleName
    ) {
        public TenantInfo {
            if (schemaName == null || schemaName.isBlank()) {
                schemaName = "public";
            }
        }

        public static TenantInfo publicTenant() {
            return new TenantInfo(null, "public", null, null, null);
        }

        public boolean isPublic() {
            return Objects.equals("public", schemaName);
        }
    }
}