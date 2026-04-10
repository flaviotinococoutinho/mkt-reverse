package com.marketplace.contract.domain.model;

import com.marketplace.contract.domain.event.ContractCreatedEvent;
import com.marketplace.contract.domain.event.ContractMilestoneCompletedEvent;
import com.marketplace.contract.domain.event.ContractStatusChangedEvent;
import com.marketplace.contract.domain.valueobject.ContractId;
import com.marketplace.contract.domain.valueobject.ContractParty;
import com.marketplace.contract.domain.valueobject.ContractStatus;
import com.marketplace.contract.domain.valueobject.ContractTerm;
import com.marketplace.contract.domain.valueobject.ContractType;
import com.marketplace.contract.domain.valueobject.Milestone;
import com.marketplace.contract.domain.valueobject.PartyRole;
import com.marketplace.shared.domain.model.AggregateRoot;
import com.marketplace.shared.valueobject.Money;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Contract aggregate covering draft to renewal lifecycle with smart contract hooks.
 */
@Entity
@Table(name = "CTR_CONTRACTS", indexes = {
    @Index(name = "idx_ctr_tenant", columnList = "tenant_id"),
    @Index(name = "idx_ctr_status", columnList = "status"),
    @Index(name = "idx_ctr_contract_number", columnList = "contract_number", unique = true)
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Contract extends AggregateRoot<ContractId> {

    @EmbeddedId
    private ContractId id;

    @Column(name = "tenant_id", nullable = false, length = 36)
    private String tenantId;

    @Column(name = "contract_number", nullable = false, length = 50)
    private String contractNumber;

    @Column(name = "sourcing_event_id", length = 36)
    private String sourcingEventId;

    @Column(name = "auction_id", length = 36)
    private String auctionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "contract_type", nullable = false, length = 30)
    private ContractType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private ContractStatus status;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "total_value", precision = 19, scale = 4)),
        @AttributeOverride(name = "currency", column = @Column(name = "value_currency", length = 3))
    })
    private Money totalValue;

    @Embedded
    private ContractTerm term;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "CTR_CONTRACT_PARTIES", joinColumns = @JoinColumn(name = "contract_id"))
    private Set<ContractParty> parties = new HashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "CTR_CONTRACT_REQUIREMENTS", joinColumns = @JoinColumn(name = "contract_id"))
    @Column(name = "requirement", length = 200)
    private Set<String> complianceRequirements = new HashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "CTR_CONTRACT_MILESTONES", joinColumns = @JoinColumn(name = "contract_id"))
    @OrderColumn(name = "sequence")
    private List<Milestone> milestones = new ArrayList<>();

    @Column(name = "blockchain_hash", length = 128)
    private String blockchainHash;

    @Column(name = "signed_at")
    private Instant signedAt;

    @Column(name = "terminated_at")
    private Instant terminatedAt;

    @Column(name = "termination_reason", length = 500)
    private String terminationReason;

    private Contract(
        ContractId id,
        String tenantId,
        String contractNumber,
        ContractType type,
        ContractStatus status,
        Money totalValue,
        ContractTerm term,
        Set<String> requirements
    ) {
        this.id = id;
        this.tenantId = tenantId;
        this.contractNumber = contractNumber;
        this.type = type;
        this.status = status;
        this.totalValue = totalValue;
        this.term = term;
        if (requirements != null) {
            this.complianceRequirements.addAll(requirements);
        }
    }

    public static Contract create(
        String tenantId,
        String contractNumber,
        ContractType type,
        Money totalValue,
        ContractTerm term,
        Set<String> requirements
    ) {
        Objects.requireNonNull(tenantId, "tenantId is required");
        Objects.requireNonNull(contractNumber, "contractNumber is required");
        Objects.requireNonNull(type, "type is required");
        Objects.requireNonNull(totalValue, "totalValue is required");
        Objects.requireNonNull(term, "term is required");

        Contract contract = new Contract(
            ContractId.generate(),
            tenantId.trim(),
            contractNumber.trim(),
            type,
            ContractStatus.DRAFT,
            totalValue,
            term,
            requirements
        );

        contract.markAsCreated();
        contract.addDomainEvent(new ContractCreatedEvent(
            contract.getId().asString(),
            contract.getContractNumber(),
            contract.getType(),
            contract.getTotalValue()
        ));
        return contract;
    }

    public void linkSourcingContext(String sourcingEventId, String auctionId) {
        this.sourcingEventId = sourcingEventId;
        this.auctionId = auctionId;
        markAsUpdated();
    }

    public void addParty(ContractParty party) {
        parties.add(party);
        markAsUpdated();
    }

    public void markPartySigned(PartyRole role) {
        List<ContractParty> updated = new ArrayList<>();
        boolean changed = false;
        for (ContractParty party : parties) {
            if (party.getRole() == role && !party.isSigned()) {
                updated.add(party.markSigned());
                changed = true;
            } else {
                updated.add(party);
            }
        }
        if (!changed) {
            throw new IllegalStateException("No party found awaiting signature for role " + role);
        }
        parties.clear();
        parties.addAll(updated);

        if (parties.stream().allMatch(ContractParty::isSigned)) {
            this.status = ContractStatus.PENDING_SIGNATURE;
            this.signedAt = Instant.now();
            addDomainEvent(new ContractStatusChangedEvent(
                id.asString(),
                ContractStatus.PENDING_SIGNATURE,
                Instant.now(),
                null
            ));
        }
        markAsUpdated();
    }

    public void activate() {
        transitionStatus(ContractStatus.ACTIVE, null);
    }

    public void suspend(String reason) {
        transitionStatus(ContractStatus.SUSPENDED, reason);
    }

    public void resume() {
        if (status != ContractStatus.SUSPENDED) {
            throw new IllegalStateException("Contract must be suspended to resume");
        }
        transitionStatus(ContractStatus.ACTIVE, null);
    }

    public void terminate(String reason) {
        transitionStatus(ContractStatus.TERMINATED, reason);
        this.terminatedAt = Instant.now();
        this.terminationReason = reason;
    }

    public void expire() {
        transitionStatus(ContractStatus.EXPIRED, "Term ended");
    }

    public void renew(ContractTerm newTerm) {
        if (!status.isActive()) {
            throw new IllegalStateException("Only active contracts can be renewed");
        }
        this.term = newTerm;
        transitionStatus(ContractStatus.RENEWED, null);
    }

    public void addMilestone(Milestone milestone) {
        milestones.add(milestone);
        markAsUpdated();
    }

    public void completeMilestone(String milestoneName) {
        List<Milestone> updated = new ArrayList<>();
        boolean completed = false;
        for (Milestone milestone : milestones) {
            if (!milestone.isCompleted() && milestone.getName().equalsIgnoreCase(milestoneName)) {
                updated.add(milestone.markCompleted());
                completed = true;
            } else {
                updated.add(milestone);
            }
        }
        if (!completed) {
            throw new IllegalStateException("Milestone not found or already completed");
        }
        milestones.clear();
        milestones.addAll(updated);
        addDomainEvent(new ContractMilestoneCompletedEvent(id.asString(), milestoneName, Instant.now()));
        markAsUpdated();
    }

    public boolean isExpired() {
        return term.isExpired(LocalDate.now());
    }

    public Optional<Milestone> findMilestone(String name) {
        return milestones.stream().filter(m -> m.getName().equalsIgnoreCase(name)).findFirst();
    }

    private void transitionStatus(ContractStatus target, String reason) {
        if (!status.canTransitionTo(target)) {
            throw new IllegalStateException("Cannot transition contract from " + status + " to " + target);
        }
        this.status = target;
        addDomainEvent(new ContractStatusChangedEvent(
            id.asString(),
            target,
            Instant.now(),
            reason
        ));
        markAsUpdated();
    }

    @Override
    public void validate() {
        if (id == null) {
            throw new IllegalStateException("Contract id cannot be null");
        }
        if (tenantId == null || tenantId.trim().isEmpty()) {
            throw new IllegalStateException("tenantId cannot be null");
        }
        if (contractNumber == null || contractNumber.trim().isEmpty()) {
            throw new IllegalStateException("contractNumber cannot be null");
        }
        if (type == null) {
            throw new IllegalStateException("type cannot be null");
        }
        if (status == null) {
            throw new IllegalStateException("status cannot be null");
        }
        if (totalValue == null) {
            throw new IllegalStateException("totalValue cannot be null");
        }
        if (term == null) {
            throw new IllegalStateException("term cannot be null");
        }
    }

    public Set<ContractParty> getParties() {
        return Collections.unmodifiableSet(parties);
    }

    public List<Milestone> getMilestones() {
        return Collections.unmodifiableList(milestones);
    }

    public Set<String> getComplianceRequirements() {
        return Collections.unmodifiableSet(complianceRequirements);
    }
}
