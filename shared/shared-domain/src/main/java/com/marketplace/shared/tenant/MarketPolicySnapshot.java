package com.marketplace.shared.tenant;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * Immutable snapshot of the MarketPolicy applied to a contract/settlement.
 *
 * <p>Why snapshot? Policies can change over time per market. Financial and dispute
 * outcomes must be reproducible and auditable with the exact policy version
 * that was in force when the parties accepted the deal.
 */
public final class MarketPolicySnapshot implements Serializable {

    private final TenantId tenantId;
    private final long version;

    // Visibility
    private final IntentVisibility intentVisibility;
    private final SellerDiscovery sellerDiscovery;

    // Economy
    private final int takeRateBps;
    private final RefundFeePolicy refundFeePolicy;
    private final ReverseShippingPayer reverseShippingPayer;

    // Dispute windows
    private final int disputeWindowHours;
    private final int inspectionWindowHours;

    // Compliance & limits
    private final int pfMaxActiveProposals;
    private final long pfMaxEscrowExposureCents;
    private final int pjMaxActiveProposals;
    private final long pjMaxEscrowExposureCents;
    private final List<String> blockedMccCodes;
    private final long requiresVerifiedKycAboveCents;

    public MarketPolicySnapshot(
        TenantId tenantId,
        long version,
        IntentVisibility intentVisibility,
        SellerDiscovery sellerDiscovery,
        int takeRateBps,
        RefundFeePolicy refundFeePolicy,
        ReverseShippingPayer reverseShippingPayer,
        int disputeWindowHours,
        int inspectionWindowHours,
        int pfMaxActiveProposals,
        long pfMaxEscrowExposureCents,
        int pjMaxActiveProposals,
        long pjMaxEscrowExposureCents,
        List<String> blockedMccCodes,
        long requiresVerifiedKycAboveCents
    ) {
        this.tenantId = Objects.requireNonNull(tenantId, "tenantId");
        if (version <= 0) throw new IllegalArgumentException("version must be > 0");
        this.version = version;
        this.intentVisibility = Objects.requireNonNull(intentVisibility, "intentVisibility");
        this.sellerDiscovery = Objects.requireNonNull(sellerDiscovery, "sellerDiscovery");
        this.refundFeePolicy = Objects.requireNonNull(refundFeePolicy, "refundFeePolicy");
        this.reverseShippingPayer = Objects.requireNonNull(reverseShippingPayer, "reverseShippingPayer");

        validateTakeRateBps(takeRateBps);
        this.takeRateBps = takeRateBps;

        this.disputeWindowHours = positive("disputeWindowHours", disputeWindowHours);
        this.inspectionWindowHours = positive("inspectionWindowHours", inspectionWindowHours);

        this.pfMaxActiveProposals = nonNegative("pfMaxActiveProposals", pfMaxActiveProposals);
        this.pfMaxEscrowExposureCents = nonNegative("pfMaxEscrowExposureCents", pfMaxEscrowExposureCents);
        this.pjMaxActiveProposals = nonNegative("pjMaxActiveProposals", pjMaxActiveProposals);
        this.pjMaxEscrowExposureCents = nonNegative("pjMaxEscrowExposureCents", pjMaxEscrowExposureCents);
        this.blockedMccCodes = blockedMccCodes == null ? List.of() : List.copyOf(blockedMccCodes);
        this.requiresVerifiedKycAboveCents = nonNegative("requiresVerifiedKycAboveCents", requiresVerifiedKycAboveCents);
    }

    private static void validateTakeRateBps(int bps) {
        if (bps < 0 || bps > 10_000) {
            throw new IllegalArgumentException("takeRateBps must be between 0 and 10000");
        }
    }

    private static int positive(String name, int value) {
        if (value <= 0) throw new IllegalArgumentException(name + " must be > 0");
        return value;
    }

    private static int nonNegative(String name, int value) {
        if (value < 0) throw new IllegalArgumentException(name + " must be >= 0");
        return value;
    }

    private static long nonNegative(String name, long value) {
        if (value < 0) throw new IllegalArgumentException(name + " must be >= 0");
        return value;
    }

    public TenantId tenantId() { return tenantId; }
    public long version() { return version; }
    public IntentVisibility intentVisibility() { return intentVisibility; }
    public SellerDiscovery sellerDiscovery() { return sellerDiscovery; }
    public int takeRateBps() { return takeRateBps; }
    public RefundFeePolicy refundFeePolicy() { return refundFeePolicy; }
    public ReverseShippingPayer reverseShippingPayer() { return reverseShippingPayer; }
    public int disputeWindowHours() { return disputeWindowHours; }
    public int inspectionWindowHours() { return inspectionWindowHours; }
    public int pfMaxActiveProposals() { return pfMaxActiveProposals; }
    public long pfMaxEscrowExposureCents() { return pfMaxEscrowExposureCents; }
    public int pjMaxActiveProposals() { return pjMaxActiveProposals; }
    public long pjMaxEscrowExposureCents() { return pjMaxEscrowExposureCents; }
    public List<String> blockedMccCodes() { return blockedMccCodes; }
    public long requiresVerifiedKycAboveCents() { return requiresVerifiedKycAboveCents; }

    public enum IntentVisibility {
        PUBLIC,
        MARKET_ONLY,
        INVITE_ONLY
    }

    public enum SellerDiscovery {
        ENABLED,
        DISABLED
    }

    public enum RefundFeePolicy {
        PROPORTIONAL,
        FIXED_ORIGINAL,
        WAIVED_PARTIAL
    }

    public enum ReverseShippingPayer {
        PLATFORM,
        SELLER,
        BUYER
    }
}

