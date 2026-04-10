package com.marketplace.catalog.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.marketplace.catalog.domain.entity.AssetType;

@Repository
public interface AssetTypeRepository extends JpaRepository<AssetType, Long> {
    Optional<AssetType> findByName(String name);
}
