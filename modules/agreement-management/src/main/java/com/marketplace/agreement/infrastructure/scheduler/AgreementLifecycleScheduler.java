package com.marketplace.agreement.infrastructure.scheduler;

import com.marketplace.agreement.application.AgreementApplicationService;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Drives the time-based transitions of the agreement state machine:
 * - PENDING_FUNDING past deadline → LAPSED (contract dissolves)
 * - FUNDED past shipping deadline → SELLER_DEFAULTED (refund + penalty)
 * - SHIPPED past delivery deadline → SELLER_DEFAULTED (refund — SHIPPED is
 *   never a dead end with the buyer's money locked at the PSP)
 * - DELIVERED past inspection window → RELEASED (automatic release)
 *
 * Each agreement is processed in its OWN transaction (the *One methods of the
 * service): a poison item is logged and skipped, never reverting escrow
 * commands already emitted for the other items of the sweep.
 */
@Component
@ConditionalOnProperty(name = "marketplace.agreement.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class AgreementLifecycleScheduler {

    private static final Logger log = LoggerFactory.getLogger(AgreementLifecycleScheduler.class);

    private final AgreementApplicationService service;

    public AgreementLifecycleScheduler(AgreementApplicationService service) {
        this.service = service;
    }

    @Scheduled(fixedDelayString = "${marketplace.agreement.scheduler.fixed-delay-ms:300000}")
    @SchedulerLock(name = "agreementLifecycleLock", lockAtMostFor = "4m", lockAtLeastFor = "5s")
    public void run() {
        Instant now = Instant.now();
        int lapsed = sweep("lapse", service.findLapseCandidates(now), service::lapseOne, now);
        int defaulted = sweep("sellerDefault", service.findSellerDefaultCandidates(now), service::sellerDefaultOne, now);
        int deliveryOverdue = sweep("deliveryOverdue", service.findDeliveryOverdueCandidates(now), service::deliveryOverdueOne, now);
        int released = sweep("autoRelease", service.findAutoReleaseCandidates(now), service::autoReleaseOne, now);
        if (lapsed + defaulted + deliveryOverdue + released > 0) {
            log.info("Agreement lifecycle sweep: lapsed={}, sellerDefaulted={}, deliveryOverdue={}, autoReleased={}",
                lapsed, defaulted, deliveryOverdue, released);
        }
    }

    private int sweep(String name, List<String> candidateIds, BiConsumer<String, Instant> operation, Instant now) {
        int processed = 0;
        for (String agreementId : candidateIds) {
            try {
                operation.accept(agreementId, now);
                processed++;
            } catch (Exception e) {
                log.warn("Agreement lifecycle sweep '{}' skipped agreement {} (will retry next tick): {}",
                    name, agreementId, e.getMessage());
            }
        }
        return processed;
    }
}
