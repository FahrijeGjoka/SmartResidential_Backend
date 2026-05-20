package com.smartresidential.backend.services.impl;

import com.smartresidential.backend.entities.User;
import com.smartresidential.backend.services.interfaces.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtServiceImpl implements JwtService {

    private static final String TOKEN_TYPE_ACCESS = "access";
    private static final String TOKEN_TYPE_REFRESH = "refresh";

    private final String secretKey;
    private final long accessExpirationMillis;
    private final long refreshExpirationMillis;

    public JwtServiceImpl(
            @Value("${app.jwt.secret:${JWT_SECRET:}}") String secretKey,
            @Value("${app.jwt.access-expiration-ms:900000}") long accessExpirationMillis,
            @Value("${app.jwt.refresh-expiration-ms:1209600000}") long refreshExpirationMillis
    ) {
        if (secretKey == null || secretKey.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("JWT secret must be configured with at least 32 bytes.");
        }
        this.secretKey = secretKey;
        this.accessExpirationMillis = accessExpirationMillis;
        this.refreshExpirationMillis = refreshExpirationMillis;
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String generateAccessToken(User user,
                                      Long tenantId,
                                      String schemaName,
                                      String identifier,
                                      String roleName) {

        return buildToken(user, tenantId, schemaName, identifier, TOKEN_TYPE_ACCESS, roleName, accessExpirationMillis);
    }

    @Override
    public String generateRefreshToken(User user,
                                       Long tenantId,
                                       String schemaName,
                                       String identifier) {
        return buildToken(user, tenantId, schemaName, identifier, TOKEN_TYPE_REFRESH, null, refreshExpirationMillis);
    }

    private String buildToken(User user,
                              Long tenantId,
                              String schemaName,
                              String identifier,
                              String tokenType,
                              String roleName,
                              long expirationMillis) {
        var builder = Jwts.builder()
                .setSubject(user.getEmail())
                .claim("type", tokenType)
                .claim("tenantId", tenantId)
                .claim("tenant_id", tenantId)
                .claim("schemaName", schemaName)
                .claim("identifier", identifier)
                .claim("userId", user.getId())
                .claim("id", user.getId())
                .claim("nameidentifier", user.getId())
                .claim("tokenVersion", user.getTokenVersion() == null ? 0 : user.getTokenVersion())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMillis));

        if (roleName != null) {
            builder.claim("role", roleName).claim("roleName", roleName);
        }

        return builder.signWith(getSigningKey()).compact();
    }

    @Override
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @Override
    public Long extractUserId(String token) {
        Object val = extractAllClaims(token).get("userId");
        return val == null ? null : ((Number) val).longValue();
    }

    @Override
    public Long extractTenantId(String token) {
        Object val = extractAllClaims(token).get("tenantId");
        return val == null ? null : ((Number) val).longValue();
    }

    @Override
    public String extractSchemaName(String token) {
        return extractAllClaims(token).get("schemaName", String.class);
    }

    @Override
    public String extractIdentifier(String token) {
        return extractAllClaims(token).get("identifier", String.class);
    }

    @Override
    public Integer extractTokenVersion(String token) {
        Object val = extractAllClaims(token).get("tokenVersion");
        return val == null ? 0 : ((Number) val).intValue();
    }

    @Override
    public String extractTokenType(String token) {
        return extractAllClaims(token).get("type", String.class);
    }

    @Override
    public boolean isTokenValid(String token, User user) {
        String email = extractEmail(token);
        return email.equals(user.getEmail())
                && TOKEN_TYPE_ACCESS.equals(extractTokenType(token))
                && !isTokenExpired(token);
    }

    @Override
    public boolean isRefreshTokenValid(String token, User user) {
        String email = extractEmail(token);
        Integer expectedVersion = user.getTokenVersion() == null ? 0 : user.getTokenVersion();
        return email.equals(user.getEmail())
                && TOKEN_TYPE_REFRESH.equals(extractTokenType(token))
                && expectedVersion.equals(extractTokenVersion(token))
                && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
