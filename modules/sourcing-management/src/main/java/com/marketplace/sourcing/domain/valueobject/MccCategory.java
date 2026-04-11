package com.marketplace.sourcing.domain.valueobject;

import java.util.Set;

/**
 * MCC Category codes with validation.
 * 
 * Reference: ISO 18245 - Merchant Category Codes
 */
public final class MccCategory {

    // Valid MCC codes for the marketplace
    private static final Set<Integer> VALID_CODES = Set.of(
            174, // Electronics and Computer
            275, // Apparel and Accessories
            553, // Automotive Parts and Services
            521, // Furniture and Home Furnishings
            571, // Real Estate
            501, // Medical and Pharmaceutical
            581, // Food and Beverages
            504, // Machinery and Equipment
            821, // Professional Services
            829, // Other Services
            481, // Telecommunications
            412, // Transportation Services
            525, // Hardware Stores
            549, // Grocery Stores
            530, // Wholesale Clubs
            573, // Computer Software Stores
            594, // Sporting Goods Stores
            597, // Art and Craft Supply
            565, // Family Clothing
            762, // Electronics Repair
            541, // Building Materials
            891, // Engineering Services
            872, // Accounting Services
            872  // Professional Consulting
    );

    private final Integer code;
    private final String description;
    private final String segment;

    private MccCategory(Integer code, String description, String segment) {
        this.code = code;
        this.description = description;
        this.segment = segment;
    }

    /**
     * Validates if a code is valid.
     */
    public static boolean isValid(Integer code) {
        return code != null && VALID_CODES.contains(code);
    }

    /**
     * Returns the MCC code or throws if invalid.
     */
    public static MccCategory requireFromCode(Integer code) {
        if (!isValid(code)) {
            throw new IllegalArgumentException("Invalid MCC code: " + code + 
                    ". Valid codes are: " + VALID_CODES);
        }
        return fromCode(code);
    }

    /**
     * Gets MCC from code, returns null if invalid.
     */
    public static MccCategory fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        return switch (code) {
            case 174 -> new MccCategory(174, "Electronics and Computer", "Technology");
            case 275 -> new MccCategory(275, "Apparel and Accessories", "Fashion");
            case 553 -> new MccCategory(553, "Automotive Parts and Services", "Automotive");
            case 521 -> new MccCategory(521, "Furniture and Home Furnishings", "Home");
            case 571 -> new MccCategory(571, "Real Estate", "Property");
            case 501 -> new MccCategory(501, "Medical and Pharmaceutical", "Healthcare");
            case 581 -> new MccCategory(581, "Food and Beverages", "Food");
            case 504 -> new MccCategory(504, "Machinery and Equipment", "Industrial");
            case 821 -> new MccCategory(821, "Professional Services", "Services");
            case 829 -> new MccCategory(829, "Other Services", "Services");
            default -> new MccCategory(code, "Category " + code, "Other");
        };
    }

    public Integer getCode() { return code; }
    public String getDescription() { return description; }
    public String getSegment() { return segment; }

    @Override
    public String toString() {
        return "MccCategory{" +
                "code=" + code +
                ", description='" + description + '\'' +
                ", segment='" + segment + '\'' +
                '}';
    }
}