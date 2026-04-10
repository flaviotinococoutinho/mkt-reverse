package com.marketplace.blockchain.domain.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

/**
 * Deployment details for smart contracts.
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ContractDeployment implements Serializable {

    @Column(name = "tx_hash", nullable = false, length = 80)
    private String transactionHash;

    @Column(name = "contract_address", nullable = false, length = 80)
    private String contractAddress;

    @Column(name = "block_number", nullable = false)
    private Long blockNumber;

    @Column(name = "deployed_at", nullable = false)
    private Instant deployedAt;

    @Column(name = "deployer_address", nullable = false, length = 80)
    private String deployerAddress;

    public static ContractDeployment of(String txHash, String contractAddress, Long blockNumber, String deployerAddress) {
        if (txHash == null || txHash.trim().isEmpty()) {
            throw new IllegalArgumentException("Transaction hash cannot be blank");
        }
        if (contractAddress == null || contractAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("Contract address cannot be blank");
        }
        if (blockNumber == null || blockNumber < 0) {
            throw new IllegalArgumentException("Block number must be positive");
        }
        if (deployerAddress == null || deployerAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("Deployer address cannot be blank");
        }
        return new ContractDeployment(txHash.trim(), contractAddress.trim(), blockNumber, Instant.now(), deployerAddress.trim());
    }
}
