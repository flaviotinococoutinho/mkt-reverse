package com.marketplace.sourcing.domain.repository;

import com.marketplace.sourcing.domain.model.SupplierResponse;
import com.marketplace.sourcing.domain.valueobject.SourcingEventId;
import com.marketplace.sourcing.domain.valueobject.SupplierResponseId;

import java.util.List;
import java.util.Optional;

public interface SupplierResponseRepository {

    Optional<SupplierResponse> findById(SupplierResponseId id);

    List<SupplierResponse> findByEventId(SourcingEventId eventId);

    List<SupplierResponse> findByEventId(String eventId);

    SupplierResponse save(SupplierResponse response);
}
