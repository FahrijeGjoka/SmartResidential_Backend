package com.smartresidential.backend.dto.auth;

public class LoginResponse {

    private String token;
    private String type;
    private String tenantIdentifier;

    public LoginResponse() {
    }

    public LoginResponse(String token, String tenantIdentifier) {
        this.token = token;
        this.type = "Bearer";
        this.tenantIdentifier = tenantIdentifier;
    }

    public String getToken() {
        return token;
    }

    public String getType() {
        return type;
    }

    public String getTenantIdentifier() {
        return tenantIdentifier;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setTenantIdentifier(String tenantIdentifier) {
        this.tenantIdentifier = tenantIdentifier;
    }
}