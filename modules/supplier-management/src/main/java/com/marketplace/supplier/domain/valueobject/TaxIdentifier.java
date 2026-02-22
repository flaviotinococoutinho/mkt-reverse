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
import java.util.Locale;

/**
 * Legal tax identifier for supplier entities.
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TaxIdentifier implements Serializable {

    @Enumerated(EnumType.STRING)
    @Column(name = "tax_id_type", nullable = false, length = 20)
    private TaxIdType type;

    @Column(name = "tax_id_number", nullable = false, length = 40)
    private String number;

    @Column(name = "tax_id_country", nullable = false, length = 2)
    private String countryCode;

    @Column(name = "tax_id_verified", nullable = false)
    private boolean verified;

    @Column(name = "tax_id_verified_at")
    private Instant verifiedAt;

    public static TaxIdentifier of(TaxIdType type, String number, String countryCode) {
        if (type == null) {
            throw new IllegalArgumentException("Tax identifier type cannot be null");
        }
        if (number == null || number.trim().isEmpty()) {
            throw new IllegalArgumentException("Tax identifier number cannot be blank");
        }
        if (number.length() > 40) {
            throw new IllegalArgumentException("Tax identifier number cannot exceed 40 characters");
        }
        if (countryCode == null || countryCode.trim().length() != 2) {
            throw new IllegalArgumentException("Country code must have 2 characters");
        }

        return new TaxIdentifier(
            type,
            sanitize(number),
            countryCode.trim().toUpperCase(Locale.ROOT),
            false,
            null
        );
    }

    public TaxIdentifier markVerified() {
        return new TaxIdentifier(type, number, countryCode, true, Instant.now());
    }

    private static String sanitize(String value) {
        return value.replaceAll("[^A-Za-z0-9]","").toUpperCase(Locale.ROOT);
    }
}
