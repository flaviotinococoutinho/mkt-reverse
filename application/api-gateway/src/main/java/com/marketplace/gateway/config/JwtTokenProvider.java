package com.marketplace.gateway.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long accessTokenValidityMs;
    private final long refreshTokenValidityMs;

    public JwtTokenProvider(
            @Value("${jwt.secret:queroja-mvp-secret-key-minimo-256-bits-para-hs256}") String secret,
            @Value("${jwt.access-token-validity-ms:3600000}") long accessTokenValidityMs,
            @Value("${jwt.refresh-token-validity-ms:604800000}") long refreshTokenValidityMs) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenValidityMs = accessTokenValidityMs;
        this.refreshTokenValidityMs = refreshTokenValidityMs;
    }

    public String generateAccessToken(String userId, String email, String role, String tenantId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenValidityMs);

        return Jwts.builder()
                .subject(userId)
                .claim("email", email)
                .claim("role", role)
                .claim("tenantId", tenantId)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    public String generateRefreshToken(String userId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshTokenValidityMs);

        return Jwts.builder()
                .subject(userId)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    public Map<String, Object> validateToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return Map.of(
                    "valid", true,
                    "userId", claims.getSubject(),
                    "email", claims.get("email", String.class),
                    "role", claims.get("role", String.class),
                    "tenantId", claims.get("tenantId", String.class)
            );
        } catch (JwtException | IllegalArgumentException e) {
            return Map.of("valid", false, "error", e.getMessage());
        }
    }

    public String getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

    public boolean isRefreshToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return "refresh".equals(claims.get("type"));
        } catch (JwtException e) {
            return false;
        }
    }
}
