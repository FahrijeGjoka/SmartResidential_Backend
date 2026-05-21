package com.smartresidential.backend.middleware;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartresidential.backend.entities.Session;
import com.smartresidential.backend.entities.User;
import com.smartresidential.backend.repositories.SessionRepository;
import com.smartresidential.backend.repositories.UserRepository;
import com.smartresidential.backend.services.interfaces.JwtService;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {

    private final JwtService jwtService = mock(JwtService.class);
    private final UserDetailsService userDetailsService = mock(UserDetailsService.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final SessionRepository sessionRepository = mock(SessionRepository.class);
    private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(
            jwtService,
            userDetailsService,
            userRepository,
            sessionRepository,
            new ObjectMapper().findAndRegisterModules()
    );

    @Test
    void skipsJwtValidationForPublicLoginEvenWhenAuthorizationHeaderIsPresent()
            throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
        request.addHeader("Authorization", "Bearer expired-token");
        request.addHeader("X-Tenant-Identifier", "tenant-a");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(200);
        verify(jwtService, never()).extractEmail("expired-token");
    }

    @Test
    void validatesJwtForProtectedPaths() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/users");
        request.addHeader("Authorization", "Bearer expired-token");
        request.addHeader("X-Tenant-Identifier", "tenant-a");
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.extractEmail("expired-token"))
                .thenThrow(new MalformedJwtException("invalid"));

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("Invalid or expired JWT token.");
        verify(jwtService).extractEmail("expired-token");
    }

    @Test
    void rejectsValidJwtWhenSessionIsMissingOrExpired() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/users");
        request.addHeader("Authorization", "Bearer valid-token");
        request.addHeader("X-Tenant-Identifier", "tenant-a");
        MockHttpServletResponse response = new MockHttpServletResponse();
        User user = new User();
        user.setId(10L);
        user.setEmail("user@example.com");

        when(jwtService.extractEmail("valid-token")).thenReturn(user.getEmail());
        when(jwtService.extractIdentifier("valid-token")).thenReturn("tenant-a");
        when(jwtService.extractSchemaName("valid-token")).thenReturn("tenant_a");
        when(jwtService.extractTenantId("valid-token")).thenReturn(1L);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(jwtService.isTokenValid("valid-token", user)).thenReturn(true);
        when(sessionRepository.findByTokenAndExpiresAtAfter(org.mockito.ArgumentMatchers.eq("valid-token"), org.mockito.ArgumentMatchers.any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString()).contains("Session is expired or revoked.");
        verify(userDetailsService, never()).loadUserByUsername(user.getEmail());
    }

    @Test
    void authenticatesValidJwtWhenSessionExistsAndIsNotExpired() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/users");
        request.addHeader("Authorization", "Bearer valid-token");
        request.addHeader("X-Tenant-Identifier", "tenant-a");
        MockHttpServletResponse response = new MockHttpServletResponse();
        User user = new User();
        user.setId(10L);
        user.setEmail("user@example.com");
        user.setPasswordHash("password");
        org.springframework.security.core.userdetails.User userDetails =
                new org.springframework.security.core.userdetails.User(
                        user.getEmail(),
                        user.getPasswordHash(),
                        List.of()
                );

        when(jwtService.extractEmail("valid-token")).thenReturn(user.getEmail());
        when(jwtService.extractIdentifier("valid-token")).thenReturn("tenant-a");
        when(jwtService.extractSchemaName("valid-token")).thenReturn("tenant_a");
        when(jwtService.extractTenantId("valid-token")).thenReturn(1L);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(jwtService.isTokenValid("valid-token", user)).thenReturn(true);
        when(sessionRepository.findByTokenAndExpiresAtAfter(org.mockito.ArgumentMatchers.eq("valid-token"), org.mockito.ArgumentMatchers.any(LocalDateTime.class)))
                .thenReturn(Optional.of(new Session()));
        when(userDetailsService.loadUserByUsername(user.getEmail())).thenReturn(userDetails);

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(200);
        verify(userDetailsService).loadUserByUsername(user.getEmail());
    }
}
