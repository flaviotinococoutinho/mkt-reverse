package com.marketplace.contract.domain.valueobject;

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

/**
 * Contract party representation.
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ContractParty implements Serializable {

    @Enumerated(EnumType.STRING)
    @Column(name = "party_role", nullable = false, length = 20)
    private PartyRole role;

    @Column(name = "party_org_id", nullable = false, length = 36)
    private String organizationId;

    @Column(name = "party_contact_name", nullable = false, length = 120)
    private String contactName;

    @Column(name = "party_contact_email", nullable = false, length = 255)
    private String contactEmail;

    @Column(name = "party_signed", nullable = false)
    private boolean signed;

    @Column(name = "party_signed_at")
    private Instant signedAt;

    public static ContractParty of(PartyRole role, String organizationId, String contactName, String contactEmail) {
        if (role == null) {
            throw new IllegalArgumentException("Party role cannot be null");
        }
        if (organizationId == null || organizationId.trim().isEmpty()) {
            throw new IllegalArgumentException("Organization id cannot be blank");
        }
        if (contactName == null || contactName.trim().isEmpty()) {
            throw new IllegalArgumentException("Contact name cannot be blank");
        }
        if (contactEmail == null || contactEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("Contact email cannot be blank");
        }

        return new ContractParty(role, organizationId.trim(), contactName.trim(), contactEmail.trim().toLowerCase(), false, null);
    }

    public ContractParty markSigned() {
        return new ContractParty(role, organizationId, contactName, contactEmail, true, Instant.now());
    }
}
