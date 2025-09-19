package com.marketplace.shared.valueobject;

import lombok.Getter;

/**
 * Enumeration of supported currency codes.
 * Based on ISO 4217 standard.
 */
@Getter
public enum CurrencyCode {
    
    BRL("R$", "Real Brasileiro", 2),
    USD("$", "US Dollar", 2),
    EUR("€", "Euro", 2),
    GBP("£", "British Pound", 2),
    JPY("¥", "Japanese Yen", 0),
    CAD("C$", "Canadian Dollar", 2),
    AUD("A$", "Australian Dollar", 2),
    CHF("CHF", "Swiss Franc", 2),
    CNY("¥", "Chinese Yuan", 2),
    INR("₹", "Indian Rupee", 2),
    MXN("$", "Mexican Peso", 2),
    ARS("$", "Argentine Peso", 2),
    CLP("$", "Chilean Peso", 0),
    COP("$", "Colombian Peso", 2),
    PEN("S/", "Peruvian Sol", 2),
    UYU("$U", "Uruguayan Peso", 2);
    
    private final String symbol;
    private final String displayName;
    private final int defaultFractionDigits;
    
    CurrencyCode(String symbol, String displayName, int defaultFractionDigits) {
        this.symbol = symbol;
        this.displayName = displayName;
        this.defaultFractionDigits = defaultFractionDigits;
    }
    
    /**
     * Gets the currency code from a string, case-insensitive.
     */
    public static CurrencyCode fromString(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Currency code cannot be null or empty");
        }
        
        try {
            return CurrencyCode.valueOf(code.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unsupported currency code: " + code);
        }
    }
    
    /**
     * Checks if the given currency code is supported.
     */
    public static boolean isSupported(String code) {
        try {
            fromString(code);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * Gets all supported currency codes as strings.
     */
    public static String[] getSupportedCodes() {
        CurrencyCode[] values = CurrencyCode.values();
        String[] codes = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            codes[i] = values[i].name();
        }
        return codes;
    }
    
    @Override
    public String toString() {
        return name();
    }
}

