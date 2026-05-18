package com.smartresidential.backend.controllers;
import com.smartresidential.backend.dto.role.RoleResponseDTO;
import com.smartresidential.backend.exceptions.ResourceNotFoundException;
import com.smartresidential.backend.entities.Role;
import com.smartresidential.backend.services.interfaces.RoleService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("/api/roles")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public List<RoleResponseDTO> getAllRoles() {
        return roleService.getAllRoles().stream().map(this::mapToResponse).toList();
    }

    @GetMapping("/page")
    public Page<RoleResponseDTO> getRolesPage(Pageable pageable) {
        return roleService.getAllRoles(pageable).map(this::mapToResponse);
    }

    @GetMapping("/{id}")
    public RoleResponseDTO getRoleById(@PathVariable Long id) {
        return roleService.getRoleById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));
    }

    @GetMapping("/name/{name}")
    public RoleResponseDTO getRoleByName(@PathVariable String name) {
        return roleService.getRoleByName(name)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public RoleResponseDTO createRole(@RequestBody Role role) {
        return mapToResponse(roleService.createRole(role));
    }

    private RoleResponseDTO mapToResponse(Role role) {
        RoleResponseDTO response = new RoleResponseDTO();
        response.setId(role.getId());
        response.setName(role.getName());
        return response;
    }
}
