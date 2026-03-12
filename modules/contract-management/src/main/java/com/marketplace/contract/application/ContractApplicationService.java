package com.marketplace.contract.application;

import com.marketplace.contract.domain.model.Contract;
import com.marketplace.contract.domain.repository.ContractRepository;
import com.marketplace.contract.domain.valueobject.*;
import com.marketplace.shared.id.IdGenerator;
import com.marketplace.shared.valueobject.Money;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContractApplicationService {

    private final ContractRepository contractRepository;
    private final IdGenerator idGenerator;

    public ContractApplicationService(ContractRepository contractRepository, IdGenerator idGenerator) {
        this.contractRepository = contractRepository;
        this.idGenerator = idGenerator;
    }

    @Transactional
    public ContractId createContractFromAward(
            String tenantId,
            String sourcingEventId,
            String buyerId,
            String sellerId,
            Money amount
    ) {
        ContractId id = ContractId.of(String.valueOf(idGenerator.nextId()));

        ContractTerm mainTerm = ContractTerm.of(
                java.time.LocalDate.now(),
                java.time.LocalDate.now().plusMonths(1),
                false,
                null,
                30
        );

        Contract contract = Contract.create(
                tenantId,
                sourcingEventId,
                ContractType.SPOT,
                amount,
                mainTerm,
                java.util.Set.of(buyerId, sellerId)
        );

        contractRepository.save(contract);
        return id;
    }

    public Contract getContract(String id) {
        return contractRepository.findById(ContractId.of(id))
                .orElseThrow(() -> new IllegalArgumentException("Contract not found: " + id));
    }
}
