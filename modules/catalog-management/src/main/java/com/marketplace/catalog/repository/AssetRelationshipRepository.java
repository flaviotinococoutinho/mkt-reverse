package com.marketplace.catalog.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.marketplace.catalog.domain.entity.AssetRelationship;

@Repository
public interface AssetRelationshipRepository extends JpaRepository<AssetRelationship, Long> {
    List<AssetRelationship> findBySourceAssetId(Long sourceAssetId);
    List<AssetRelationship> findByTargetAssetId(Long targetAssetId);
}
