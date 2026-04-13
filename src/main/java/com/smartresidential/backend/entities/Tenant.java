package com.smartresidential.backend.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="tenants", schema = "public")
public class Tenant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name="schema_name", nullable=false, unique = true)
    private String schemaName;

    @Column(nullable = false, unique = true)
    private String identifier;

    @Column(name="created_at")
    private LocalDateTime createdAt;

    @Column(name="is_active")
    private Boolean isActive = true;

    public Tenant(){

    }

    @PrePersist
    public void prePersist(){
        this.createdAt = LocalDateTime.now();
        if(this.isActive == null){
            this.isActive = true;
        }
    }

    public Long getId(){
        return id;
    }

    public String getName(){
        return name;
    }

    public String getSchemaName(){
        return schemaName;
    }

    public String getIdentifier(){
        return identifier;
    }

    public LocalDateTime getCreatedAt(){
        return createdAt;
    }

    public Boolean getIsActive(){
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

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }
}





