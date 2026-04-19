package com.smartresidential.backend.dto.tenant;

public class CreateTenantRequest {
    private String name;
    private String schemaName;
    private String identifier;

    public CreateTenantRequest(){}

    public String getName(){
        return name;
    }

    public String getSchemaName(){
        return schemaName;
    }

    public String getIdentifier(){
        return identifier;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setIdentifier(String identifier){
        this.identifier = identifier;
    }

    public void setSchemaName(String schemaName){
        this.schemaName = schemaName;
    }
}
