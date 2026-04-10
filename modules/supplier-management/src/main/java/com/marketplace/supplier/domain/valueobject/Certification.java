package com.marketplace.supplier.domain.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * Certification value object representing supplier compliance credentials.
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Certification implements Serializable {

    @Column(name = "certification_name", nullable = false, length = 150)
    private String name;

    @Column(name = "certification_number", length = 60)
    private String certificateNumber;

    @Column(name = "certification_issuer", length = 150)
    private String issuer;

    @Column(name = "certification_issued_at", nullable = false)
    private Instant issuedAt;

    @Column(name = "certification_expires_at")
    private Instant expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "certification_status", nullable = false, length = 20)
    private CertificationStatus status;

    public static Certification of(String name, String issuer, Instant issuedAt, Instant expiresAt) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Certification name cannot be blank");
        }
        if (issuedAt == null) {
            throw new IllegalArgumentException("Certification issuedAt cannot be null");
        }
        if (expiresAt != null && !expiresAt.isAfter(issuedAt)) {
            throw new IllegalArgumentException("Certification expiresAt must be after issuedAt");
        }

        return new Certification(
            name.trim(),
            null,
            issuer != null ? issuer.trim() : null,
            issuedAt,
            expiresAt,
            CertificationStatus.PENDING
        );
    }

    public Certification assignNumber(String certificateNumber) {
        return new Certification(name, certificateNumber, issuer, issuedAt, expiresAt, status);
    }

    public Certification markValid() {
        return new Certification(name, certificateNumber, issuer, issuedAt, expiresAt, CertificationStatus.VALID);
    }

    public Certification markRevoked() {
        return new Certification(name, certificateNumber, issuer, issuedAt, expiresAt, CertificationStatus.REVOKED);
    }

    public boolean isExpired(Instant reference) {
        if (expiresAt == null) {
            return false;
        }
        Instant now = reference != null ? reference : Instant.now();
        return now.isAfter(expiresAt);
    }

    public boolean isValid() {
        return status == CertificationStatus.VALID && !isExpired(Instant.now());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Certification that = (Certification) o;
        return Objects.equals(name, that.name) && Objects.equals(certificateNumber, that.certificateNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, certificateNumber);
    }
}
