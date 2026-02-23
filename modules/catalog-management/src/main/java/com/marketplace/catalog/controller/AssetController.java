package com.marketplace.catalog.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.marketplace.catalog.domain.entity.Asset;
import com.marketplace.catalog.domain.entity.AssetRelationship;
import com.marketplace.catalog.domain.entity.RelationshipType;
import com.marketplace.catalog.service.AssetService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/catalog/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService service;

    @GetMapping("/{id}")
    public ResponseEntity<Asset> getAssetById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getAssetById(id));
    }

    @GetMapping("/tag/{tag}")
    public ResponseEntity<Asset> getAssetByTag(@PathVariable String tag) {
        return ResponseEntity.ok(service.getAssetByTag(tag));
    }

    @PostMapping
    public ResponseEntity<Asset> createAsset(@RequestBody Asset asset) {
        return new ResponseEntity<>(service.createAsset(asset), HttpStatus.CREATED);
    }

    // A structured request DTO is preferred here, but using Map directly for dynamic patching
    @PatchMapping("/{id}")
    public ResponseEntity<Asset> updateAsset(
            @PathVariable Long id, 
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String status,
            @RequestBody(required = false) Map<String, Object> attributes) {
        return ResponseEntity.ok(service.updateAsset(id, attributes, status, name));
    }

    @PostMapping("/{sourceId}/relationships/{targetId}")
    public ResponseEntity<AssetRelationship> createRelationship(
            @PathVariable Long sourceId,
            @PathVariable Long targetId,
            @RequestParam RelationshipType type) {
        return new ResponseEntity<>(service.createRelationship(sourceId, targetId, type), HttpStatus.CREATED);
    }

    @GetMapping("/{id}/dependencies")
    public ResponseEntity<List<AssetRelationship>> getAssetDependencies(@PathVariable Long id) {
        return ResponseEntity.ok(service.getAssetDependencies(id));
    }
}
