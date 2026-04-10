package com.marketplace.sourcing.domain.valueobject;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Hard-normalized attribute schema by category.
 *
 * Rules:
 * - For a category code, only keys in ALLOWED_KEYS may be used.
 * - When a key is in the schema, its SpecAttributeType must match the expected one.
 * - REQUIRED_KEYS must be present for that category.
 *
 * MVP: curated subset; expand incrementally.
 */
public final class CategoryAttributeSchema {

    private CategoryAttributeSchema() {}

    public record Schema(Set<String> requiredKeys, Map<String, SpecAttributeType> allowedKeys) {}

    public static final Schema HARDWARE_PARTS = new Schema(
        Set.of(),
        Map.of(
            AttributeCatalog.VOLTAGE, SpecAttributeType.VOLTAGE,
            AttributeCatalog.COLOR, SpecAttributeType.COLOR,
            AttributeCatalog.WEIGHT_G, SpecAttributeType.WEIGHT,
            AttributeCatalog.VOLUME_ML, SpecAttributeType.VOLUME,
            AttributeCatalog.LANGUAGE, SpecAttributeType.LANGUAGE
        )
    );

    public static final Schema COLLECTIBLES = new Schema(
        Set.of(AttributeCatalog.LANGUAGE),
        Map.of(
            AttributeCatalog.LANGUAGE, SpecAttributeType.LANGUAGE,
            AttributeCatalog.COLOR, SpecAttributeType.COLOR,
            AttributeCatalog.WEIGHT_G, SpecAttributeType.WEIGHT
        )
    );

    public static Schema schemaFor(MccCategory category) {
        if (category == null) {
            throw new IllegalArgumentException("category is required");
        }

        return switch (category) {
            case MOTOR_VEHICLE_PARTS,
                 ELECTRICAL_PARTS_EQUIPMENT,
                 INDUSTRIAL_SUPPLIES,
                 HARDWARE_STORES,
                 REPAIR_SHOPS,
                 ELECTRICAL_REPAIR -> HARDWARE_PARTS;

            case ANTIQUES,
                 HOBBY_TOY_GAME,
                 BOOK_STORES -> COLLECTIBLES;

            case OTHER -> new Schema(Set.of(), Map.of());
        };
    }

    public static void validate(MccCategory category, List<SpecAttribute> attributes) {
        Schema schema = schemaFor(category);
        if (attributes == null || attributes.isEmpty()) {
            if (!schema.requiredKeys.isEmpty()) {
                throw new IllegalArgumentException("Missing required attributes: " + schema.requiredKeys);
            }
            return;
        }

        // required keys
        for (String req : schema.requiredKeys) {
            boolean present = attributes.stream().anyMatch(a -> req.equals(a.getKey()));
            if (!present) {
                throw new IllegalArgumentException("Missing required attribute: " + req);
            }
        }

        // allowed keys + type match
        for (SpecAttribute a : attributes) {
            if (!schema.allowedKeys.containsKey(a.getKey())) {
                throw new IllegalArgumentException("Attribute key not allowed for category " + category.getCode() + ": " + a.getKey());
            }
            SpecAttributeType expected = schema.allowedKeys.get(a.getKey());
            if (a.getType() != expected) {
                throw new IllegalArgumentException("Invalid attribute type for key=" + a.getKey() + ": expected " + expected + " got " + a.getType());
            }
        }
    }
}
