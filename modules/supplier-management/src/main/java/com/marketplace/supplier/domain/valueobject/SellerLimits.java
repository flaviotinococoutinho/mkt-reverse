package com.marketplace.supplier.domain.valueobject;

import com.marketplace.shared.tenant.MarketPolicySnapshot;

import java.io.Serializable;
import java.util.Objects;

/**
 * Effective limits for a seller profile within a given market policy.
 *
 * <p>This is intentionally a small value object that can be derived from
 * (policy snapshot + seller nature). KYC/KYB refinements can be layered later.
 */
public final class SellerLimits implements Serializable {

    private final int maxActiveProposals;
    private final long maxEscrowExposureCents;

    private SellerLimits(int maxActiveProposals, long maxEscrowExposureCents) {
        if (maxActiveProposals < 0) {
            throw new IllegalArgumentException("maxActiveProposals must be >= 0");
        }
        if (maxEscrowExposureCents < 0) {
            throw new IllegalArgumentException("maxEscrowExposureCents must be >= 0");
        }
        this.maxActiveProposals = maxActiveProposals;
        this.maxEscrowExposureCents = maxEscrowExposureCents;
    }

    public static SellerLimits from(MarketPolicySnapshot policy, SellerNature nature) {
        Objects.requireNonNull(policy, "policy");
        Objects.requireNonNull(nature, "nature");

        return switch (nature) {
            case INDIVIDUAL -> new SellerLimits(
                policy.pfMaxActiveProposals(),
                policy.pfMaxEscrowExposureCents()
            );
            case BUSINESS -> new SellerLimits(
                policy.pjMaxActiveProposals(),
                policy.pjMaxEscrowExposureCents()
            );
        };
    }

    public int maxActiveProposals() {
        return maxActiveProposals;
    }

    public long maxEscrowExposureCents() {
        return maxEscrowExposureCents;
    }
}

