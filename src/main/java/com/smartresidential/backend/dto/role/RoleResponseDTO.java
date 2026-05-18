package com.smartresidential.backend.dto.role;

public class RoleResponseDTO {

    private Long id;
    private String name;

    public RoleResponseDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
