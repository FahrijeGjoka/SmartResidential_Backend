package com.smartresidential.backend.dto.auth;

public class RegisterRequest {

    private String identifier;
    private String fullName;
    private String email;
    private String password;

    public RegisterRequest() {
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}