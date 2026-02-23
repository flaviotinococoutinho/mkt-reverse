package com.marketplace.catalog.domain.entity;

public enum RelationshipType {
    DEPENDS_ON("Depends on"),
    USED_BY("Used by"),
    INSTALLED_ON("Installed on"),
    LOCATED_IN("Located in"),
    MANAGED_BY("Managed by"),
    CONNECTED_TO("Connected to"),
    REPLACES("Replaces"),
    PARENT_OF("Parent of"),
    CHILD_OF("Child of");

    private final String description;

    RelationshipType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
