package com.smartresidential.backend.controllers;

import com.smartresidential.backend.dto.user.CreateUserRequest;
import com.smartresidential.backend.dto.user.UserResponseDTO;
import com.smartresidential.backend.entities.Role;
import com.smartresidential.backend.entities.User;
import com.smartresidential.backend.exceptions.ResourceNotFoundException;
import com.smartresidential.backend.services.interfaces.RoleService;
import com.smartresidential.backend.services.interfaces.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
public class UserController {

    private final UserService userService;
    private final RoleService roleService;

    public UserController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @GetMapping
    public List<UserResponseDTO> getAllUsers() {
        return userService.getAllUsers().stream().map(this::mapToResponse).toList();
    }

    @GetMapping("/page")
    public Page<UserResponseDTO> getUsersPage(Pageable pageable) {
        return userService.getAllUsers(pageable).map(this::mapToResponse);
    }

    @GetMapping("/active")
    public List<UserResponseDTO> getActiveUsers() {
        return userService.getAllActiveUsers().stream().map(this::mapToResponse).toList();
    }

    @GetMapping("/{id}")
    public UserResponseDTO getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    @GetMapping("/role/{roleId}")
    public List<UserResponseDTO> getUsersByRole(@PathVariable Long roleId) {
        return userService.getUsersByRoleId(roleId).stream().map(this::mapToResponse).toList();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public UserResponseDTO createUser(@RequestBody CreateUserRequest request) {

        Role role = roleService.getRoleById(request.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        User user = new User();
        user.setEmail(request.getEmail());
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPasswordHash(request.getPassword());
        }
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setRoleId(request.getRoleId());
        user.setIsActive(true);

        return mapToResponse(userService.createUser(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public UserResponseDTO updateUser(@PathVariable Long id, @RequestBody CreateUserRequest request) {

        Role role = roleService.getRoleById(request.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(request.getPassword());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setRoleId(request.getRoleId());
        user.setIsActive(true);

        return mapToResponse(userService.updateUser(id, user));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public UserResponseDTO deactivateUser(@PathVariable Long id) {
        return mapToResponse(userService.deactivateUser(id));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public UserResponseDTO activateUser(@PathVariable Long id) {
        return mapToResponse(userService.activateUser(id));
    }

    // ADMIN -> STAFF
    @PatchMapping("/{id}/make-staff")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public UserResponseDTO makeStaff(@PathVariable Long id) {
        return mapToResponse(userService.assignStaffRole(id));
    }

    // ADMIN or STAFF -> TECHNICIAN
    @PatchMapping("/{id}/make-technician")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STAFF')")
    public UserResponseDTO makeTechnician(@PathVariable Long id) {
        return mapToResponse(userService.assignTechnicianRole(id));
    }

    private UserResponseDTO mapToResponse(User user) {
        UserResponseDTO response = new UserResponseDTO();
        response.setId(user.getId());
        response.setRoleId(user.getRoleId());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setIsActive(user.getIsActive());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }
}
