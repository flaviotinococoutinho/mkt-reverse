package com.marketplace.gateway.handler;

import com.marketplace.gateway.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

/**
 * Handler for authentication endpoints.
 * 
 * Endpoints:
 * - POST /api/v1/auth/login - User login
 * - POST /api/v1/auth/refresh - Refresh access token
 * - POST /api/v1/auth/logout - User logout
 * 
 * Uses functional endpoints with Spring WebFlux.
 */
@Component
public class AuthenticationHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationHandler.class);
    
    private final ReactiveAuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    
    public AuthenticationHandler(
        ReactiveAuthenticationManager authenticationManager,
        JwtTokenProvider tokenProvider
    ) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }
    
    /**
     * Handles user login request.
     * 
     * @param request server request with login credentials
     * @return server response with JWT tokens
     */
    public Mono<ServerResponse> login(ServerRequest request) {
        return request.bodyToMono(LoginRequest.class)
            .flatMap(this::authenticate)
            .flatMap(this::generateTokens)
            .flatMap(response -> 
                ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(response)
            )
            .doOnSuccess(response -> logger.info("User logged in successfully"))
            .onErrorResume(this::handleLoginError);
    }
    
    /**
     * Handles token refresh request.
     * 
     * @param request server request with refresh token
     * @return server response with new access token
     */
    public Mono<ServerResponse> refresh(ServerRequest request) {
        return request.bodyToMono(RefreshTokenRequest.class)
            .filter(req -> tokenProvider.validateToken(req.refreshToken()))
            .flatMap(this::generateNewAccessToken)
            .flatMap(response ->
                ServerResponse.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(response)
            )
            .switchIfEmpty(
                ServerResponse.status(HttpStatus.UNAUTHORIZED)
                    .bodyValue(new ErrorResponse("Invalid refresh token"))
            )
            .doOnSuccess(response -> logger.info("Token refreshed successfully"))
            .onErrorResume(this::handleRefreshError);
    }
    
    /**
     * Handles user logout request.
     * 
     * @param request server request
     * @return server response
     */
    public Mono<ServerResponse> logout(ServerRequest request) {
        // In stateless JWT, logout is handled client-side by removing token
        // For additional security, implement token blacklist in Redis
        
        return ServerResponse.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(new LogoutResponse("Logged out successfully"))
            .doOnSuccess(response -> logger.info("User logged out"));
    }
    
    private Mono<Authentication> authenticate(LoginRequest request) {
        UsernamePasswordAuthenticationToken authToken = 
            new UsernamePasswordAuthenticationToken(
                request.username(),
                request.password()
            );
        
        return authenticationManager.authenticate(authToken);
    }
    
    private Mono<LoginResponse> generateTokens(Authentication authentication) {
        return Mono.fromCallable(() -> {
            // In real implementation, fetch from user service
            Long userId = 1L;  // Mock
            Long tenantId = 1L;  // Mock
            
            String accessToken = tokenProvider.generateAccessToken(
                authentication,
                userId,
                tenantId
            );
            
            String refreshToken = tokenProvider.generateRefreshToken(
                authentication.getName(),
                userId,
                tenantId
            );
            
            return new LoginResponse(
                accessToken,
                refreshToken,
                "Bearer",
                3600L  // expires in seconds
            );
        });
    }
    
    private Mono<RefreshTokenResponse> generateNewAccessToken(RefreshTokenRequest request) {
        return Mono.fromCallable(() -> {
            String username = tokenProvider.getUsername(request.refreshToken());
            Long userId = tokenProvider.getUserId(request.refreshToken());
            Long tenantId = tokenProvider.getTenantId(request.refreshToken());
            
            // Create minimal authentication for token generation
            Authentication auth = new UsernamePasswordAuthenticationToken(
                username,
                null,
                List.of()
            );
            
            String accessToken = tokenProvider.generateAccessToken(auth, userId, tenantId);
            
            return new RefreshTokenResponse(
                accessToken,
                "Bearer",
                3600L
            );
        });
    }
    
    private Mono<ServerResponse> handleLoginError(Throwable error) {
        logger.error("Login failed: {}", error.getMessage());
        
        return ServerResponse.status(HttpStatus.UNAUTHORIZED)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(new ErrorResponse("Invalid credentials"));
    }
    
    private Mono<ServerResponse> handleRefreshError(Throwable error) {
        logger.error("Token refresh failed: {}", error.getMessage());
        
        return ServerResponse.status(HttpStatus.UNAUTHORIZED)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(new ErrorResponse("Token refresh failed"));
    }
    
    // DTOs
    
    public record LoginRequest(
        String username,
        String password
    ) {}
    
    public record LoginResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        Long expiresIn
    ) {}
    
    public record RefreshTokenRequest(
        String refreshToken
    ) {}
    
    public record RefreshTokenResponse(
        String accessToken,
        String tokenType,
        Long expiresIn
    ) {}
    
    public record LogoutResponse(
        String message
    ) {}
    
    public record ErrorResponse(
        String error
    ) {}
}
