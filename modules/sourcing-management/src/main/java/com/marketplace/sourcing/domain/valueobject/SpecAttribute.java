package com.marketplace.sourcing.domain.valueobject;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Serializable attribute for opportunities and proposals.
 *
 * We keep this lightweight for MVP: validation is done at the application layer.
 */
public final class SpecAttribute {

    private final String key;
    private final SpecAttributeType type;
    private final String unit; // optional (e.g. V, g, ml)
    private final Object value;

    @JsonCreator
    public SpecAttribute(
        @JsonProperty("key") String key,
        @JsonProperty("type") SpecAttributeType type,
        @JsonProperty("unit") String unit,
        @JsonProperty("value") Object value
    ) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Attribute key is required");
        }
        this.key = key.trim();
        this.type = Objects.requireNonNull(type, "type is required");
        this.unit = unit != null && !unit.trim().isEmpty() ? unit.trim() : null;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public SpecAttributeType getType() {
        return type;
    }

    public String getUnit() {
        return unit;
    }

    public Object getValue() {
        return value;
    }
}
