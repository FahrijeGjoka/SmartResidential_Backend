package com.smartresidential.backend.cache;

import com.smartresidential.backend.dto.issue.IssueFilterRequest;
import com.smartresidential.backend.multitenancy.TenantContext;

import java.time.LocalDateTime;

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

    public static String search(IssueFilterRequest filter) {
        if (filter == null) {
            return prefix() + ":search"
                    + ":created-by:null"
                    + ":apartment:null"
                    + ":category:null"
                    + ":assigned-tech:null"
                    + ":status:none"
                    + ":priority:none"
                    + ":keyword:none"
                    + ":title:none"
                    + ":created-after:null"
                    + ":created-before:null"
                    + ":updated-after:null"
                    + ":updated-before:null"
                    + ":page:0"
                    + ":size:20"
                    + ":sort-by:createdat"
                    + ":sort-direction:desc";
        }

        return prefix() + ":search"
                + ":created-by:" + filter.getCreatedById()
                + ":apartment:" + filter.getApartmentId()
                + ":category:" + filter.getCategoryId()
                + ":assigned-tech:" + filter.getAssignedTechnicianId()
                + ":status:" + normalize(filter.getStatus())
                + ":priority:" + normalize(filter.getPriority())
                + ":keyword:" + normalize(filter.getKeyword())
                + ":title:" + normalize(filter.getTitle())
                + ":created-after:" + format(filter.getCreatedAfter())
                + ":created-before:" + format(filter.getCreatedBefore())
                + ":updated-after:" + format(filter.getUpdatedAfter())
                + ":updated-before:" + format(filter.getUpdatedBefore())
                + ":page:" + filter.getPage()
                + ":size:" + filter.getSize()
                + ":sort-by:" + normalize(filter.getSortBy())
                + ":sort-direction:" + normalize(filter.getSortDirection());
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

    private static String format(LocalDateTime value) {
        return value != null ? value.toString() : "null";
    }
}
