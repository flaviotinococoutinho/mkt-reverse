package com.marketplace.erp.domain.repository;

import com.marketplace.erp.domain.model.ErpConnector;
import com.marketplace.erp.domain.valueobject.ErpConnectorId;
import com.marketplace.erp.domain.valueobject.ErpSystem;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository abstraction for ERP connectors.
 */
public interface ErpConnectorRepository {

    Optional<ErpConnector> findById(ErpConnectorId id);

    Optional<ErpConnector> findById(String id);

    Optional<ErpConnector> findBySystem(String tenantId, ErpSystem system);

    List<ErpConnector> findSyncDue(Instant reference);

    ErpConnector save(ErpConnector connector);

    void delete(ErpConnector connector);
}
