package com.marketplace.erp.domain.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

/**
 * API credential value object for ERP integrations.
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiCredential implements Serializable {

    @Column(name = "client_id", nullable = false, length = 80)
    private String clientId;

    @Column(name = "client_secret", nullable = false, length = 180)
    private String clientSecret;

    @Column(name = "credential_rotated_at", nullable = false)
    private Instant rotatedAt;

    public static ApiCredential of(String clientId, String clientSecret) {
        if (clientId == null || clientId.trim().isEmpty()) {
            throw new IllegalArgumentException("clientId cannot be blank");
        }
        if (clientSecret == null || clientSecret.trim().isEmpty()) {
            throw new IllegalArgumentException("clientSecret cannot be blank");
        }
        return new ApiCredential(clientId.trim(), clientSecret.trim(), Instant.now());
    }

    public ApiCredential rotate(String newClientSecret) {
        return new ApiCredential(clientId, newClientSecret.trim(), Instant.now());
    }
}
