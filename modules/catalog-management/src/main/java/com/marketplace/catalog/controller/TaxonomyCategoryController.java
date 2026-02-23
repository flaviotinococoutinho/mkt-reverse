package com.marketplace.catalog.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.marketplace.catalog.domain.entity.TaxonomyCategory;
import com.marketplace.catalog.service.TaxonomyCategoryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/catalog/taxonomy")
@RequiredArgsConstructor
public class TaxonomyCategoryController {

    private final TaxonomyCategoryService service;

    @GetMapping
    public ResponseEntity<List<TaxonomyCategory>> getAllCategories(
            @RequestParam(required = false) Boolean activeOnly) {
        if (Boolean.TRUE.equals(activeOnly)) {
            return ResponseEntity.ok(service.getActiveCategories());
        }
        return ResponseEntity.ok(service.getAllCategories());
    }

    @GetMapping("/{code}")
    public ResponseEntity<TaxonomyCategory> getCategoryByCode(@PathVariable String code) {
        return ResponseEntity.ok(service.getCategoryByCode(code));
    }

    @GetMapping("/parent/{parentId}")
    public ResponseEntity<List<TaxonomyCategory>> getSubcategories(@PathVariable Long parentId) {
        return ResponseEntity.ok(service.getSubcategories(parentId));
    }

    @PostMapping
    public ResponseEntity<TaxonomyCategory> createCategory(@RequestBody TaxonomyCategory category) {
        return new ResponseEntity<>(service.createCategory(category), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaxonomyCategory> updateCategory(@PathVariable Long id, @RequestBody TaxonomyCategory category) {
        return ResponseEntity.ok(service.updateCategory(id, category));
    }
}
