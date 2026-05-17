package com.smartresidential.backend.middleware;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {

    private final JwtService jwtService = mock(JwtService.class);
    private final UserDetailsService userDetailsService = mock(UserDetailsService.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final JwtAuthenticationFilter filter = new JwtAuthenticationFilter(
            jwtService,
            userDetailsService,
            userRepository,
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
}
