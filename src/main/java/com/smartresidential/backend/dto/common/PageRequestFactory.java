package com.smartresidential.backend.dto.common;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

public final class PageRequestFactory {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 100;
    private static final String DEFAULT_DIRECTION = "desc";

    private PageRequestFactory() {
    }

    public static PageRequest from(BaseFilterRequest filter, String defaultSortBy) {
        int page = filter.getPage() == null || filter.getPage() < 0
                ? DEFAULT_PAGE
                : filter.getPage();
        int size = filter.getSize() == null || filter.getSize() < 1
                ? DEFAULT_SIZE
                : Math.min(filter.getSize(), MAX_SIZE);
        String sortBy = filter.getSortBy() == null || filter.getSortBy().isBlank()
                ? defaultSortBy
                : filter.getSortBy();
        Sort.Direction direction = DEFAULT_DIRECTION.equalsIgnoreCase(filter.getSortDirection())
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        return PageRequest.of(page, size, Sort.by(direction, sortBy));
    }
}
