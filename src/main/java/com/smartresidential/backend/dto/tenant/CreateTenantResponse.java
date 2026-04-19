package com.smartresidential.backend.dto.tenant;

public class CreateTenantResponse {

    private Long id;
    private String name;
    private String schemaName;
    private String identifier;
    private Boolean isActive;

    public CreateTenantResponse() {}

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getIdentifier() {
        return identifier;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }
}