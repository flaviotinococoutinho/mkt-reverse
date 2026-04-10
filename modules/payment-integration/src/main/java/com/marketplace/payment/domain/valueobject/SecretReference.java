package com.marketplace.payment.domain.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

/**
 * Reference to encrypted credentials managed by vault.
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SecretReference implements Serializable {

    @Column(name = "secret_key", nullable = false, length = 80)
    private String key;

    @Column(name = "secret_value", nullable = false, length = 180)
    private String encryptedValue;

    @Column(name = "secret_rotated_at", nullable = false)
    private Instant rotatedAt;

    public static SecretReference of(String key, String encryptedValue) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Secret key cannot be blank");
        }
        if (encryptedValue == null || encryptedValue.trim().isEmpty()) {
            throw new IllegalArgumentException("Secret value cannot be blank");
        }
        return new SecretReference(key.trim(), encryptedValue.trim(), Instant.now());
    }

    public SecretReference rotate(String newEncryptedValue) {
        return new SecretReference(key, newEncryptedValue.trim(), Instant.now());
    }
}
