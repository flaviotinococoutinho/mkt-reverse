package com.marketplace.catalog.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.marketplace.catalog.domain.entity.Asset;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {
    Optional<Asset> findByTag(String tag);
    
    List<Asset> findByAssetTypeId(Long assetTypeId);
    
    List<Asset> findByTaxonomyCategoryId(Long taxonomyCategoryId);
    
    // Example query using Postgres JSONB to find by dynamic attribute
    @Query(value = "SELECT * FROM assets WHERE attributes->>:key = :value", nativeQuery = true)
    List<Asset> findByAttributesContaining(@Param("key") String key, @Param("value") String value);
}
