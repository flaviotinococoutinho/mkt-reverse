package com.marketplace.sourcing.domain.valueobject;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Curated marketplace taxonomy backed by real MCC codes (ISO 18245).
 *
 * Compliance by design (see docs/compliance/guardrails.md):
 * - The taxonomy is a structural denylist: prohibited categories (weapons,
 *   pharmaceuticals, live animals, protected fauna/flora, personal documents,
 *   named tickets, etc.) simply do not exist here. Forbidding by absence is
 *   cheaper than moderating by presence.
 * - A buyer intent or seller proposal can only reference a category present
 *   in this enum, and each category maps to a typed attribute schema
 *   (see {@link CategoryAttributeSchema}).
 *
 * Niche sequencing (kickoff plan): collectibles first, circular fashion
 * second, auto parts last and restricted — the sequencing is enforced
 * operationally (which categories are opened per phase), not by this type.
 */
public enum MccCategory {

    // ── Nicho 1 (kickoff): Colecionáveis ────────────────────────────
    ANTIQUES(5937, "Antique Stores", "Collectibles"),
    HOBBY_TOY_GAME(5945, "Hobby, Toy and Game Shops", "Collectibles"),
    BOOK_STORES(5942, "Book Stores", "Collectibles"),
    STAMP_COIN_STORES(5972, "Stamp and Coin Stores", "Collectibles"),

    // ── Nicho 2 (Fase 2): Moda circular / segunda mão ───────────────
    USED_MERCHANDISE(5931, "Used Merchandise and Secondhand Stores", "Circular Fashion"),

    // ── Nicho 3 (Fase 3, restrito): Autopeças e hardware ────────────
    MOTOR_VEHICLE_PARTS(5533, "Automotive Parts and Accessories Stores", "Automotive"),
    ELECTRICAL_PARTS_EQUIPMENT(5065, "Electrical Parts and Equipment", "Industrial"),
    INDUSTRIAL_SUPPLIES(5085, "Industrial Supplies (Not Elsewhere Classified)", "Industrial"),
    HARDWARE_STORES(5251, "Hardware Stores", "Industrial"),
    ELECTRONICS_STORES(5732, "Electronics Stores", "Technology"),
    REPAIR_SHOPS(7699, "Miscellaneous Repair Shops and Related Services", "Services"),
    ELECTRICAL_REPAIR(7629, "Electrical and Small Appliance Repair Shops", "Services"),

    // ── Fallback controlado ──────────────────────────────────────────
    OTHER(5999, "Miscellaneous and Specialty Retail Stores", "Other");

    private static final Map<Integer, MccCategory> BY_CODE = Arrays.stream(values())
        .collect(Collectors.toUnmodifiableMap(MccCategory::getCode, Function.identity()));

    private final int code;
    private final String description;
    private final String segment;

    MccCategory(int code, String description, String segment) {
        this.code = code;
        this.description = description;
        this.segment = segment;
    }

    /**
     * Validates if a code belongs to the curated taxonomy.
     */
    public static boolean isValid(Integer code) {
        return code != null && BY_CODE.containsKey(code);
    }

    /**
     * Returns the category for the code or throws if it is not part of the
     * curated taxonomy (structural denylist: unknown code = forbidden).
     */
    public static MccCategory requireFromCode(Integer code) {
        MccCategory category = fromCode(code);
        if (category == null) {
            throw new IllegalArgumentException("Invalid MCC code: " + code
                + ". Valid codes are: " + BY_CODE.keySet());
        }
        return category;
    }

    /**
     * Gets the category from a code, returning null when unknown.
     */
    public static MccCategory fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        return BY_CODE.get(code);
    }

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public String getSegment() {
        return segment;
    }
}
