package com.smartresidential.backend.dto.common;

import lombok.Data;

@Data
public class BaseFilterRequest {

    private Integer page = 0;

    private Integer size = 10;

    private String sortBy = "createdAt";

    private String sortDirection = "desc";
}