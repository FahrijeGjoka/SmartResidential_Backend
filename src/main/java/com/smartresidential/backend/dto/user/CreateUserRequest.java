package com.smartresidential.backend.dto.user;

public class CreateUserRequest {

    private String email;
    private String password;
    private Long roleId;
    private Long tenantId;
    private String firstName;
    private String lastName;

    public CreateUserRequest() {}

    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public Long getRoleId() { return roleId; }
    public Long getTenantId() { return tenantId; }

    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }

    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }

    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
}