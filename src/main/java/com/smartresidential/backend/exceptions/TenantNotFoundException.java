package com.smartresidential.backend.exceptions;

import org.springframework.http.HttpStatus;

public class TenantNotFoundException extends ApiException {

    public TenantNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
