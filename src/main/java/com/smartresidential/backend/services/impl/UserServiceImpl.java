package com.smartresidential.backend.services.impl;

import com.smartresidential.backend.dto.user.CreateUserRequest;
import com.smartresidential.backend.entities.Role;
import com.smartresidential.backend.entities.Tenant;
import com.smartresidential.backend.entities.User;
import com.smartresidential.backend.exceptions.ConflictException;
import com.smartresidential.backend.exceptions.ResourceNotFoundException;
import com.smartresidential.backend.exceptions.TenantNotFoundException;
import com.smartresidential.backend.multitenancy.TenantContext;
import com.smartresidential.backend.repositories.RoleRepository;
import com.smartresidential.backend.repositories.TenantRepository;
import com.smartresidential.backend.repositories.UserRepository;
import com.smartresidential.backend.services.interfaces.AuditLogService;
import com.smartresidential.backend.services.interfaces.UserService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final AuditLogService auditLogService;

    public UserServiceImpl(
            UserRepository userRepository,
            TenantRepository tenantRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            AuditLogService auditLogService
    ) {
        this.userRepository = userRepository;
        this.tenantRepository = tenantRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional
    public User createUser(CreateUserRequest request) {
        Long actingUserId = TenantContext.getUserId();

        Tenant tenant = tenantRepository.findById(request.getTenantId())
                .orElseThrow(() -> new TenantNotFoundException("Tenant not found"));

        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        try {
            TenantContext.set(
                    tenant.getId(),
                    tenant.getSchemaName(),
                    tenant.getIdentifier(),
                    actingUserId,
                    role.getName()
            );

            String schemaName = tenant.getSchemaName();

            entityManager.createNativeQuery("SET search_path TO " + schemaName)
                    .executeUpdate();

            if (userRepository.existsByEmail(request.getEmail())) {
                throw new ConflictException("User with this email already exists");
            }

            User user = new User();
            user.setEmail(request.getEmail().trim().toLowerCase());
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setRoleId(request.getRoleId());
            user.setIsActive(true);

            User savedUser = userRepository.save(user);
            auditLogService.logCurrentUser("USER_CREATED", "USER", savedUser.getId());
            return savedUser;

        } finally {
            TenantContext.clear();
        }
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
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
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        Optional<User> userWithSameEmail = userRepository.findByEmail(user.getEmail());

        if (userWithSameEmail.isPresent() && !userWithSameEmail.get().getId().equals(id)) {
            throw new ConflictException("Email already in use");
        }

        existingUser.setEmail(user.getEmail());

        if (user.getPasswordHash() != null && !user.getPasswordHash().isBlank()) {
            existingUser.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
            existingUser.setTokenVersion(nextTokenVersion(existingUser));
        }

        existingUser.setFirstName(user.getFirstName());
        existingUser.setLastName(user.getLastName());
        existingUser.setRoleId(user.getRoleId());
        existingUser.setIsActive(user.getIsActive());

        User savedUser = userRepository.save(existingUser);
        auditLogService.logCurrentUser("USER_UPDATED", "USER", savedUser.getId());
        return savedUser;
    }

    private int nextTokenVersion(User user) {
        return (user.getTokenVersion() == null ? 0 : user.getTokenVersion()) + 1;
    }

    @Override
    public void deleteUser(Long id) {

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        userRepository.delete(existingUser);
    }

    @Override
    public User activateUser(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        user.setIsActive(true);

        User savedUser = userRepository.save(user);
        auditLogService.logCurrentUser("USER_ACTIVATED", "USER", savedUser.getId());
        return savedUser;
    }

    @Override
    public User deactivateUser(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        user.setIsActive(false);

        User savedUser = userRepository.save(user);
        auditLogService.logCurrentUser("USER_DEACTIVATED", "USER", savedUser.getId());
        return savedUser;
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public User assignStaffRole(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Role role = roleRepository.findByName("ROLE_STAFF")
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: ROLE_STAFF"));

        user.setRoleId(role.getId());

        return userRepository.save(user);
    }

    @Override
    public User assignTechnicianRole(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Role role = roleRepository.findByName("ROLE_TECHNICIAN")
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: ROLE_TECHNICIAN"));

        user.setRoleId(role.getId());

        return userRepository.save(user);
    }
}
