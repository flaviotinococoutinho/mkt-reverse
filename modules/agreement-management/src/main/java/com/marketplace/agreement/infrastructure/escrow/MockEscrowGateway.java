package com.marketplace.agreement.infrastructure.escrow;

import com.marketplace.agreement.application.port.EscrowGateway;
import com.marketplace.agreement.domain.model.Agreement;
import com.marketplace.agreement.domain.valueobject.ResolutionOutcome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Development/Fase-0 stand-in for the PSP escrow adapter.
 *
 * It only simulates the PSP round-trips so the agreement state machine can be
 * exercised end-to-end. The Fase 1 gate REQUIRES replacing this bean with a
 * real adapter for an authorized PSP (Pagar.me, Mercado Pago, Asaas, Iugu,
 * Stripe etc.) before any real money flows — the platform itself never holds
 * funds under any circumstance.
 *
 * Deduplicates by idempotency key, mirroring what a real PSP does, so the
 * application-level retry semantics can be exercised against the mock.
 *
 * Disable with marketplace.escrow.mock=false once a real adapter exists.
 */
@Component
@ConditionalOnProperty(name = "marketplace.escrow.mock", havingValue = "true", matchIfMissing = true)
public class MockEscrowGateway implements EscrowGateway {

    private static final Logger log = LoggerFactory.getLogger(MockEscrowGateway.class);

    private final Map<String, String> processedCommands = new ConcurrentHashMap<>();

    @Override
    public String fund(Agreement agreement, String idempotencyKey) {
        return processedCommands.computeIfAbsent(idempotencyKey, key -> {
            String reference = "mock-escrow-" + UUID.randomUUID();
            log.info("[MOCK PSP] Funding escrow for agreement {} amount {} {} -> {} (idempotency={})",
                agreement.getId(), agreement.getPriceCents(), agreement.getCurrency(), reference, key);
            return reference;
        });
    }

    @Override
    public void release(Agreement agreement, String idempotencyKey) {
        deduplicate(idempotencyKey, () -> log.info("[MOCK PSP] Releasing escrow {} to seller {} (idempotency={})",
            agreement.getEscrowReference(), agreement.getSupplierId(), idempotencyKey));
    }

    @Override
    public void refund(Agreement agreement, String idempotencyKey) {
        deduplicate(idempotencyKey, () -> log.info("[MOCK PSP] Refunding escrow {} to buyer {} (idempotency={})",
            agreement.getEscrowReference(), agreement.getBuyerId(), idempotencyKey));
    }

    @Override
    public void resolve(Agreement agreement, ResolutionOutcome outcome, String idempotencyKey) {
        deduplicate(idempotencyKey, () -> log.info("[MOCK PSP] Resolving escrow {} with outcome {} (idempotency={})",
            agreement.getEscrowReference(), outcome, idempotencyKey));
    }

    private void deduplicate(String idempotencyKey, Runnable command) {
        if (processedCommands.putIfAbsent(idempotencyKey, "done") != null) {
            log.info("[MOCK PSP] Duplicate command suppressed (idempotency={})", idempotencyKey);
            return;
        }
        command.run();
    }
}
