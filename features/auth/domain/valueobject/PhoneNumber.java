package com.marketplace.user.domain.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Phone Number Value Object
 * 
 * Represents a phone number with country code and verification status.
 * Handles international phone number formats and validation.
 * 
 * Design principles:
 * - Immutable
 * - Self-validating with international standards
 * - Rich behavior for phone operations
 * - E.164 format compliant
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PhoneNumber implements Serializable {

    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{8,15}$");
    private static final Pattern COUNTRY_CODE_PATTERN = Pattern.compile("^\\+[1-9]\\d{0,3}$");

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "phone_country_code", length = 5)
    private String countryCode;

    @Column(name = "phone_verified", nullable = false)
    private boolean verified;

    /**
     * Creates a PhoneNumber with country code
     */
    public static PhoneNumber of(String phoneNumber, String countryCode) {
        return of(phoneNumber, countryCode, false);
    }

    /**
     * Creates a PhoneNumber with country code and verification status
     */
    public static PhoneNumber of(String phoneNumber, String countryCode, boolean verified) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be null or empty");
        }
        if (countryCode == null || countryCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Country code cannot be null or empty");
        }

        String cleanPhone = cleanPhoneNumber(phoneNumber);
        String cleanCountryCode = cleanCountryCode(countryCode);

        validatePhoneNumber(cleanPhone);
        validateCountryCode(cleanCountryCode);

        return new PhoneNumber(cleanPhone, cleanCountryCode, verified);
    }

    /**
     * Creates a Brazilian phone number
     */
    public static PhoneNumber brazilian(String phoneNumber) {
        return of(phoneNumber, "+55");
    }

    /**
     * Creates a US phone number
     */
    public static PhoneNumber us(String phoneNumber) {
        return of(phoneNumber, "+1");
    }

    /**
     * Creates an empty/null phone number
     */
    public static PhoneNumber empty() {
        return new PhoneNumber(null, null, false);
    }

    /**
     * Checks if this phone number is empty
     */
    public boolean isEmpty() {
        return phoneNumber == null || phoneNumber.trim().isEmpty();
    }

    /**
     * Gets the full international format (E.164)
     */
    public String getInternationalFormat() {
        if (isEmpty()) {
            return "";
        }
        return countryCode + phoneNumber;
    }

    /**
     * Gets the national format (without country code)
     */
    public String getNationalFormat() {
        if (isEmpty()) {
            return "";
        }

        return switch (countryCode) {
            case "+55" -> formatBrazilianPhone(phoneNumber);
            case "+1" -> formatUsPhone(phoneNumber);
            default -> phoneNumber;
        };
    }

    /**
     * Gets the display format for UI
     */
    public String getDisplayFormat() {
        if (isEmpty()) {
            return "";
        }
        return countryCode + " " + getNationalFormat();
    }

    /**
     * Gets the masked phone number for display
     */
    public String getMaskedNumber() {
        if (isEmpty()) {
            return "";
        }

        String national = getNationalFormat();
        if (national.length() <= 4) {
            return "****";
        }

        // Show first 2 and last 2 digits
        return national.substring(0, 2) + 
               "*".repeat(Math.max(0, national.length() - 4)) + 
               national.substring(national.length() - 2);
    }

    /**
     * Creates a verified version of this phone number
     */
    public PhoneNumber markAsVerified() {
        if (isEmpty()) {
            throw new IllegalStateException("Cannot verify empty phone number");
        }
        return new PhoneNumber(this.phoneNumber, this.countryCode, true);
    }

    /**
     * Creates an unverified version of this phone number
     */
    public PhoneNumber markAsUnverified() {
        if (isEmpty()) {
            return this;
        }
        return new PhoneNumber(this.phoneNumber, this.countryCode, false);
    }

    /**
     * Checks if this is a mobile phone number
     */
    public boolean isMobile() {
        if (isEmpty()) {
            return false;
        }

        return switch (countryCode) {
            case "+55" -> isBrazilianMobile(phoneNumber);
            case "+1" -> isUsMobile(phoneNumber);
            default -> true; // Assume mobile for other countries
        };
    }

    /**
     * Checks if this is a landline phone number
     */
    public boolean isLandline() {
        return !isEmpty() && !isMobile();
    }

    /**
     * Gets the country name from country code
     */
    public String getCountryName() {
        if (countryCode == null) {
            return "";
        }

        return switch (countryCode) {
            case "+55" -> "Brasil";
            case "+1" -> "United States";
            case "+44" -> "United Kingdom";
            case "+33" -> "France";
            case "+49" -> "Germany";
            case "+39" -> "Italy";
            case "+34" -> "Spain";
            case "+81" -> "Japan";
            case "+86" -> "China";
            case "+91" -> "India";
            default -> "Unknown";
        };
    }

    /**
     * Gets the area code (for supported countries)
     */
    public String getAreaCode() {
        if (isEmpty()) {
            return "";
        }

        return switch (countryCode) {
            case "+55" -> getBrazilianAreaCode(phoneNumber);
            case "+1" -> getUsAreaCode(phoneNumber);
            default -> "";
        };
    }

    /**
     * Cleans phone number removing formatting
     */
    private static String cleanPhoneNumber(String phone) {
        return phone.replaceAll("[^0-9]", "");
    }

    /**
     * Cleans country code ensuring + prefix
     */
    private static String cleanCountryCode(String countryCode) {
        String clean = countryCode.replaceAll("[^0-9+]", "");
        if (!clean.startsWith("+")) {
            clean = "+" + clean;
        }
        return clean;
    }

    /**
     * Validates phone number format
     */
    private static void validatePhoneNumber(String phone) {
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            throw new IllegalArgumentException("Invalid phone number format");
        }
    }

    /**
     * Validates country code format
     */
    private static void validateCountryCode(String countryCode) {
        if (!COUNTRY_CODE_PATTERN.matcher(countryCode).matches()) {
            throw new IllegalArgumentException("Invalid country code format");
        }
    }

    /**
     * Formats Brazilian phone number
     */
    private static String formatBrazilianPhone(String phone) {
        if (phone.length() == 11) {
            // Mobile: (11) 99999-9999
            return "(" + phone.substring(0, 2) + ") " + 
                   phone.substring(2, 7) + "-" + phone.substring(7);
        } else if (phone.length() == 10) {
            // Landline: (11) 9999-9999
            return "(" + phone.substring(0, 2) + ") " + 
                   phone.substring(2, 6) + "-" + phone.substring(6);
        }
        return phone;
    }

    /**
     * Formats US phone number
     */
    private static String formatUsPhone(String phone) {
        if (phone.length() == 10) {
            // (555) 123-4567
            return "(" + phone.substring(0, 3) + ") " + 
                   phone.substring(3, 6) + "-" + phone.substring(6);
        }
        return phone;
    }

    /**
     * Checks if Brazilian number is mobile
     */
    private static boolean isBrazilianMobile(String phone) {
        return phone.length() == 11 && phone.charAt(2) == '9';
    }

    /**
     * Checks if US number is mobile (simplified)
     */
    private static boolean isUsMobile(String phone) {
        // In US, mobile/landline distinction is complex, assume mobile for simplicity
        return true;
    }

    /**
     * Gets Brazilian area code
     */
    private static String getBrazilianAreaCode(String phone) {
        if (phone.length() >= 2) {
            return phone.substring(0, 2);
        }
        return "";
    }

    /**
     * Gets US area code
     */
    private static String getUsAreaCode(String phone) {
        if (phone.length() >= 3) {
            return phone.substring(0, 3);
        }
        return "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhoneNumber that = (PhoneNumber) o;
        return verified == that.verified &&
               Objects.equals(phoneNumber, that.phoneNumber) &&
               Objects.equals(countryCode, that.countryCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(phoneNumber, countryCode, verified);
    }

    @Override
    public String toString() {
        return isEmpty() ? "" : getDisplayFormat();
    }
}

