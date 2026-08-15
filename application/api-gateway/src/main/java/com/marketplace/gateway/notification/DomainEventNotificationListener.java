package com.marketplace.gateway.notification;

import com.marketplace.agreement.domain.event.AgreementStatusChangedEvent;
import com.marketplace.notification.application.NotificationApplicationService;
import com.marketplace.notification.domain.valueobject.NotificationPriority;
import com.marketplace.sourcing.domain.event.SupplierResponseStatusChangedEvent;
import com.marketplace.sourcing.domain.model.SourcingEvent;
import com.marketplace.sourcing.domain.repository.SourcingEventRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Composition-root listener: translates domain events into in-app
 * notifications for the counterpart user.
 *
 * Runs AFTER_COMMIT in its own transaction so the business operation never
 * fails because of a notification, and notifications are only produced for
 * changes that actually committed. The probative trail lives in the outbox
 * (same transaction as the business change); this listener is the
 * user-facing latency killer: "sua proposta foi aceita", "pague o escrow",
 * "envie o item", "dinheiro liberado".
 */
@Component
public class DomainEventNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(DomainEventNotificationListener.class);

    private final NotificationApplicationService notifications;
    private final SourcingEventRepository sourcingEventRepository;

    public DomainEventNotificationListener(
        NotificationApplicationService notifications,
        SourcingEventRepository sourcingEventRepository
    ) {
        this.notifications = notifications;
        this.sourcingEventRepository = sourcingEventRepository;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void onSupplierResponseStatusChanged(SupplierResponseStatusChangedEvent event) {
        try {
            SourcingEvent sourcingEvent = sourcingEventRepository.findById(event.getSourcingEventId()).orElse(null);
            if (sourcingEvent == null) {
                return;
            }
            String tenantId = sourcingEvent.getBuyerContext().getTenantId();
            String buyerId = sourcingEvent.getBuyerContext().getContactId();
            String title = sourcingEvent.getTitle();

            switch (event.getNewStatus()) {
                case SUBMITTED -> notifications.notifyInApp(
                    tenantId, buyerId, "proposal.submitted", NotificationPriority.NORMAL,
                    "Nova proposta na sua intenção",
                    "Você recebeu uma nova proposta selada em \"" + title + "\".",
                    payload(event));
                case ACCEPTED -> notifications.notifyInApp(
                    tenantId, event.getSupplierId(), "proposal.accepted", NotificationPriority.CRITICAL,
                    "Sua proposta foi aceita!",
                    "Sua proposta para \"" + title + "\" foi aceita. O contrato foi aberto e aguarda o pagamento em custódia.",
                    payload(event));
                case REJECTED -> notifications.notifyInApp(
                    tenantId, event.getSupplierId(), "proposal.rejected", NotificationPriority.NORMAL,
                    "Proposta não selecionada",
                    "Sua proposta para \"" + title + "\" não foi selecionada desta vez.",
                    payload(event));
                default -> { /* no notification for other transitions */ }
            }
        } catch (Exception e) {
            log.warn("Failed to create in-app notification for response {}: {}", event.getAggregateId(), e.getMessage());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void onAgreementStatusChanged(AgreementStatusChangedEvent event) {
        try {
            String tenantId = event.getTenantId() != null ? event.getTenantId() : "tenant-default";
            String buyer = event.getBuyerId();
            String supplier = event.getSupplierId();
            String agreementId = event.getAggregateId();

            switch (event.getToStatus()) {
                case PENDING_FUNDING -> notifications.notifyInApp(
                    tenantId, buyer, "agreement.pending_funding", NotificationPriority.CRITICAL,
                    "Contrato aberto — pague em custódia",
                    "Seu aceite abriu o contrato " + agreementId
                        + ". Efetue o pagamento em custódia dentro da janela para o contrato entrar em vigor.",
                    payload(event));
                case FUNDED -> notifications.notifyInApp(
                    tenantId, supplier, "agreement.funded", NotificationPriority.CRITICAL,
                    "Pagamento em custódia — envie o item",
                    "O comprador pagou em custódia no contrato " + agreementId
                        + ". Registre o envio com rastreio dentro da janela.",
                    payload(event));
                case SHIPPED -> notifications.notifyInApp(
                    tenantId, buyer, "agreement.shipped", NotificationPriority.HIGH,
                    "Item enviado",
                    "O vendedor registrou o envio no contrato " + agreementId + ". Confirme o recebimento quando chegar.",
                    payload(event));
                case DELIVERED -> notifications.notifyInApp(
                    tenantId, buyer, "agreement.delivered", NotificationPriority.HIGH,
                    "Janela de inspeção aberta",
                    "Entrega confirmada no contrato " + agreementId
                        + ". Você tem a janela de inspeção para liberar o pagamento ou abrir disputa.",
                    payload(event));
                case RELEASED -> notifications.notifyInApp(
                    tenantId, supplier, "agreement.released", NotificationPriority.CRITICAL,
                    "Pagamento liberado",
                    "O escrow do contrato " + agreementId + " foi liberado para você.",
                    payload(event));
                case DISPUTED -> notifications.notifyInApp(
                    tenantId, supplier, "agreement.disputed", NotificationPriority.CRITICAL,
                    "Disputa aberta",
                    "O comprador abriu disputa no contrato " + agreementId + ". A resolução (ODR) foi iniciada.",
                    payload(event));
                case LAPSED -> notifications.notifyInApp(
                    tenantId, supplier, "agreement.lapsed", NotificationPriority.HIGH,
                    "Contrato caducou sem pagamento",
                    "O contrato " + agreementId + " caducou: o comprador não pagou dentro da janela.",
                    payload(event));
                case SELLER_DEFAULTED -> notifications.notifyInApp(
                    tenantId, buyer, "agreement.seller_defaulted", NotificationPriority.CRITICAL,
                    "Reembolso emitido",
                    "O vendedor não cumpriu o prazo no contrato " + agreementId
                        + ". O reembolso integral foi comandado ao PSP.",
                    payload(event));
                case RESOLVED_REFUNDED, RESOLVED_PARTIAL, RESOLVED_RELEASED -> {
                    String subject = "Disputa resolvida";
                    String body = "A disputa do contrato " + agreementId + " foi resolvida: " + event.getToStatus() + ".";
                    notifications.notifyInApp(tenantId, buyer, "agreement.resolved", NotificationPriority.HIGH,
                        subject, body, payload(event));
                    notifications.notifyInApp(tenantId, supplier, "agreement.resolved", NotificationPriority.HIGH,
                        subject, body, payload(event));
                }
                default -> { /* CANCELLED etc.: no notification in MVP */ }
            }
        } catch (Exception e) {
            log.warn("Failed to create in-app notification for agreement {}: {}", event.getAggregateId(), e.getMessage());
        }
    }

    private static String payload(SupplierResponseStatusChangedEvent event) {
        return "{\"responseId\":\"" + event.getAggregateId()
            + "\",\"sourcingEventId\":\"" + event.getSourcingEventId() + "\"}";
    }

    private static String payload(AgreementStatusChangedEvent event) {
        return "{\"agreementId\":\"" + event.getAggregateId()
            + "\",\"sourcingEventId\":\"" + (event.getSourcingEventId() != null ? event.getSourcingEventId() : "")
            + "\",\"status\":\"" + event.getToStatus() + "\"}";
    }
}
