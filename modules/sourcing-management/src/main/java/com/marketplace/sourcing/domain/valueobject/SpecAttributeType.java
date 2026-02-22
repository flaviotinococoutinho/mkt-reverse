package com.marketplace.sourcing.domain.valueobject;

/**
 * Attribute type system for ProductSpecification attributes.
 * These types are used for validation and (later) UI rendering.
 */
public enum SpecAttributeType {
    TEXT,
    NUMBER,
    BOOLEAN,
    ENUM,
    WEIGHT,   // value in grams by convention
    VOLUME,   // value in milliliters by convention
    VOLTAGE,  // value in volts
    LANGUAGE,
    COLOR
}
