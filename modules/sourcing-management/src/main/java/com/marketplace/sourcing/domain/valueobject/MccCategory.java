package com.marketplace.sourcing.domain.valueobject;

import java.util.Arrays;
import java.util.Optional;

/**
 * MCC-like category set for opportunity classification.
 *
 * Not a full ISO-18245 MCC table. It's a curated subset that is useful for the MVP and
 * can be expanded incrementally.
 */
public enum MccCategory {

    // --- Hardware / parts / tools ---
    HARDWARE_STORES(5251, "Hardware Stores"),
    INDUSTRIAL_SUPPLIES(5085, "Industrial Supplies"),
    ELECTRICAL_PARTS_EQUIPMENT(5065, "Electrical Parts and Equipment"),
    MOTOR_VEHICLE_PARTS(5533, "Automotive Parts, Accessories"),

    // --- Collectibles / niche retail ---
    ANTIQUES(5932, "Antique Shops"),
    HOBBY_TOY_GAME(5945, "Hobby, Toy, Game Shops"),
    BOOK_STORES(5942, "Book Stores"),

    // --- Services (when we expand) ---
    REPAIR_SHOPS(7699, "Repair Shops and Related Services"),
    ELECTRICAL_REPAIR(7622, "Electrical Repair Shops"),

    // --- Generic fallback ---
    OTHER(9999, "Other / Not Classified");

    private final int code;
    private final String description;

    MccCategory(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static Optional<MccCategory> fromCode(Integer code) {
        if (code == null) return Optional.empty();
        return Arrays.stream(values()).filter(v -> v.code == code).findFirst();
    }

    public static MccCategory requireFromCode(Integer code) {
        return fromCode(code).orElseThrow(() -> new IllegalArgumentException("Invalid category code: " + code));
    }
}
