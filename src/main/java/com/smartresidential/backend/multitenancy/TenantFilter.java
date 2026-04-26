package com.smartresidential.backend.multitenancy;

import com.smartresidential.backend.entities.Tenant;
import com.smartresidential.backend.repositories.TenantRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class TenantFilter extends OncePerRequestFilter {

    private static final String TENANT_HEADER = "X-Tenant-Identifier";

    private final TenantRepository tenantRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String tenantIdentifier = request.getHeader(TENANT_HEADER);

        try {
            if (tenantIdentifier != null && !tenantIdentifier.isBlank()) {
                Tenant tenant = tenantRepository.findByIdentifier(tenantIdentifier)
                        .orElse(null);

                if (tenant == null) {
                    response.setStatus(HttpStatus.BAD_REQUEST.value());
                    response.setContentType("application/json");
                    response.getWriter().write("""
                            {
                              "error": "Invalid tenant identifier"
                            }
                            """);
                    return;
                }

                TenantContext.set(
                        tenant.getId(),
                        tenant.getSchemaName(),
                        tenant.getIdentifier(),
                        null,
                        null
                );
            }

            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}