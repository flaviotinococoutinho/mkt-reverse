package com.marketplace.catalog.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.marketplace.catalog.domain.entity.Asset;
import com.marketplace.catalog.domain.entity.AssetType;
import com.marketplace.catalog.domain.entity.AttributeDefinition;
import com.marketplace.catalog.domain.entity.AttributeType;
import com.marketplace.catalog.repository.AssetRelationshipRepository;
import com.marketplace.catalog.repository.AssetRepository;

@ExtendWith(MockitoExtension.class)
public class AssetServiceTest {

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private AssetTypeService assetTypeService;

    @Mock
    private AssetRelationshipRepository relationshipRepository;

    @InjectMocks
    private AssetService assetService;

    private AssetType serverType;

    @BeforeEach
    void setUp() {
        serverType = AssetType.builder()
                .id(1L)
                .name("Server")
                .build();

        AttributeDefinition ramAttr = AttributeDefinition.builder()
                .name("ram_gb")
                .type(AttributeType.INTEGER)
                .required(true)
                .build();

        AttributeDefinition ipAttr = AttributeDefinition.builder()
                .name("ip_address")
                .type(AttributeType.STRING)
                .required(false)
                .build();

        serverType.addAttributeDefinition(ramAttr);
        serverType.addAttributeDefinition(ipAttr);
    }

    @Test
    void createAsset_ValidAttributes_Success() {
        // Arrange
        when(assetTypeService.getAssetTypeById(1L)).thenReturn(serverType);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("ram_gb", 32);
        attributes.put("ip_address", "192.168.1.100");

        Asset asset = Asset.builder()
                .tag("SRV-001")
                .name("Main Database Server")
                .status("ACTIVE")
                .assetType(AssetType.builder().id(1L).build())
                .attributes(attributes)
                .build();

        when(assetRepository.findByTag("SRV-001")).thenReturn(Optional.empty());
        when(assetRepository.save(any(Asset.class))).thenReturn(asset);

        // Act
        Asset saved = assetService.createAsset(asset);

        // Assert
        assertNotNull(saved);
        assertEquals("SRV-001", saved.getTag());
        verify(assetRepository).save(any(Asset.class));
    }

    @Test
    void createAsset_MissingRequiredAttribute_ThrowsException() {
        // Arrange
        when(assetTypeService.getAssetTypeById(1L)).thenReturn(serverType);

        // "ram_gb" is required, but we omit it
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("ip_address", "192.168.1.100");

        Asset asset = Asset.builder()
                .tag("SRV-002")
                .name("Backup Server")
                .assetType(AssetType.builder().id(1L).build())
                .attributes(attributes)
                .build();

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            assetService.createAsset(asset);
        });

        assertTrue(exception.getMessage().contains("Missing required attribute: ram_gb"));
        verify(assetRepository, never()).save(any(Asset.class));
    }

    @Test
    void createAsset_InvalidAttributeType_ThrowsException() {
        // Arrange
        when(assetTypeService.getAssetTypeById(1L)).thenReturn(serverType);

        // "ram_gb" should be an Integer
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("ram_gb", "ThirtyTwo"); // Invalid type string

        Asset asset = Asset.builder()
                .tag("SRV-003")
                .name("Web Server")
                .assetType(AssetType.builder().id(1L).build())
                .attributes(attributes)
                .build();

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            assetService.createAsset(asset);
        });

        assertTrue(exception.getMessage().contains("must be an Integer"));
        verify(assetRepository, never()).save(any(Asset.class));
    }
}
