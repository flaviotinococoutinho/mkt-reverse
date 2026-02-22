package com.marketplace.sourcing.infrastructure.persistence;

import com.marketplace.sourcing.domain.model.SupplierResponse;
import com.marketplace.sourcing.domain.repository.SupplierResponseRepository;
import com.marketplace.sourcing.domain.valueobject.SourcingEventId;
import com.marketplace.sourcing.domain.valueobject.SupplierResponseId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JpaSupplierResponseRepository implements SupplierResponseRepository {

    private final SpringDataSupplierResponseJpaRepository jpa;

    public JpaSupplierResponseRepository(SpringDataSupplierResponseJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<SupplierResponse> findById(SupplierResponseId id) {
        return jpa.findById(id);
    }

    @Override
    public List<SupplierResponse> findByEventId(SourcingEventId eventId) {
        return jpa.findByEventId(eventId);
    }

    @Override
    public List<SupplierResponse> findByEventId(String eventId) {
        return jpa.findByEventId(SourcingEventId.of(eventId));
    }

    @Override
    public SupplierResponse save(SupplierResponse response) {
        return jpa.save(response);
    }
}
