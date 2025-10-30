package com.marketplace.gateway.config;

import com.marketplace.gateway.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Security configuration for BFF Gateway.
 * 
 * Centralizes authentication and authorization:
 * - JWT-based authentication
 * - Role-based authorization
 * - CORS configuration
 * - Public endpoints (health, login)
 * - Protected endpoints (API routes)
 * 
 * Follows security best practices:
 * - CSRF disabled for stateless JWT
 * - HTTPS in production
 * - Rate limiting (configured separately)
 */
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfiguration {
    
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    
    public SecurityConfiguration(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }
    
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeExchange(exchanges -> exchanges
                // Public endpoints
                .pathMatchers("/actuator/health", "/actuator/info").permitAll()
                .pathMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                .pathMatchers(HttpMethod.POST, "/api/v1/auth/register").permitAll()
                .pathMatchers(HttpMethod.POST, "/api/v1/auth/refresh").permitAll()
                
                // WebSocket endpoints (authenticated)
                .pathMatchers("/ws/**").authenticated()
                
                // Consumer endpoints
                .pathMatchers("/api/v1/opportunities/**")
                    .hasAnyRole("CONSUMER", "ADMIN")
                
                // Company endpoints
                .pathMatchers("/api/v1/proposals/**")
                    .hasAnyRole("COMPANY", "ADMIN")
                
                // Admin endpoints
                .pathMatchers("/api/v1/admin/**")
                    .hasRole("ADMIN")
                
                // All other requests require authentication
                .anyExchange().authenticated()
            )
            .exceptionHandling(exceptionHandling -> exceptionHandling
                .authenticationEntryPoint(new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED))
            )
            .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            .build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        configuration.setAllowedOrigins(List.of(
            "http://localhost:3000",  // React dev server
            "http://localhost:5173",  // Vite dev server
            "https://marketplace.example.com"  // Production
        ));
        
        configuration.setAllowedMethods(List.of(
            "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));
        
        configuration.setAllowedHeaders(List.of(
            "Authorization",
            "Content-Type",
            "X-Tenant-ID",
            "X-Trace-ID"
        ));
        
        configuration.setExposedHeaders(List.of(
            "X-Total-Count",
            "X-Page-Number",
            "X-Page-Size"
        ));
        
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
