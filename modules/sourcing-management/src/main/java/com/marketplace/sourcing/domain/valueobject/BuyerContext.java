package com.marketplace.sourcing.domain.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

/**
 * Buyer context for sourcing events.
 * Encapsulates tenant and buyer contact information used across the aggregate.
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BuyerContext implements Serializable {

    @Column(name = "tenant_id", nullable = false, length = 36)
    private String tenantId;

    @Column(name = "buyer_org_id", nullable = false, length = 36)
    private String organizationId;

    @Column(name = "buyer_department", length = 100)
    private String department;

    @Column(name = "buyer_contact_id", nullable = false, length = 36)
    private String contactId;

    @Column(name = "buyer_contact_name", nullable = false, length = 120)
    private String contactName;

    @Column(name = "buyer_contact_phone", nullable = false, length = 30)
    private String contactPhone;

    /**
     * Compatibility field for legacy/local schemas that still enforce NOT NULL on
     * buyer_contact_email. MVP flows do not use email as a contact channel.
     */
    @Column(name = "buyer_contact_email", nullable = false, length = 255)
    private String contactEmail;

    public static BuyerContext of(
        String tenantId,
        String organizationId,
        String contactId,
        String contactName,
        String contactPhone
    ) {
        return of(tenantId, organizationId, null, contactId, contactName, contactPhone, null);
    }

    public static BuyerContext of(
        String tenantId,
        String organizationId,
        String department,
        String contactId,
        String contactName,
        String contactPhone
    ) {
        return of(tenantId, organizationId, department, contactId, contactName, contactPhone, null);
    }

    public static BuyerContext of(
        String tenantId,
        String organizationId,
        String department,
        String contactId,
        String contactName,
        String contactPhone,
        String contactEmail
    ) {
        validateMandatory("tenantId", tenantId);
        validateMandatory("organizationId", organizationId);
        validateMandatory("contactId", contactId);
        validateMandatory("contactName", contactName);
        validatePhone(contactPhone);

        String resolvedEmail = resolveCompatibilityEmail(contactEmail, contactId);

        return new BuyerContext(
            tenantId.trim(),
            organizationId.trim(),
            department != null ? department.trim() : null,
            contactId.trim(),
            contactName.trim(),
            contactPhone.trim(),
            resolvedEmail
        );
    }

    private static String resolveCompatibilityEmail(String contactEmail, String contactId) {
        if (contactEmail != null && !contactEmail.trim().isEmpty()) {
            return contactEmail.trim();
        }

        String normalizedContactId = contactId.trim().toLowerCase().replaceAll("[^a-z0-9._-]", "-");
        return normalizedContactId + "@queroja.local";
    }

    private static void validateMandatory(String field, String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " cannot be null or empty");
        }
    }

    private static void validatePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("contactPhone cannot be null or empty");
        }
        if (phone.trim().length() > 30) {
            throw new IllegalArgumentException("contactPhone is too long");
        }
    }

    public boolean isSameOrganization(String otherOrganizationId) {
        return organizationId.equalsIgnoreCase(otherOrganizationId);
    }

    public boolean belongsToTenant(String otherTenantId) {
        return tenantId.equalsIgnoreCase(otherTenantId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BuyerContext that = (BuyerContext) o;
        return Objects.equals(tenantId, that.tenantId) &&
               Objects.equals(organizationId, that.organizationId) &&
               Objects.equals(contactId, that.contactId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tenantId, organizationId, contactId);
    }

    @Override
    public String toString() {
        return "BuyerContext{" +
               "tenantId='" + tenantId + '\'' +
               ", organizationId='" + organizationId + '\'' +
               ", contactId='" + contactId + '\'' +
               '}';
    }
}
