package com.smartresidential.backend.middleware;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartresidential.backend.entities.User;
import com.smartresidential.backend.exceptions.ApiErrorResponse;
import com.smartresidential.backend.multitenancy.TenantContext;
import com.smartresidential.backend.repositories.UserRepository;
import com.smartresidential.backend.services.interfaces.JwtService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String TENANT_HEADER = "X-Tenant-Identifier";
    private static final String LOGIN_PATH = "/api/auth/login";
    private static final String REFRESH_PATH = "/api/auth/refresh";
    private static final String LOGOUT_PATH = "/api/auth/logout";
    private static final String SIGNUP_PATH = "/api/auth/signup";
    private static final String VERIFY_PATH = "/api/auth/verify";

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserDetailsService userDetailsService,
            UserRepository userRepository,
            ObjectMapper objectMapper
    ) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String method = request.getMethod();
        String path = getRequestPath(request);

        return ("POST".equalsIgnoreCase(method)
                        && (LOGIN_PATH.equals(path) || REFRESH_PATH.equals(path) || LOGOUT_PATH.equals(path) || SIGNUP_PATH.equals(path)))
                || ("GET".equalsIgnoreCase(method) && VERIFY_PATH.equals(path));
    }

    private String getRequestPath(HttpServletRequest request) {
        String path = request.getServletPath();

        if (path == null || path.isBlank()) {
            path = request.getRequestURI();
            String contextPath = request.getContextPath();

            if (contextPath != null && !contextPath.isBlank() && path.startsWith(contextPath)) {
                path = path.substring(contextPath.length());
            }
        }

        return path;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader(AUTH_HEADER);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = authHeader.substring(BEARER_PREFIX.length());

            String email = jwtService.extractEmail(token);
            String jwtIdentifier = jwtService.extractIdentifier(token);
            String schemaName = jwtService.extractSchemaName(token);
            Long tenantId = jwtService.extractTenantId(token);

            String headerIdentifier = request.getHeader(TENANT_HEADER);

            if (headerIdentifier == null || headerIdentifier.isBlank()) {
                writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                        "Missing X-Tenant-Identifier header.", request.getRequestURI());
                return;
            }

            if (jwtIdentifier == null || !headerIdentifier.trim().equals(jwtIdentifier)) {
                writeError(response, HttpServletResponse.SC_FORBIDDEN,
                        "Tenant header does not match JWT tenant.", request.getRequestURI());
                return;
            }

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                TenantContext.set(
                        tenantId,
                        schemaName,
                        jwtIdentifier,
                        null,
                        null
                );

                User user = userRepository.findByEmail(email.trim().toLowerCase())
                        .orElse(null);

                if (user != null && jwtService.isTokenValid(token, user)) {

                    UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    TenantContext.set(
                            tenantId,
                            schemaName,
                            jwtIdentifier,
                            user.getId(),
                            userDetails.getAuthorities().isEmpty()
                                    ? null
                                    : userDetails.getAuthorities().iterator().next().getAuthority()
                    );
                }
            }

            filterChain.doFilter(request, response);

        } catch (JwtException | IllegalArgumentException e) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Invalid or expired JWT token.", request.getRequestURI());
        }
    }

    private void writeError(
            HttpServletResponse response,
            int status,
            String message,
            String path
    ) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(
                ApiErrorResponse.of(
                        status,
                        HttpServletResponse.SC_UNAUTHORIZED == status ? "Unauthorized"
                                : HttpServletResponse.SC_FORBIDDEN == status ? "Forbidden"
                                : "Bad Request",
                        message,
                        path
                )
        ));
    }
}
