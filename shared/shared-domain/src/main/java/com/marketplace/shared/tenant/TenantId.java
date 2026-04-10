package com.marketplace.shared.tenant;

import java.io.Serializable;
import java.util.Objects;

/**
 * TenantId is the technical identifier for a Market/Niche (multi-tenancy).
 *
 * <p>Today many modules persist tenant_id as a string (UUID-like, length 36).
 * This Value Object provides a migration path to stronger typing without forcing
 * a big-bang refactor.
 */
public final class TenantId implements Serializable {

    private final String value;

    private TenantId(String value) {
        this.value = value;
    }

    public static TenantId of(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            throw new IllegalArgumentException("tenantId cannot be null or empty");
        }
        String normalized = raw.trim();
        if (normalized.length() > 36) {
            throw new IllegalArgumentException("tenantId is too long");
        }
        return new TenantId(normalized);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TenantId tenantId = (TenantId) o;
        return value.equalsIgnoreCase(tenantId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value.toLowerCase());
    }

    @Override
    public String toString() {
        return value;
    }
}

