package com.marketplace.contract.application;

import com.marketplace.contract.domain.model.Contract;
import com.marketplace.contract.domain.repository.ContractRepository;
import com.marketplace.contract.domain.valueobject.*;
import com.marketplace.shared.id.IdGenerator;
import com.marketplace.shared.valueobject.Money;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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

        // Define parties
        ContractParty buyer = ContractParty.of(PartyRole.BUYER, buyerId, "System Generated", "system@example.com");
        ContractParty seller = ContractParty.of(PartyRole.SUPPLIER, sellerId, "System Generated", "system@example.com");

        // MVP Terms: simple fixed price contract for 1 year
        LocalDate today = LocalDate.now();
        ContractTerm mainTerm = ContractTerm.of(
            today,
            today.plusYears(1),
            false,
            null,
            30
        );

        Contract contract = Contract.create(
                tenantId,
                sourcingEventId, // Use sourcingEventId as contractNumber for MVP
                ContractType.SPOT, // Using SPOT since PURCHASE_ORDER doesn't exist in enum
                amount,
                mainTerm,
                java.util.Collections.emptySet() // empty requirements
        );
        contract.linkSourcingContext(sourcingEventId, null);
        contract.addParty(buyer);
        contract.addParty(seller);

        contractRepository.save(contract);
        id = contract.getId();
        return id;
    }

    public Contract getContract(String id) {
        return contractRepository.findById(ContractId.of(id))
                .orElseThrow(() -> new IllegalArgumentException("Contract not found: " + id));
    }
}
