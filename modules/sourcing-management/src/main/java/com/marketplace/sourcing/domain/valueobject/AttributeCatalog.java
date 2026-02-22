package com.marketplace.sourcing.domain.valueobject;

import java.util.Map;
import java.util.Set;

/**
 * Curated catalog of common attributes (typed) for MVP.
 *
 * Goal: make opportunities and proposals comparable (normalize fields).
 */
public final class AttributeCatalog {

    private AttributeCatalog() {}

    public static final String VOLTAGE = "voltage";
    public static final String LANGUAGE = "language";
    public static final String COLOR = "color";
    public static final String WEIGHT_G = "weight_g";
    public static final String VOLUME_ML = "volume_ml";

    public static final Map<String, SpecAttributeType> TYPES = Map.of(
        VOLTAGE, SpecAttributeType.VOLTAGE,
        LANGUAGE, SpecAttributeType.LANGUAGE,
        COLOR, SpecAttributeType.COLOR,
        WEIGHT_G, SpecAttributeType.WEIGHT,
        VOLUME_ML, SpecAttributeType.VOLUME
    );

    public static final Set<String> KEYS = TYPES.keySet();
}
