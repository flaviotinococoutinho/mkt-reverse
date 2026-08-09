package com.marketplace.agreement.infrastructure.scheduler;

import com.marketplace.agreement.application.AgreementApplicationService;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Drives the time-based transitions of the agreement state machine:
 * - PENDING_FUNDING past deadline → LAPSED (contract dissolves)
 * - FUNDED past shipping deadline → SELLER_DEFAULTED (refund + penalty)
 * - DELIVERED past inspection window → RELEASED (automatic release)
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
        int lapsed = service.lapseOverdueFunding(now);
        int defaulted = service.defaultOverdueShipments(now);
        int released = service.autoReleaseAfterInspection(now);
        if (lapsed + defaulted + released > 0) {
            log.info("Agreement lifecycle sweep: lapsed={}, sellerDefaulted={}, autoReleased={}",
                lapsed, defaulted, released);
        }
    }
}
