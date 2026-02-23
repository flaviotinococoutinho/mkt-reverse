package com.marketplace.catalog.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.marketplace.catalog.domain.entity.TaxonomyCategory;
import com.marketplace.catalog.repository.TaxonomyCategoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaxonomyCategoryService {

    private final TaxonomyCategoryRepository repository;

    @Transactional(readOnly = true)
    public List<TaxonomyCategory> getAllCategories() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public List<TaxonomyCategory> getActiveCategories() {
        return repository.findByActiveTrue();
    }

    @Transactional(readOnly = true)
    public TaxonomyCategory getCategoryByCode(String code) {
        return repository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with code: " + code));
    }

    @Transactional(readOnly = true)
    public List<TaxonomyCategory> getSubcategories(Long parentId) {
        return repository.findByParentId(parentId);
    }

    @Transactional
    public TaxonomyCategory createCategory(TaxonomyCategory category) {
        if (repository.findByCode(category.getCode()).isPresent()) {
            throw new IllegalArgumentException("Category code already exists: " + category.getCode());
        }
        return repository.save(category);
    }

    @Transactional
    public TaxonomyCategory updateCategory(Long id, TaxonomyCategory updatedInfo) {
        TaxonomyCategory existing = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + id));

        existing.setName(updatedInfo.getName());
        existing.setDescription(updatedInfo.getDescription());
        existing.setActive(updatedInfo.isActive());
        
        return repository.save(existing);
    }
}
