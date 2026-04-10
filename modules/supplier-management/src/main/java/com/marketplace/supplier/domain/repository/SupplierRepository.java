package com.marketplace.supplier.domain.repository;

import com.marketplace.supplier.domain.model.Supplier;
import com.marketplace.supplier.domain.valueobject.ComplianceStatus;
import com.marketplace.supplier.domain.valueobject.SupplierId;
import com.marketplace.supplier.domain.valueobject.SupplierStatus;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Domain repository abstraction for suppliers.
 */
public interface SupplierRepository {

    Optional<Supplier> findById(SupplierId id);

    Optional<Supplier> findById(String id);

    Optional<Supplier> findByTaxIdentifier(String taxNumber);

    List<Supplier> findByStatus(SupplierStatus status);

    List<Supplier> findByComplianceStatus(ComplianceStatus status);

    List<Supplier> findActiveByCategories(String tenantId, Set<String> categories);

    Supplier save(Supplier supplier);

    void delete(Supplier supplier);
}
