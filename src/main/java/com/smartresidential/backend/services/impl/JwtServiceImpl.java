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

    private static final long EXPIRATION_TIME = 1000L * 60 * 60 * 24;

    private final String secretKey;

    public JwtServiceImpl(@Value("${app.jwt.secret:${JWT_SECRET:}}") String secretKey) {
        if (secretKey == null || secretKey.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("JWT secret must be configured with at least 32 bytes.");
        }
        this.secretKey = secretKey;
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String generateToken(User user,
                                Long tenantId,
                                String schemaName,
                                String identifier) {

        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("tenantId", tenantId)
                .claim("schemaName", schemaName)
                .claim("identifier", identifier)
                .claim("userId", user.getId())
                .claim("roleName", user.getRoleId())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey())
                .compact();
    }

    @Override
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
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
    public boolean isTokenValid(String token, User user) {
        String email = extractEmail(token);
        return email.equals(user.getEmail()) && !isTokenExpired(token);
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
