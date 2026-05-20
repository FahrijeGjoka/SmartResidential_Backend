package com.smartresidential.backend.services.interfaces;

import com.smartresidential.backend.entities.User;

public interface JwtService {

    String generateAccessToken(User user,
                               Long tenantId,
                               String schemaName,
                               String identifier,
                               String roleName);

    String generateRefreshToken(User user,
                                Long tenantId,
                                String schemaName,
                                String identifier);

    String extractEmail(String token);

    Long extractUserId(String token);

    Long extractTenantId(String token);

    String extractSchemaName(String token);

    String extractIdentifier(String token);

    Integer extractTokenVersion(String token);

    String extractTokenType(String token);

    boolean isTokenValid(String token, User user);

    boolean isRefreshTokenValid(String token, User user);
}
