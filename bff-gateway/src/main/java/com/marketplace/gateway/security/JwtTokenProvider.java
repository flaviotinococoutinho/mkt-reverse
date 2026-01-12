package com.marketplace.gateway.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT Token Provider for authentication and authorization.
 * 
 * Responsibilities:
 * - Generate JWT access tokens
 * - Generate JWT refresh tokens
 * - Validate JWT tokens
 * - Extract claims from tokens
 * 
 * Follows security best practices:
 * - HMAC-SHA256 signing
 * - Configurable expiration
 * - Role-based claims
 * - Tenant isolation
 */
@Component
public class JwtTokenProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);
    
    private static final String ROLES_CLAIM = "roles";
    private static final String TENANT_ID_CLAIM = "tenantId";
    private static final String USER_ID_CLAIM = "userId";
    
    private final SecretKey secretKey;
    private final long accessTokenValidityInMilliseconds;
    private final long refreshTokenValidityInMilliseconds;
    
    public JwtTokenProvider(
        @Value("${jwt.secret}") String secret,
        @Value("${jwt.access-token-validity-in-seconds:3600}") long accessTokenValidity,
        @Value("${jwt.refresh-token-validity-in-seconds:86400}") long refreshTokenValidity
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenValidityInMilliseconds = accessTokenValidity * 1000;
        this.refreshTokenValidityInMilliseconds = refreshTokenValidity * 1000;
    }
    
    /**
     * Generates JWT access token from authentication.
     * 
     * @param authentication Spring Security authentication
     * @param userId user identifier
     * @param tenantId tenant identifier
     * @return JWT access token
     */
    public String generateAccessToken(
        Authentication authentication,
        Long userId,
        Long tenantId
    ) {
        Instant now = Instant.now();
        Instant expiration = now.plus(accessTokenValidityInMilliseconds, ChronoUnit.MILLIS);
        
        List<String> roles = extractRoles(authentication);
        
        return Jwts.builder()
            .setSubject(authentication.getName())
            .claim(USER_ID_CLAIM, userId)
            .claim(TENANT_ID_CLAIM, tenantId)
            .claim(ROLES_CLAIM, roles)
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(expiration))
            .signWith(secretKey, SignatureAlgorithm.HS256)
            .compact();
    }
    
    /**
     * Generates JWT refresh token.
     * 
     * @param username username
     * @param userId user identifier
     * @param tenantId tenant identifier
     * @return JWT refresh token
     */
    public String generateRefreshToken(String username, Long userId, Long tenantId) {
        Instant now = Instant.now();
        Instant expiration = now.plus(refreshTokenValidityInMilliseconds, ChronoUnit.MILLIS);
        
        return Jwts.builder()
            .setSubject(username)
            .claim(USER_ID_CLAIM, userId)
            .claim(TENANT_ID_CLAIM, tenantId)
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(expiration))
            .signWith(secretKey, SignatureAlgorithm.HS256)
            .compact();
    }
    
    /**
     * Validates JWT token.
     * 
     * @param token JWT token
     * @return true if valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException exception) {
            logger.debug("Invalid JWT token: {}", exception.getMessage());
            return false;
        }
    }
    
    /**
     * Extracts username from token.
     * 
     * @param token JWT token
     * @return username
     */
    public String getUsername(String token) {
        Claims claims = parseClaims(token);
        return claims.getSubject();
    }
    
    /**
     * Extracts user ID from token.
     * 
     * @param token JWT token
     * @return user ID
     */
    public Long getUserId(String token) {
        Claims claims = parseClaims(token);
        return claims.get(USER_ID_CLAIM, Long.class);
    }
    
    /**
     * Extracts tenant ID from token.
     * 
     * @param token JWT token
     * @return tenant ID
     */
    public Long getTenantId(String token) {
        Claims claims = parseClaims(token);
        return claims.get(TENANT_ID_CLAIM, Long.class);
    }
    
    /**
     * Extracts roles from token.
     * 
     * @param token JWT token
     * @return list of roles
     */
    @SuppressWarnings("unchecked")
    public List<String> getRoles(String token) {
        Claims claims = parseClaims(token);
        return claims.get(ROLES_CLAIM, List.class);
    }
    
    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token)
            .getBody();
    }
    
    private List<String> extractRoles(Authentication authentication) {
        return authentication.getAuthorities()
            .stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList());
    }
}
