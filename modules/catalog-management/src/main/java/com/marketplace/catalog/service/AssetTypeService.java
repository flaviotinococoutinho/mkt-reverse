package com.marketplace.catalog.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.marketplace.catalog.domain.entity.AssetType;
import com.marketplace.catalog.domain.entity.AttributeDefinition;
import com.marketplace.catalog.repository.AssetTypeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AssetTypeService {

    private final AssetTypeRepository repository;

    @Transactional(readOnly = true)
    public List<AssetType> getAllAssetTypes() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public AssetType getAssetTypeById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("AssetType not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public AssetType getAssetTypeByName(String name) {
        return repository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("AssetType not found with name: " + name));
    }

    @Transactional
    public AssetType createAssetType(AssetType assetType) {
        if (repository.findByName(assetType.getName()).isPresent()) {
            throw new IllegalArgumentException("AssetType already exists with name: " + assetType.getName());
        }
        
        // Ensure bidirectional relationship is set for attributes
        if (assetType.getAttributeDefinitions() != null) {
            assetType.getAttributeDefinitions().forEach(attr -> attr.setAssetType(assetType));
        }

        return repository.save(assetType);
    }

    @Transactional
    public AssetType updateAssetType(Long id, AssetType updatedInfo) {
        AssetType existing = getAssetTypeById(id);
        existing.setDescription(updatedInfo.getDescription());
        existing.setActive(updatedInfo.isActive());
        // For simplicity, we manage attributes via separate calls or carefully match them, 
        // Here we just update the basic fields.
        return repository.save(existing);
    }

    @Transactional
    public AssetType addAttribute(Long assetTypeId, AttributeDefinition attribute) {
        AssetType type = getAssetTypeById(assetTypeId);
        
        // Check if name already exists
        boolean exists = type.getAttributeDefinitions().stream()
                .anyMatch(a -> a.getName().equalsIgnoreCase(attribute.getName()));
        
        if (exists) {
            throw new IllegalArgumentException("Attribute with name " + attribute.getName() + " already exists for this AssetType");
        }
        
        type.addAttributeDefinition(attribute);
        return repository.save(type);
    }
}
