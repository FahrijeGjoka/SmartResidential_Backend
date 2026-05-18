package com.smartresidential.backend.services.interfaces;

import com.smartresidential.backend.entities.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface RoleService {

    List<Role> getAllRoles();

    Page<Role> getAllRoles(Pageable pageable);

    Optional<Role> getRoleById(Long id);

    Optional<Role> getRoleByName(String name);

    Role createRole(Role role);

    boolean existsByName(String name);
}
