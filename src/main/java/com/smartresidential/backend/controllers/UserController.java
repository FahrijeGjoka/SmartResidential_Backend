package com.smartresidential.backend.controllers;
import com.smartresidential.backend.dto.user.CreateUserRequest;
import com.smartresidential.backend.entities.Role;
import com.smartresidential.backend.entities.User;
import com.smartresidential.backend.services.interfaces.RoleService;
import com.smartresidential.backend.services.interfaces.UserService;
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

    // 🔹 GET ALL USERS
    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    // 🔹 GET ONLY ACTIVE USERS
    @GetMapping("/active")
    public List<User> getActiveUsers() {
        return userService.getAllActiveUsers();
    }

    // 🔹 GET USER BY ID
    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    // 🔹 GET USERS BY ROLE
    @GetMapping("/role/{roleId}")
    public List<User> getUsersByRole(@PathVariable Long roleId) {
        return userService.getUsersByRoleId(roleId);
    }

    // 🔹 CREATE USER
    @PostMapping
    public User createUser(@RequestBody CreateUserRequest request) {

        Role role = roleService.getRoleById(request.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        User user = new User();
        user.setEmail(request.getEmail());
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPasswordHash(request.getPassword());
        }
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setRole(role);
        user.setIsActive(true);

        return userService.createUser(user);
    }

    // 🔹 UPDATE USER
    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody CreateUserRequest request) {

        Role role = roleService.getRoleById(request.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(request.getPassword());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setRole(role);
        user.setIsActive(true);

        return userService.updateUser(id, user);
    }

    // 🔹 DELETE USER
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    // 🔹 DEACTIVATE USER
    @PatchMapping("/{id}/deactivate")
    public User deactivateUser(@PathVariable Long id) {
        return userService.deactivateUser(id);
    }

    // 🔹 ACTIVATE USER
    @PatchMapping("/{id}/activate")
    public User activateUser(@PathVariable Long id) {
        return userService.activateUser(id);
    }
}