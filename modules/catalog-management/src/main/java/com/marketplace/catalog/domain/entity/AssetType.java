package com.marketplace.catalog.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "asset_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name; // e.g., "Server", "Software License", "Laptop"

    @Column(length = 500)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private AssetType parentType; // Support Inheritance

    @OneToMany(mappedBy = "assetType", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<AttributeDefinition> attributeDefinitions = new HashSet<>();

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public void addAttributeDefinition(AttributeDefinition attr) {
        attributeDefinitions.add(attr);
        attr.setAssetType(this);
    }
    
    public void removeAttributeDefinition(AttributeDefinition attr) {
        attributeDefinitions.remove(attr);
        attr.setAssetType(null);
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
