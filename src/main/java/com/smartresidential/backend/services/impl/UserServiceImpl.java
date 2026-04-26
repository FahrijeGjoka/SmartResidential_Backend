package com.smartresidential.backend.services.impl;

import com.smartresidential.backend.dto.user.CreateUserRequest;
import com.smartresidential.backend.entities.Role;
import com.smartresidential.backend.entities.Tenant;
import com.smartresidential.backend.entities.User;
import com.smartresidential.backend.multitenancy.TenantContext;
import com.smartresidential.backend.repositories.RoleRepository;
import com.smartresidential.backend.repositories.TenantRepository;
import com.smartresidential.backend.repositories.UserRepository;
import com.smartresidential.backend.services.interfaces.UserService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(
            UserRepository userRepository,
            TenantRepository tenantRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public User createUser(CreateUserRequest request) {

        Tenant tenant = tenantRepository.findById(request.getTenantId())
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        try {
            TenantContext.set(
                    tenant.getId(),
                    tenant.getSchemaName(),
                    tenant.getIdentifier(),
                    null,
                    role.getName()
            );

            String schemaName = tenant.getSchemaName();

            entityManager.createNativeQuery("SET search_path TO " + schemaName)
                    .executeUpdate();

            if (userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("User with this email already exists");
            }

            User user = new User();
            user.setEmail(request.getEmail().trim().toLowerCase());
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setRoleId(request.getRoleId());
            user.setIsActive(true);

            return userRepository.save(user);

        } finally {
            TenantContext.clear();
        }
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public List<User> getAllActiveUsers() {
        return userRepository.findAllByIsActiveTrue();
    }

    @Override
    public List<User> getUsersByRoleId(Long roleId) {
        return userRepository.findAllByRoleId(roleId);
    }

    @Override
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public Optional<User> getActiveUserByEmail(String email) {
        return userRepository.findByEmailAndIsActiveTrue(email);
    }

    @Override
    public User updateUser(Long id, User user) {

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        Optional<User> userWithSameEmail = userRepository.findByEmail(user.getEmail());

        if (userWithSameEmail.isPresent() && !userWithSameEmail.get().getId().equals(id)) {
            throw new RuntimeException("Email already in use");
        }

        existingUser.setEmail(user.getEmail());

        if (user.getPasswordHash() != null && !user.getPasswordHash().isBlank()) {
            existingUser.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        }

        existingUser.setFirstName(user.getFirstName());
        existingUser.setLastName(user.getLastName());
        existingUser.setRoleId(user.getRoleId());
        existingUser.setIsActive(user.getIsActive());

        return userRepository.save(existingUser);
    }

    @Override
    public void deleteUser(Long id) {

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        userRepository.delete(existingUser);
    }

    @Override
    public User activateUser(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        user.setIsActive(true);

        return userRepository.save(user);
    }

    @Override
    public User deactivateUser(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        user.setIsActive(false);

        return userRepository.save(user);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}