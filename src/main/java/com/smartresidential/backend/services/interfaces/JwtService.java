package com.smartresidential.backend.services.interfaces;

import com.smartresidential.backend.entities.User;

public interface JwtService {

    String generateToken(User user,
                         Long tenantId,
                         String schemaName,
                         String identifier);

    String extractEmail(String token);

    Long extractTenantId(String token);

    String extractSchemaName(String token);

    String extractIdentifier(String token);

    boolean isTokenValid(String token, User user);
}
