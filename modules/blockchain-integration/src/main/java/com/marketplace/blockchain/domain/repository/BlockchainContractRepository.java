package com.marketplace.blockchain.domain.repository;

import com.marketplace.blockchain.domain.model.BlockchainContract;
import com.marketplace.blockchain.domain.valueobject.BlockchainContractId;
import com.marketplace.blockchain.domain.valueobject.BlockchainNetwork;
import com.marketplace.blockchain.domain.valueobject.VerificationStatus;

import java.util.List;
import java.util.Optional;

/**
 * Repository abstraction for blockchain contracts.
 */
public interface BlockchainContractRepository {

    Optional<BlockchainContract> findById(BlockchainContractId id);

    Optional<BlockchainContract> findById(String id);

    Optional<BlockchainContract> findByAddress(BlockchainNetwork network, String contractAddress);

    List<BlockchainContract> findByNetwork(BlockchainNetwork network);

    List<BlockchainContract> findByVerificationStatus(VerificationStatus status);

    BlockchainContract save(BlockchainContract contract);

    void delete(BlockchainContract contract);
}
