package com.marketplace.sourcing.infrastructure.persistence;

import com.marketplace.sourcing.domain.model.SupplierResponse;
import com.marketplace.sourcing.domain.valueobject.SourcingEventId;
import com.marketplace.sourcing.domain.valueobject.SupplierResponseId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataSupplierResponseJpaRepository extends JpaRepository<SupplierResponse, SupplierResponseId> {
    List<SupplierResponse> findByEventId(SourcingEventId eventId);
}
