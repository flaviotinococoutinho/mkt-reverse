package com.marketplace.gateway.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtTokenProvider {

    /** Dev-only fallback — ProductionSafetyGuard refuses to boot prod with it. */
    public static final String DEV_DEFAULT_SECRET = "queroja-mvp-secret-key-minimo-256-bits-para-hs256";

    private final SecretKey secretKey;
    private final long accessTokenValidityMs;
    private final long refreshTokenValidityMs;

    public JwtTokenProvider(
            @Value("${jwt.secret:" + DEV_DEFAULT_SECRET + "}") String secret,
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
                .claim("type", "refresh")
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    /**
     * Validates a token and extracts its claims. Null-tolerant: a refresh
     * token (which carries only the subject) must yield {valid:true} with
     * null claims, never a NullPointerException-turned-500.
     */
    public Map<String, Object> validateToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Map<String, Object> result = new HashMap<>();
            result.put("valid", true);
            result.put("userId", claims.getSubject());
            result.put("email", claims.get("email", String.class));
            result.put("role", claims.get("role", String.class));
            result.put("tenantId", claims.get("tenantId", String.class));
            result.put("type", claims.get("type", String.class));
            return result;
        } catch (JwtException | IllegalArgumentException e) {
            return Map.of("valid", false, "error", String.valueOf(e.getMessage()));
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
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
