package com.smartresidential.backend.services.impl;

import com.smartresidential.backend.dto.auth.LoginRequest;
import com.smartresidential.backend.dto.auth.LoginResponse;
import com.smartresidential.backend.dto.auth.RegisterRequest;
import com.smartresidential.backend.entities.Role;
import com.smartresidential.backend.entities.Tenant;
import com.smartresidential.backend.entities.User;
import com.smartresidential.backend.entities.VerificationToken;
import com.smartresidential.backend.exceptions.BadRequestException;
import com.smartresidential.backend.exceptions.ConflictException;
import com.smartresidential.backend.exceptions.ResourceNotFoundException;
import com.smartresidential.backend.exceptions.TenantNotFoundException;
import com.smartresidential.backend.exceptions.UnauthorizedException;
import com.smartresidential.backend.multitenancy.TenantContext;
import com.smartresidential.backend.repositories.RoleRepository;
import com.smartresidential.backend.repositories.TenantRepository;
import com.smartresidential.backend.repositories.UserRepository;
import com.smartresidential.backend.repositories.VerificationTokenRepository;
import com.smartresidential.backend.services.interfaces.AuthService;
import com.smartresidential.backend.services.interfaces.EmailService;
import com.smartresidential.backend.services.interfaces.JwtService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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

    @PersistenceContext
    private EntityManager entityManager;

    public AuthServiceImpl(
            TenantRepository tenantRepository,
            UserRepository userRepository,
            VerificationTokenRepository verificationTokenRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            EmailService emailService
    ) {
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

        Tenant tenant = getTenantFromContext();
        setTenantSchema(tenant.getSchemaName());

        String normalizedEmail = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new ConflictException("Email already exists.");
        }

        Role defaultRole = roleRepository.findByName(DEFAULT_ROLE_NAME)
                .orElseThrow(() -> new ResourceNotFoundException("Default role not found: " + DEFAULT_ROLE_NAME));

        User user = new User();

        String fullName = request.getFullName().trim();
        String[] names = fullName.split("\\s+", 2);

        user.setFirstName(names[0]);
        user.setLastName(names.length > 1 ? names[1] : "");
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setIsActive(false);
        user.setRoleId(defaultRole.getId());

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
    }

    @Override
    @Transactional
    public void verifyEmail(String identifier, String token) {
        if (identifier == null || identifier.isBlank()) {
            throw new BadRequestException("Tenant identifier is required.");
        }

        if (token == null || token.isBlank()) {
            throw new BadRequestException("Verification token is required.");
        }

        Tenant tenant = tenantRepository.findByIdentifier(identifier.trim())
                .orElseThrow(() -> new TenantNotFoundException("Tenant not found."));

        if (Boolean.FALSE.equals(tenant.getIsActive())) {
            throw new BadRequestException("Tenant is inactive.");
        }

        setTenantSchema(tenant.getSchemaName());

        VerificationToken verificationToken = verificationTokenRepository.findByToken(token.trim())
                .orElseThrow(() -> new BadRequestException("Invalid verification token."));

        if (verificationToken.isUsed()) {
            throw new ConflictException("Verification token has already been used.");
        }

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Verification token has expired.");
        }

        User user = verificationToken.getUser();
        user.setIsActive(true);
        userRepository.save(user);

        verificationToken.setUsed(true);
        verificationTokenRepository.save(verificationToken);
    }

    @Override
    @Transactional
    public AuthTokens login(LoginRequest request) {
        validateLoginRequest(request);

        Tenant tenant = getTenantFromContext();
        setTenantSchema(tenant.getSchemaName());

        String normalizedEmail = request.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password."));

        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new UnauthorizedException("Please verify your email before logging in.");
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        normalizedEmail,
                        request.getPassword()
                )
        );

        Role role = roleRepository.findById(user.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        String accessToken = jwtService.generateAccessToken(
                user,
                tenant.getId(),
                tenant.getSchemaName(),
                tenant.getIdentifier(),
                role.getName()
        );

        String refreshToken = jwtService.generateRefreshToken(
                user,
                tenant.getId(),
                tenant.getSchemaName(),
                tenant.getIdentifier()
        );

        return new AuthTokens(new LoginResponse(
                accessToken,
                user.getEmail(),
                role.getName()
        ), refreshToken);
    }

    @Override
    @Transactional
    public AuthTokens refresh(String refreshToken, String tenantIdentifier) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new UnauthorizedException("Refresh token is required.");
        }

        if (tenantIdentifier == null || tenantIdentifier.isBlank()) {
            throw new BadRequestException("Tenant identifier is required in X-Tenant-Identifier header.");
        }

        String tokenTenantIdentifier;
        Long tokenTenantId;
        Long tokenUserId;
        String tokenEmail;

        try {
            tokenTenantIdentifier = jwtService.extractIdentifier(refreshToken);
            tokenTenantId = jwtService.extractTenantId(refreshToken);
            tokenUserId = jwtService.extractUserId(refreshToken);
            tokenEmail = jwtService.extractEmail(refreshToken);
        } catch (RuntimeException e) {
            throw new UnauthorizedException("Invalid refresh token.");
        }

        if (tokenTenantIdentifier == null || tokenEmail == null || tokenEmail.isBlank()
                || !tenantIdentifier.trim().equals(tokenTenantIdentifier)) {
            throw new UnauthorizedException("Refresh token tenant does not match request tenant.");
        }

        Tenant tenant = tenantRepository.findByIdentifier(tenantIdentifier.trim())
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token tenant."));

        if (Boolean.FALSE.equals(tenant.getIsActive())
                || tokenTenantId == null
                || !tokenTenantId.equals(tenant.getId())) {
            throw new UnauthorizedException("Invalid refresh token tenant.");
        }

        setTenantSchema(tenant.getSchemaName());
        TenantContext.set(tenant.getId(), tenant.getSchemaName(), tenant.getIdentifier(), null, null);

        User user = tokenUserId == null
                ? userRepository.findByEmail(tokenEmail.trim().toLowerCase())
                        .orElseThrow(() -> new UnauthorizedException("Invalid refresh token."))
                : userRepository.findById(tokenUserId)
                        .orElseThrow(() -> new UnauthorizedException("Invalid refresh token."));

        if (!user.getEmail().equals(tokenEmail) || Boolean.FALSE.equals(user.getIsActive())) {
            throw new UnauthorizedException("Invalid refresh token.");
        }

        try {
            if (!jwtService.isRefreshTokenValid(refreshToken, user)) {
                throw new UnauthorizedException("Invalid refresh token.");
            }
        } catch (RuntimeException e) {
            throw new UnauthorizedException("Invalid refresh token.");
        }

        Role role = roleRepository.findById(user.getRoleId())
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token user role."));

        String accessToken = jwtService.generateAccessToken(
                user,
                tenant.getId(),
                tenant.getSchemaName(),
                tenant.getIdentifier(),
                role.getName()
        );

        String rotatedRefreshToken = jwtService.generateRefreshToken(
                user,
                tenant.getId(),
                tenant.getSchemaName(),
                tenant.getIdentifier()
        );

        return new AuthTokens(new LoginResponse(
                accessToken,
                user.getEmail(),
                role.getName()
        ), rotatedRefreshToken);
    }

    private Tenant getTenantFromContext() {
        String tenantIdentifier = TenantContext.getIdentifier();

        if (tenantIdentifier == null || tenantIdentifier.isBlank()) {
            throw new BadRequestException("Tenant identifier is required in X-Tenant-Identifier header.");
        }

        Tenant tenant = tenantRepository.findByIdentifier(tenantIdentifier.trim())
                .orElseThrow(() -> new TenantNotFoundException("Tenant not found."));

        if (Boolean.FALSE.equals(tenant.getIsActive())) {
            throw new BadRequestException("Tenant is inactive.");
        }

        return tenant;
    }

    private void setTenantSchema(String schemaName) {
        entityManager.createNativeQuery(
                "SET search_path TO \"" + schemaName + "\""
        ).executeUpdate();
    }

    private void validateRegisterRequest(RegisterRequest request) {
        if (request == null) {
            throw new BadRequestException("Request must not be null.");
        }

        if (request.getFullName() == null || request.getFullName().isBlank()) {
            throw new BadRequestException("Full name is required.");
        }

        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new BadRequestException("Email is required.");
        }

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BadRequestException("Password is required.");
        }
    }

    private void validateLoginRequest(LoginRequest request) {
        if (request == null) {
            throw new BadRequestException("Request must not be null.");
        }

        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new BadRequestException("Email is required.");
        }

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BadRequestException("Password is required.");
        }
    }
}
