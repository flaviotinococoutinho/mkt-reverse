package com.marketplace.agreement.infrastructure.escrow;

import com.marketplace.agreement.application.port.EscrowGateway;
import com.marketplace.agreement.domain.model.Agreement;
import com.marketplace.agreement.domain.valueobject.ResolutionOutcome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Development/Fase-0 stand-in for the PSP escrow adapter.
 *
 * It only simulates the PSP round-trips so the agreement state machine can be
 * exercised end-to-end. The Fase 1 gate REQUIRES replacing this bean with a
 * real adapter for an authorized PSP (Pagar.me, Mercado Pago, Asaas, Iugu,
 * Stripe etc.) before any real money flows — the platform itself never holds
 * funds under any circumstance.
 *
 * Disable with marketplace.escrow.mock=false once a real adapter exists.
 */
@Component
@ConditionalOnProperty(name = "marketplace.escrow.mock", havingValue = "true", matchIfMissing = true)
public class MockEscrowGateway implements EscrowGateway {

    private static final Logger log = LoggerFactory.getLogger(MockEscrowGateway.class);

    @Override
    public String fund(Agreement agreement) {
        String reference = "mock-escrow-" + UUID.randomUUID();
        log.info("[MOCK PSP] Funding escrow for agreement {} amount {} {} -> {}",
            agreement.getId(), agreement.getPriceCents(), agreement.getCurrency(), reference);
        return reference;
    }

    @Override
    public void release(Agreement agreement) {
        log.info("[MOCK PSP] Releasing escrow {} to seller {}", agreement.getEscrowReference(), agreement.getSupplierId());
    }

    @Override
    public void refund(Agreement agreement) {
        log.info("[MOCK PSP] Refunding escrow {} to buyer {}", agreement.getEscrowReference(), agreement.getBuyerId());
    }

    @Override
    public void resolve(Agreement agreement, ResolutionOutcome outcome) {
        log.info("[MOCK PSP] Resolving escrow {} with outcome {}", agreement.getEscrowReference(), outcome);
    }
}
