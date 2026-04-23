package com.smartresidential.backend.dto.auth;

public class LoginResponse {

    private String token;
    private String email;
    private String role;
    private String identifier; //tenantIdentifier

    public LoginResponse() {}

    public String getToken() {
        return token;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public String getTenantIdentifier(){return identifier;}

    public void setToken(String token) {
        this.token = token;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public void setTenantIdentifier(String tenantIdentifier) {
        this.identifier = tenantIdentifier;
    }
}