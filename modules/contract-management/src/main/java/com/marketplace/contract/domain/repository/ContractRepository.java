package com.marketplace.contract.domain.repository;

import com.marketplace.contract.domain.model.Contract;
import com.marketplace.contract.domain.valueobject.ContractId;
import com.marketplace.contract.domain.valueobject.ContractStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Domain repository abstraction for contracts.
 */
public interface ContractRepository {

    Optional<Contract> findById(ContractId id);

    Optional<Contract> findById(String id);

    Optional<Contract> findByContractNumber(String contractNumber);

    List<Contract> findByStatus(ContractStatus status);

    List<Contract> findActiveContracts(String tenantId);

    List<Contract> findExpiringBetween(LocalDate start, LocalDate end);

    Contract save(Contract contract);

    void delete(Contract contract);
}
