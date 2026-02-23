package com.marketplace.catalog.domain.entity;

public enum AttributeType {
    STRING,
    INTEGER,
    DECIMAL,
    BOOLEAN,
    DATE,
    REFERENCE, // Reference to another asset
    JSON       // Complex nested object
}
