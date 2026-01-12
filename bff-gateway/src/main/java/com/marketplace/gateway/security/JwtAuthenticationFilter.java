package com.marketplace.gateway.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT Authentication Filter for reactive Spring Security.
 * 
 * Responsibilities:
 * - Extract JWT token from Authorization header
 * - Validate JWT token
 * - Create Spring Security authentication
 * - Add user context to MDC for logging
 * - Propagate tenant ID to downstream services
 * 
 * Follows reactive programming model with Project Reactor.
 */
@Component
public class JwtAuthenticationFilter implements WebFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    private static final String AUTHORIZATION_HEADER = HttpHeaders.AUTHORIZATION;
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String TENANT_ID_HEADER = "X-Tenant-ID";
    private static final String USER_ID_HEADER = "X-User-ID";
    private static final String TRACE_ID_HEADER = "X-Trace-ID";
    
    private final JwtTokenProvider tokenProvider;
    
    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String token = extractToken(exchange.getRequest());
        
        if (hasNoToken(token)) {
            return chain.filter(exchange);
        }
        
        if (isInvalidToken(token)) {
            logger.warn("Invalid JWT token received");
            return chain.filter(exchange);
        }
        
        return authenticateAndPropagateContext(exchange, chain, token);
    }
    
    private Mono<Void> authenticateAndPropagateContext(
        ServerWebExchange exchange,
        WebFilterChain chain,
        String token
    ) {
        UsernamePasswordAuthenticationToken authentication = createAuthentication(token);
        ServerWebExchange mutatedExchange = addContextHeaders(exchange, token);
        
        addToMdc(token);
        
        return chain.filter(mutatedExchange)
            .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication))
            .doFinally(signalType -> clearMdc());
    }
    
    private String extractToken(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getFirst(AUTHORIZATION_HEADER);
        
        if (hasBearerToken(bearerToken)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        
        return null;
    }
    
    private boolean hasBearerToken(String bearerToken) {
        return bearerToken != null && bearerToken.startsWith(BEARER_PREFIX);
    }
    
    private boolean hasNoToken(String token) {
        return token == null || token.isBlank();
    }
    
    private boolean isInvalidToken(String token) {
        return !tokenProvider.validateToken(token);
    }
    
    private UsernamePasswordAuthenticationToken createAuthentication(String token) {
        String username = tokenProvider.getUsername(token);
        List<String> roles = tokenProvider.getRoles(token);
        
        List<SimpleGrantedAuthority> authorities = roles.stream()
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());
        
        return new UsernamePasswordAuthenticationToken(username, null, authorities);
    }
    
    private ServerWebExchange addContextHeaders(ServerWebExchange exchange, String token) {
        Long tenantId = tokenProvider.getTenantId(token);
        Long userId = tokenProvider.getUserId(token);
        
        ServerHttpRequest mutatedRequest = exchange.getRequest()
            .mutate()
            .header(TENANT_ID_HEADER, String.valueOf(tenantId))
            .header(USER_ID_HEADER, String.valueOf(userId))
            .build();
        
        return exchange.mutate().request(mutatedRequest).build();
    }
    
    private void addToMdc(String token) {
        try {
            String username = tokenProvider.getUsername(token);
            Long userId = tokenProvider.getUserId(token);
            Long tenantId = tokenProvider.getTenantId(token);
            
            MDC.put("username", username);
            MDC.put("userId", String.valueOf(userId));
            MDC.put("tenantId", String.valueOf(tenantId));
            
            logger.debug("User context added to MDC: username={}, userId={}, tenantId={}", 
                username, userId, tenantId);
        } catch (Exception exception) {
            logger.warn("Failed to add user context to MDC", exception);
        }
    }
    
    private void clearMdc() {
        MDC.remove("username");
        MDC.remove("userId");
        MDC.remove("tenantId");
    }
}
