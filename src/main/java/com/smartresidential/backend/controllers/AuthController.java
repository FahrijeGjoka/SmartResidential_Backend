package com.smartresidential.backend.controllers;

import com.smartresidential.backend.dto.auth.LoginRequest;
import com.smartresidential.backend.dto.auth.LoginResponse;
import com.smartresidential.backend.dto.auth.RegisterRequest;
import com.smartresidential.backend.exceptions.UnauthorizedException;
import com.smartresidential.backend.services.interfaces.AuthService;
import com.smartresidential.backend.services.interfaces.AuthService.AuthTokens;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String REFRESH_COOKIE_NAME = "refresh_token";
    private static final String REFRESH_COOKIE_PATH = "/api/auth";

    private final AuthService authService;
    private final Duration refreshCookieMaxAge;
    private final boolean refreshCookieSecure;
    private final String refreshCookieSameSite;

    public AuthController(
            AuthService authService,
            @Value("${app.auth.refresh-cookie.max-age-seconds:1209600}") long refreshCookieMaxAgeSeconds,
            @Value("${app.auth.refresh-cookie.secure:false}") boolean refreshCookieSecure,
            @Value("${app.auth.refresh-cookie.same-site:Lax}") String refreshCookieSameSite
    ) {
        this.authService = authService;
        this.refreshCookieMaxAge = Duration.ofSeconds(refreshCookieMaxAgeSeconds);
        this.refreshCookieSameSite = refreshCookieSameSite;
        this.refreshCookieSecure = refreshCookieSecure || "None".equalsIgnoreCase(refreshCookieSameSite);
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok("Registration successful. Please check your email to verify your account.");
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(
            @RequestParam String identifier,
            @RequestParam String token
    ) {
        authService.verifyEmail(identifier, token);
        return ResponseEntity.ok("Email verified successfully.");
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthTokens tokens = authService.login(request);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, buildRefreshCookie(tokens.refreshToken(), refreshCookieMaxAge).toString())
                .body(tokens.response());
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(
            @RequestHeader("X-Tenant-Identifier") String tenantIdentifier,
            HttpServletRequest request
    ) {
        String refreshToken = findRefreshTokenCookie(request)
                .orElseThrow(() -> new UnauthorizedException("Refresh token cookie is required."));

        AuthTokens tokens = authService.refresh(refreshToken, tenantIdentifier);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, buildRefreshCookie(tokens.refreshToken(), refreshCookieMaxAge).toString())
                .body(tokens.response());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, buildRefreshCookie("", Duration.ZERO).toString())
                .build();
    }

    private Optional<String> findRefreshTokenCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }

        return Arrays.stream(cookies)
                .filter(cookie -> REFRESH_COOKIE_NAME.equals(cookie.getName()))
                .map(Cookie::getValue)
                .filter(value -> value != null && !value.isBlank())
                .findFirst();
    }

    private ResponseCookie buildRefreshCookie(String value, Duration maxAge) {
        return ResponseCookie.from(REFRESH_COOKIE_NAME, value)
                .httpOnly(true)
                .secure(refreshCookieSecure)
                .sameSite(refreshCookieSameSite)
                .path(REFRESH_COOKIE_PATH)
                .maxAge(maxAge)
                .build();
    }
}
