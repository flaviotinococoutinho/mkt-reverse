package com.marketplace.agreement.domain.event;

import com.marketplace.agreement.domain.valueobject.AgreementStatus;
import com.marketplace.shared.domain.event.DomainEvent;
import com.marketplace.shared.domain.event.EventMetadata;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Single audit event for every agreement state transition — the state machine
 * is mirrored 1:1 into the event trail (probative function via outbox).
 */
public class AgreementStatusChangedEvent implements DomainEvent {

    private final String aggregateId;
    private final AgreementStatus fromStatus;
    private final AgreementStatus toStatus;
    private final String detail;
    private final EventMetadata metadata;

    public AgreementStatusChangedEvent(
        String agreementId,
        AgreementStatus fromStatus,
        AgreementStatus toStatus,
        String detail
    ) {
        this.aggregateId = agreementId;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.detail = detail;

        Map<String, Object> props = new HashMap<>();
        props.put("from", fromStatus != null ? fromStatus.name() : "");
        props.put("to", toStatus != null ? toStatus.name() : "");
        props.put("detail", detail != null ? detail : "");
        this.metadata = EventMetadata.create(
            getEventType(),
            getEventVersion(),
            Instant.now(),
            agreementId,
            "Agreement",
            props
        );
    }

    @Override
    public String getEventType() {
        return "AgreementStatusChangedEvent";
    }

    @Override
    public String getEventVersion() {
        return "1.0";
    }

    @Override
    public Instant getOccurredAt() {
        return metadata.getOccurredAt();
    }

    @Override
    public String getAggregateId() {
        return aggregateId;
    }

    @Override
    public EventMetadata getMetadata() {
        return metadata;
    }

    public AgreementStatus getFromStatus() {
        return fromStatus;
    }

    public AgreementStatus getToStatus() {
        return toStatus;
    }

    public String getDetail() {
        return detail;
    }
}
