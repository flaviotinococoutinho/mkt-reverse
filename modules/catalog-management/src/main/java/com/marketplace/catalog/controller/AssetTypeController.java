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
import org.springframework.web.bind.annotation.RestController;

import com.marketplace.catalog.domain.entity.AssetType;
import com.marketplace.catalog.domain.entity.AttributeDefinition;
import com.marketplace.catalog.service.AssetTypeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/catalog/schema")
@RequiredArgsConstructor
public class AssetTypeController {

    private final AssetTypeService service;

    @GetMapping
    public ResponseEntity<List<AssetType>> getAllAssetTypes() {
        return ResponseEntity.ok(service.getAllAssetTypes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssetType> getAssetTypeById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getAssetTypeById(id));
    }

    @PostMapping
    public ResponseEntity<AssetType> createAssetType(@RequestBody AssetType assetType) {
        return new ResponseEntity<>(service.createAssetType(assetType), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AssetType> updateAssetType(@PathVariable Long id, @RequestBody AssetType assetType) {
        return ResponseEntity.ok(service.updateAssetType(id, assetType));
    }

    @PostMapping("/{id}/attributes")
    public ResponseEntity<AssetType> addAttribute(
            @PathVariable Long id, 
            @RequestBody AttributeDefinition attribute) {
        return ResponseEntity.ok(service.addAttribute(id, attribute));
    }
}
