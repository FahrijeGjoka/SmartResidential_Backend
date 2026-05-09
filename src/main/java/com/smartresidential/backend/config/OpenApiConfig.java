package com.smartresidential.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String TENANT_HEADER = "X-Tenant-Identifier";
    private static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components()

                        // Tenant header
                        .addSecuritySchemes(TENANT_HEADER,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.HEADER)
                                        .name(TENANT_HEADER)
                        )

                        // JWT Bearer
                        .addSecuritySchemes(BEARER_AUTH,
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        )
                )

                .addSecurityItem(new SecurityRequirement().addList(TENANT_HEADER))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH));
    }
}