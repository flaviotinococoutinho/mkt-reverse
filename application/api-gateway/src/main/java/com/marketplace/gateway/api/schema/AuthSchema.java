package com.marketplace.gateway.api.schema;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;

/**
 * Authentication API schemas.
 */
public sealed interface AuthSchema 
        permits AuthSchema.LoginRequest,
                AuthSchema.RegisterRequest,
                AuthSchema.LoginResponse,
                AuthSchema.RefreshRequest,
                AuthSchema.UserView,
                AuthSchema.ErrorResponse {

    record LoginRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        @JsonProperty("email")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        @JsonProperty("password")
        String password
    ) implements AuthSchema {}

    record RegisterRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        @JsonProperty("email")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        @JsonProperty("password")
        String password,

        @NotBlank(message = "First name is required")
        @Size(min = 2, max = 100, message = "First name must be between 2 and 100 characters")
        @JsonProperty("first_name")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(min = 2, max = 100, message = "Last name must be between 2 and 100 characters")
        @JsonProperty("last_name")
        String lastName,

        @JsonProperty("display_name")
        String displayName,

        @NotBlank(message = "Document number is required")
        @Pattern(regexp = "^[0-9]{11}$|^[0-9]{14}$", message = "Invalid document format (CPF: 11 digits, CNPJ: 14 digits)")
        @JsonProperty("document_number")
        String documentNumber,

        @NotNull(message = "Document type is required")
        @JsonProperty("document_type")
        String documentType,

        @NotNull(message = "User type is required")
        @JsonProperty("user_type")
        String userType
    ) implements AuthSchema {

        public static final Set<String> VALID_DOCUMENT_TYPES = Set.of("CPF", "CNPJ");
        public static final Set<String> VALID_USER_TYPES = Set.of("BUYER", "SUPPLIER");
    }

    record LoginResponse(
        @JsonProperty("access_token")
        String accessToken,

        @JsonProperty("refresh_token")
        String refreshToken,

        @JsonProperty("token_type")
        String tokenType,

        @JsonProperty("expires_in")
        Long expiresIn,

        @JsonProperty("user")
        UserView user
    ) implements AuthSchema {}

    record RefreshRequest(
        @NotBlank(message = "Refresh token is required")
        @JsonProperty("refresh_token")
        String refreshToken
    ) implements AuthSchema {}

    record UserView(
        @JsonProperty("id")
        String id,

        @JsonProperty("email")
        String email,

        @JsonProperty("name")
        String name,

        @JsonProperty("role")
        String role,

        @JsonProperty("tenant_id")
        String tenantId,

        @JsonProperty("organization_id")
        String organizationId,

        @JsonProperty("status")
        String status,

        @JsonProperty("created_at")
        Instant createdAt
    ) implements AuthSchema {}

    record ErrorResponse(
            @JsonProperty("error")
            String error,

            @JsonProperty("message")
            String message,

            @JsonProperty("field")
            String field
    ) implements AuthSchema {

        public static ErrorResponse unauthorized(String message) {
            return new ErrorResponse("UNAUTHORIZED", message, null);
        }

        public static ErrorResponse forbidden(String message) {
            return new ErrorResponse("FORBIDDEN", message, null);
        }

        public static ErrorResponse notFound(String resource, String id) {
            return new ErrorResponse("NOT_FOUND", resource + " not found: " + id, null);
        }

        public static ErrorResponse validation(String message, String field) {
            return new ErrorResponse("VALIDATION_ERROR", message, field);
        }
    }
}