package com.smartresidential.backend.cache;

import com.smartresidential.backend.multitenancy.TenantContext;

public final class IssueCacheKeys {

    private static final String RESOURCE = "issues";

    private IssueCacheKeys() {
    }

    public static String all() {
        return prefix() + ":all";
    }

    public static String byStatus(String status) {
        return prefix() + ":status:" + normalize(status);
    }

    public static String byPriority(String priority) {
        return prefix() + ":priority:" + normalize(priority);
    }

    public static String byCategory(Long categoryId) {
        return prefix() + ":category:" + categoryId;
    }

    public static String byApartment(Long apartmentId) {
        return prefix() + ":apartment:" + apartmentId;
    }

    public static String byCreatedBy(Long userId) {
        return prefix() + ":created-by:" + userId;
    }

    public static String byTitle(String title) {
        return prefix() + ":title:" + normalize(title);
    }

    private static String prefix() {
        return "tenant:" + TenantContext.getSchemaName()
                + ":role:" + normalize(TenantContext.getRoleName())
                + ":" + RESOURCE;
    }

    private static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return "none";
        }
        return value.trim().toLowerCase();
    }
}
