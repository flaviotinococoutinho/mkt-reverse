package com.marketplace.blockchain.domain.model;

import com.marketplace.blockchain.domain.event.BlockchainContractRegisteredEvent;
import com.marketplace.blockchain.domain.event.BlockchainContractVerificationChangedEvent;
import com.marketplace.blockchain.domain.event.BlockchainEventRecordedEvent;
import com.marketplace.blockchain.domain.valueobject.BlockchainContractId;
import com.marketplace.blockchain.domain.valueobject.BlockchainNetwork;
import com.marketplace.blockchain.domain.valueobject.ContractDeployment;
import com.marketplace.blockchain.domain.valueobject.LedgerEvent;
import com.marketplace.blockchain.domain.valueobject.VerificationStatus;
import com.marketplace.shared.domain.model.AggregateRoot;
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
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Blockchain contract aggregate storing deployment and verification lifecycle.
 */
@Entity
@Table(name = "BLK_CONTRACTS", indexes = {
    @Index(name = "idx_blk_tenant", columnList = "tenant_id"),
    @Index(name = "idx_blk_network", columnList = "network"),
    @Index(name = "idx_blk_contract_name", columnList = "contract_name")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BlockchainContract extends AggregateRoot<BlockchainContractId> {

    @EmbeddedId
    private BlockchainContractId id;

    @Column(name = "tenant_id", nullable = false, length = 36)
    private String tenantId;

    @Column(name = "contract_name", nullable = false, length = 120)
    private String contractName;

    @Enumerated(EnumType.STRING)
    @Column(name = "network", nullable = false, length = 40)
    private BlockchainNetwork network;

    @Embedded
    private ContractDeployment deployment;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 20)
    private VerificationStatus verificationStatus;

    @Column(name = "abi", columnDefinition = "TEXT")
    private String abi;

    @Column(name = "bytecode", columnDefinition = "TEXT")
    private String bytecode;

    @Column(name = "contract_version", length = 40)
    private String contractVersion;

    @Column(name = "owner_id", length = 36)
    private String ownerId;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "last_synced_at")
    private Instant lastSyncedAt;

    @Column(name = "verification_reason", length = 400)
    private String verificationReason;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "BLK_LEDGER_EVENTS", joinColumns = @JoinColumn(name = "contract_id"))
    private Set<LedgerEvent> ledgerEvents = new HashSet<>();

    private BlockchainContract(
        BlockchainContractId id,
        String tenantId,
        String contractName,
        BlockchainNetwork network,
        ContractDeployment deployment,
        String abi,
        String bytecode,
        String contractVersion,
        String ownerId
    ) {
        this.id = id;
        this.tenantId = tenantId;
        this.contractName = contractName;
        this.network = network;
        this.deployment = deployment;
        this.abi = abi;
        this.bytecode = bytecode;
        this.contractVersion = contractVersion;
        this.ownerId = ownerId;
        this.verificationStatus = VerificationStatus.PENDING;
        this.active = true;
    }

    public static BlockchainContract register(
        String tenantId,
        String contractName,
        BlockchainNetwork network,
        ContractDeployment deployment,
        String abi,
        String bytecode,
        String contractVersion,
        String ownerId
    ) {
        Objects.requireNonNull(tenantId, "tenantId is required");
        Objects.requireNonNull(contractName, "contractName is required");
        Objects.requireNonNull(network, "network is required");
        Objects.requireNonNull(deployment, "deployment is required");

        BlockchainContract contract = new BlockchainContract(
            BlockchainContractId.generate(),
            tenantId.trim(),
            contractName.trim(),
            network,
            deployment,
            abi,
            bytecode,
            contractVersion,
            ownerId
        );
        contract.addDomainEvent(new BlockchainContractRegisteredEvent(
            contract.getId().asString(),
            contract.getNetwork(),
            contract.getDeployment().getContractAddress()
        ));
        contract.markAsCreated();
        return contract;
    }

    public void markVerified() {
        this.verificationStatus = VerificationStatus.VERIFIED;
        this.verificationReason = null;
        addDomainEvent(new BlockchainContractVerificationChangedEvent(id.asString(), verificationStatus, Instant.now(), null));
        markAsUpdated();
    }

    public void markVerificationFailed(String reason) {
        this.verificationStatus = VerificationStatus.FAILED;
        this.verificationReason = reason;
        addDomainEvent(new BlockchainContractVerificationChangedEvent(id.asString(), verificationStatus, Instant.now(), reason));
        markAsUpdated();
    }

    public void recordLedgerEvent(LedgerEvent event) {
        ledgerEvents.add(event);
        addDomainEvent(new BlockchainEventRecordedEvent(id.asString(), event.getEventName(), event.getBlockNumber(), event.getEmittedAt()));
        markAsUpdated();
    }

    public void deactivate() {
        this.active = false;
        markAsUpdated();
    }

    public void syncWithLedger() {
        this.lastSyncedAt = Instant.now();
        markAsUpdated();
    }

    @Override
    public void validate() {
        if (id == null) {
            throw new IllegalStateException("BlockchainContract id cannot be null");
        }
        if (tenantId == null || tenantId.trim().isEmpty()) {
            throw new IllegalStateException("tenantId cannot be null");
        }
        if (contractName == null || contractName.trim().isEmpty()) {
            throw new IllegalStateException("contractName cannot be null");
        }
        if (network == null) {
            throw new IllegalStateException("network cannot be null");
        }
        if (deployment == null) {
            throw new IllegalStateException("deployment cannot be null");
        }
        if (verificationStatus == null) {
            throw new IllegalStateException("verificationStatus cannot be null");
        }
    }
}
