package com.smartresidential.backend.controllers;

import com.smartresidential.backend.dto.user.CreateUserRequest;
import com.smartresidential.backend.entities.Role;
import com.smartresidential.backend.entities.User;
import com.smartresidential.backend.exceptions.ResourceNotFoundException;
import com.smartresidential.backend.services.interfaces.RoleService;
import com.smartresidential.backend.services.interfaces.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final RoleService roleService;

    public UserController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/active")
    public List<User> getActiveUsers() {
        return userService.getAllActiveUsers();
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    @GetMapping("/role/{roleId}")
    public List<User> getUsersByRole(@PathVariable Long roleId) {
        return userService.getUsersByRoleId(roleId);
    }

    @PostMapping
    public User createUser(@RequestBody CreateUserRequest request) {

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

        return userService.createUser(request);
    }

    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody CreateUserRequest request) {

        Role role = roleService.getRoleById(request.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(request.getPassword());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setRoleId(request.getRoleId());
        user.setIsActive(true);

        return userService.updateUser(id, user);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    @PatchMapping("/{id}/deactivate")
    public User deactivateUser(@PathVariable Long id) {
        return userService.deactivateUser(id);
    }

    @PatchMapping("/{id}/activate")
    public User activateUser(@PathVariable Long id) {
        return userService.activateUser(id);
    }

    // ADMIN -> STAFF
    @PatchMapping("/{id}/make-staff")
    @PreAuthorize("hasRole('ADMIN')")
    public User makeStaff(@PathVariable Long id) {
        return userService.assignStaffRole(id);
    }

    // ADMIN or STAFF -> TECHNICIAN
    @PatchMapping("/{id}/make-technician")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public User makeTechnician(@PathVariable Long id) {
        return userService.assignTechnicianRole(id);
    }
}
