package com.smartresidential.backend.services.impl;

import com.smartresidential.backend.dto.auth.LoginRequest;
import com.smartresidential.backend.dto.auth.LoginResponse;
import com.smartresidential.backend.dto.auth.RegisterRequest;
import com.smartresidential.backend.entities.Role;
import com.smartresidential.backend.entities.Tenant;
import com.smartresidential.backend.entities.User;
import com.smartresidential.backend.entities.VerificationToken;
import com.smartresidential.backend.multitenancy.TenantContext;
import com.smartresidential.backend.repositories.RoleRepository;
import com.smartresidential.backend.repositories.TenantRepository;
import com.smartresidential.backend.repositories.UserRepository;
import com.smartresidential.backend.repositories.VerificationTokenRepository;
import com.smartresidential.backend.services.interfaces.AuthService;
import com.smartresidential.backend.services.interfaces.EmailService;
import com.smartresidential.backend.services.interfaces.JwtService;
import jakarta.transaction.Transactional;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private static final String DEFAULT_ROLE_NAME = "ROLE_RESIDENT";

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final EmailService emailService;

    public AuthServiceImpl(TenantRepository tenantRepository,
                           UserRepository userRepository,
                           VerificationTokenRepository verificationTokenRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager,
                           JwtService jwtService,
                           EmailService emailService) {
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        validateRegisterRequest(request);

        Tenant tenant = tenantRepository.findByIdentifier(request.getIdentifier().trim())
                .orElseThrow(() -> new RuntimeException("Tenant not found."));

        if (Boolean.FALSE.equals(tenant.getIsActive())) {
            throw new RuntimeException("Tenant is inactive.");
        }

        TenantContext.set(
                tenant.getId(),
                tenant.getSchemaName(),
                tenant.getIdentifier(),
                null,
                null
        );

        try {
            String normalizedEmail = request.getEmail().trim().toLowerCase();

            if (userRepository.existsByEmail(normalizedEmail)) {
                throw new RuntimeException("Email already exists.");
            }

            Role defaultRole = roleRepository.findByName(DEFAULT_ROLE_NAME)
                    .orElseThrow(() -> new RuntimeException("Default role not found: " + DEFAULT_ROLE_NAME));

            User user = new User();

            String fullName = request.getFullName().trim();
            String[] names = fullName.split("\\s+", 2);

            user.setFirstName(names[0]);
            user.setLastName(names.length > 1 ? names[1] : "");
            user.setEmail(normalizedEmail);
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
            user.setIsActive(false);
            user.setRole(defaultRole);

            User savedUser = userRepository.save(user);

            String tokenValue = UUID.randomUUID().toString();

            VerificationToken verificationToken = new VerificationToken();
            verificationToken.setToken(tokenValue);
            verificationToken.setUser(savedUser);
            verificationToken.setExpiryDate(LocalDateTime.now().plusHours(24));
            verificationToken.setUsed(false);

            verificationTokenRepository.save(verificationToken);

            String verificationLink =
                    "http://localhost:8080/api/auth/verify?identifier="
                            + tenant.getIdentifier()
                            + "&token="
                            + tokenValue;

            emailService.sendEmail(
                    savedUser.getEmail(),
                    "Verify your email",
                    "Click the link to verify your account: " + verificationLink
            );
        } finally {
            TenantContext.clear();
        }
    }

    @Override
    @Transactional
    public void verifyEmail(String identifier, String token) {
        if (identifier == null || identifier.isBlank()) {
            throw new IllegalArgumentException("Identifier is required.");
        }

        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Verification token is required.");
        }

        Tenant tenant = tenantRepository.findByIdentifier(identifier.trim())
                .orElseThrow(() -> new RuntimeException("Tenant not found."));

        if (Boolean.FALSE.equals(tenant.getIsActive())) {
            throw new RuntimeException("Tenant is inactive.");
        }

        TenantContext.set(
                tenant.getId(),
                tenant.getSchemaName(),
                tenant.getIdentifier(),
                null,
                null
        );

        try {
            VerificationToken verificationToken = verificationTokenRepository.findByToken(token.trim())
                    .orElseThrow(() -> new RuntimeException("Invalid verification token."));

            if (verificationToken.isUsed()) {
                throw new RuntimeException("Verification token has already been used.");
            }

            if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Verification token has expired.");
            }

            User user = verificationToken.getUser();
            user.setIsActive(true);
            userRepository.save(user);

            verificationToken.setUsed(true);
            verificationTokenRepository.save(verificationToken);
        } finally {
            TenantContext.clear();
        }
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        validateLoginRequest(request);

        Tenant tenant = tenantRepository.findByIdentifier(request.getIdentifier().trim())
                .orElseThrow(() -> new RuntimeException("Tenant not found."));

        if (Boolean.FALSE.equals(tenant.getIsActive())) {
            throw new RuntimeException("Tenant is inactive.");
        }

        TenantContext.set(
                tenant.getId(),
                tenant.getSchemaName(),
                tenant.getIdentifier(),
                null,
                null
        );

        try {
            String normalizedEmail = request.getEmail().trim().toLowerCase();

            User user = userRepository.findByEmail(normalizedEmail)
                    .orElseThrow(() -> new RuntimeException("Invalid email or password."));

            if (Boolean.FALSE.equals(user.getIsActive())) {
                throw new RuntimeException("Please verify your email before logging in.");
            }

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            normalizedEmail,
                            request.getPassword()
                    )
            );

            String jwtToken = jwtService.generateToken(
                    user,
                    tenant.getId(),
                    tenant.getSchemaName(),
                    tenant.getIdentifier()
            );

            return new LoginResponse(
                    jwtToken,
                    user.getEmail(),
                    user.getRole().getName(),
                    tenant.getIdentifier()
            );
        } finally {
            TenantContext.clear();
        }
    }

    private void validateRegisterRequest(RegisterRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request must not be null.");
        }

        if (request.getIdentifier() == null || request.getIdentifier().isBlank()) {
            throw new IllegalArgumentException("Tenant identifier is required.");
        }

        if (request.getFullName() == null || request.getFullName().isBlank()) {
            throw new IllegalArgumentException("Full name is required.");
        }

        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required.");
        }

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required.");
        }
    }

    private void validateLoginRequest(LoginRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request must not be null.");
        }

        if (request.getIdentifier() == null || request.getIdentifier().isBlank()) {
            throw new IllegalArgumentException("Tenant identifier is required.");
        }

        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required.");
        }

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required.");
        }
    }
}