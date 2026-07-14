package com.marketplace.gateway.api;

import com.marketplace.agreement.application.AgreementApplicationService;
import com.marketplace.agreement.domain.model.Agreement;
import com.marketplace.sourcing.application.service.SourcingEventApplicationService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

/**
 * Coordinates acceptance across the sourcing and agreement contexts in a
 * single transaction: accepting a proposal awards the event AND opens the
 * agreement (contract) with its immutable snapshot. If the ticket exceeds the
 * kickoff escrow ceiling, the whole acceptance rolls back — the platform
 * declares itself unavailable instead of carrying unpriced risk.
 */
@Service
public class AcceptanceCoordinator {

    private final SourcingEventApplicationService sourcingService;
    private final AgreementApplicationService agreementService;

    public AcceptanceCoordinator(
        SourcingEventApplicationService sourcingService,
        AgreementApplicationService agreementService
    ) {
        this.sourcingService = sourcingService;
        this.agreementService = agreementService;
    }

    @Transactional
    public Agreement acceptAndOpenAgreement(String eventId, String responseId, String tenantId) {
        sourcingService.acceptResponse(eventId, responseId, tenantId);
        return agreementService.openFromAcceptance(eventId, responseId);
    }
}
