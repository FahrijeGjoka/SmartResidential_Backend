package com.smartresidential.backend.services.impl;

import com.smartresidential.backend.dto.auth.LoginRequest;
import com.smartresidential.backend.dto.auth.LoginResponse;
import com.smartresidential.backend.dto.auth.RegisterRequest;
import com.smartresidential.backend.entities.Role;
import com.smartresidential.backend.entities.Session;
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
import com.smartresidential.backend.repositories.SessionRepository;
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
    private static final long JWT_EXPIRATION_HOURS = 24;

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final RoleRepository roleRepository;
    private final SessionRepository sessionRepository;
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
            SessionRepository sessionRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            EmailService emailService
    ) {
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        this.roleRepository = roleRepository;
        this.sessionRepository = sessionRepository;
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
    public LoginResponse login(LoginRequest request) {
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

        String jwtToken = jwtService.generateToken(
                user,
                tenant.getId(),
                tenant.getSchemaName(),
                tenant.getIdentifier()
        );

        persistLoginSession(user, jwtToken);

        return new LoginResponse(
                jwtToken,
                user.getEmail(),
                role.getName()
        );
    }

    private void persistLoginSession(User user, String jwtToken) {
        sessionRepository.deleteAllByToken(jwtToken);

        LocalDateTime now = LocalDateTime.now();

        Session session = new Session();
        session.setUser(user);
        session.setToken(jwtToken);
        session.setCreatedAt(now);
        session.setExpiresAt(now.plusHours(JWT_EXPIRATION_HOURS));

        sessionRepository.save(session);
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
