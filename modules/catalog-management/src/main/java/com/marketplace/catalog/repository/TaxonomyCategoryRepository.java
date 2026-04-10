package com.marketplace.catalog.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.marketplace.catalog.domain.entity.TaxonomyCategory;

@Repository
public interface TaxonomyCategoryRepository extends JpaRepository<TaxonomyCategory, Long> {
    Optional<TaxonomyCategory> findByCode(String code);
    List<TaxonomyCategory> findByParentId(Long parentId);
    List<TaxonomyCategory> findByActiveTrue();
}
