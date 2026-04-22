package com.smartresidential.backend.services;

import com.smartresidential.backend.entities.Role;

import java.util.List;
import java.util.Optional;

public interface RoleService {

    List<Role> getAllRoles();

    Optional<Role> getRoleById(Long id);

    Optional<Role> getRoleByName(String name);

    Role createRole(Role role);

    boolean existsByName(String name);
}