package com.marketplace.payment.domain.repository;

import com.marketplace.payment.domain.model.PaymentConnector;
import com.marketplace.payment.domain.valueobject.IntegrationStatus;
import com.marketplace.payment.domain.valueobject.PaymentConnectorId;

import java.util.List;
import java.util.Optional;

/**
 * Repository abstraction for payment connectors.
 */
public interface PaymentConnectorRepository {

    Optional<PaymentConnector> findById(PaymentConnectorId id);

    Optional<PaymentConnector> findById(String id);

    Optional<PaymentConnector> findByTenantAndProvider(String tenantId, String provider);

    List<PaymentConnector> findByStatus(IntegrationStatus status);

    List<PaymentConnector> findActiveConnectors(String tenantId);

    PaymentConnector save(PaymentConnector connector);

    void delete(PaymentConnector connector);
}
