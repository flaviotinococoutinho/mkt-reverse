package com.marketplace.catalog.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.marketplace.catalog.domain.entity.Asset;
import com.marketplace.catalog.domain.entity.AssetRelationship;
import com.marketplace.catalog.domain.entity.AssetType;
import com.marketplace.catalog.domain.entity.AttributeDefinition;
import com.marketplace.catalog.domain.entity.RelationshipType;
import com.marketplace.catalog.repository.AssetRelationshipRepository;
import com.marketplace.catalog.repository.AssetRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AssetService {

    private final AssetRepository assetRepository;
    private final AssetTypeService assetTypeService;
    private final AssetRelationshipRepository relationshipRepository;

    @Transactional(readOnly = true)
    public Asset getAssetById(Long id) {
        return assetRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Asset not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public Asset getAssetByTag(String tag) {
        return assetRepository.findByTag(tag)
                .orElseThrow(() -> new IllegalArgumentException("Asset not found with tag: " + tag));
    }

    @Transactional
    public Asset createAsset(Asset asset) {
        // Validation: Verify the AssetType exists
        AssetType type = assetTypeService.getAssetTypeById(asset.getAssetType().getId());
        asset.setAssetType(type);

        // Validation: Verify attributes against schema
        validateAttributes(type, asset.getAttributes());

        if (assetRepository.findByTag(asset.getTag()).isPresent()) {
            throw new IllegalArgumentException("Asset tag already exists: " + asset.getTag());
        }

        return assetRepository.save(asset);
    }

    @Transactional
    public Asset updateAsset(Long id, Map<String, Object> newAttributes, String status, String name) {
        Asset asset = getAssetById(id);
        
        if (name != null) asset.setName(name);
        if (status != null) asset.setStatus(status);
        
        if (newAttributes != null) {
            // Merge attributes
            Map<String, Object> currentAttributes = asset.getAttributes();
            if (currentAttributes == null) {
                currentAttributes = newAttributes;
            } else {
                currentAttributes.putAll(newAttributes);
            }
            validateAttributes(asset.getAssetType(), currentAttributes);
            asset.setAttributes(currentAttributes);
        }

        return assetRepository.save(asset);
    }

    @Transactional
    public AssetRelationship createRelationship(Long sourceId, Long targetId, RelationshipType type) {
        Asset source = getAssetById(sourceId);
        Asset target = getAssetById(targetId);

        AssetRelationship relationship = AssetRelationship.builder()
                .sourceAsset(source)
                .targetAsset(target)
                .type(type)
                .build();

        return relationshipRepository.save(relationship);
    }

    @Transactional(readOnly = true)
    public List<AssetRelationship> getAssetDependencies(Long assetId) {
        return relationshipRepository.findBySourceAssetId(assetId);
    }

    /**
     * Validates that the provided attributes match the definitions in the AssetType.
     * Checks for required fields and loosely checks data types.
     */
    private void validateAttributes(AssetType type, Map<String, Object> attributes) {
        if (attributes == null) attributes = Map.of();

        Set<AttributeDefinition> definitions = type.getAttributeDefinitions();

        for (AttributeDefinition def : definitions) {
            String attrName = def.getName();
            Object value = attributes.get(attrName);

            // Check required fields
            if (def.isRequired() && (value == null || value.toString().trim().isEmpty())) {
                throw new IllegalArgumentException("Missing required attribute: " + attrName);
            }

            if (value != null) {
                // Perform basic type validations based on AttributeType Enum
                switch (def.getType()) {
                    case INTEGER:
                        if (!(value instanceof Integer) && !value.toString().matches("-?\\d+")) {
                            throw new IllegalArgumentException("Attribute " + attrName + " must be an Integer");
                        }
                        break;
                    case BOOLEAN:
                        if (!(value instanceof Boolean) && !value.toString().matches("(?i)^(true|false)$")) {
                            throw new IllegalArgumentException("Attribute " + attrName + " must be a Boolean");
                        }
                        break;
                    // Additional type validations can be handled here (e.g., Decimal, Date parsing)
                    default:
                        // STRING, JSON generally pass through
                        break;
                }
            }
        }
        
        // Optional: Check if attributes map contains keys not defined in the schema
        // and throw exception if strict schema enforcement is desired.
    }
}
