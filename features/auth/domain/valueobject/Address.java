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
 * Address Value Object
 * 
 * Represents a physical address with validation and formatting.
 * Supports Brazilian and international address formats.
 * 
 * Design principles:
 * - Immutable
 * - Self-validating
 * - Rich behavior for address operations
 * - International format support
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Address implements Serializable {

    private static final Pattern BRAZILIAN_ZIP_CODE = Pattern.compile("^\\d{5}-?\\d{3}$");
    private static final Pattern US_ZIP_CODE = Pattern.compile("^\\d{5}(-\\d{4})?$");

    @Column(name = "address_street", length = 255)
    private String street;

    @Column(name = "address_number", length = 20)
    private String number;

    @Column(name = "address_complement", length = 100)
    private String complement;

    @Column(name = "address_neighborhood", length = 100)
    private String neighborhood;

    @Column(name = "address_city", length = 100)
    private String city;

    @Column(name = "address_state", length = 50)
    private String state;

    @Column(name = "address_zip_code", length = 20)
    private String zipCode;

    @Column(name = "address_country", length = 50)
    private String country;

    /**
     * Creates a complete Address
     */
    public static Address of(String street, String number, String complement, 
                           String neighborhood, String city, String state, 
                           String zipCode, String country) {
        
        validateRequiredFields(street, city, state, zipCode, country);
        
        return new Address(
            normalizeString(street),
            normalizeString(number),
            normalizeString(complement),
            normalizeString(neighborhood),
            normalizeString(city),
            normalizeString(state),
            normalizeZipCode(zipCode, country),
            normalizeString(country)
        );
    }

    /**
     * Creates a Brazilian Address
     */
    public static Address brazilian(String street, String number, String complement,
                                  String neighborhood, String city, String state, String cep) {
        return of(street, number, complement, neighborhood, city, state, cep, "Brasil");
    }

    /**
     * Creates a US Address
     */
    public static Address us(String street, String number, String city, String state, String zipCode) {
        return of(street, number, null, null, city, state, zipCode, "United States");
    }

    /**
     * Creates an empty Address
     */
    public static Address empty() {
        return new Address(null, null, null, null, null, null, null, null);
    }

    /**
     * Checks if this address is empty
     */
    public boolean isEmpty() {
        return street == null || street.trim().isEmpty();
    }

    /**
     * Gets the full address as a single line
     */
    public String getFullAddress() {
        if (isEmpty()) {
            return "";
        }

        StringBuilder address = new StringBuilder();
        
        // Street and number
        address.append(street);
        if (number != null && !number.trim().isEmpty()) {
            address.append(", ").append(number);
        }
        
        // Complement
        if (complement != null && !complement.trim().isEmpty()) {
            address.append(", ").append(complement);
        }
        
        // Neighborhood (for Brazilian addresses)
        if (neighborhood != null && !neighborhood.trim().isEmpty()) {
            address.append(", ").append(neighborhood);
        }
        
        // City, State, ZIP
        address.append(", ").append(city);
        address.append(", ").append(state);
        address.append(" ").append(zipCode);
        
        // Country (if not default)
        if (country != null && !country.equalsIgnoreCase("Brasil") && !country.equalsIgnoreCase("Brazil")) {
            address.append(", ").append(country);
        }
        
        return address.toString();
    }

    /**
     * Gets the address formatted for mailing
     */
    public String getMailingFormat() {
        if (isEmpty()) {
            return "";
        }

        StringBuilder mailing = new StringBuilder();
        
        // Line 1: Street, number, complement
        mailing.append(street);
        if (number != null && !number.trim().isEmpty()) {
            mailing.append(", ").append(number);
        }
        if (complement != null && !complement.trim().isEmpty()) {
            mailing.append(", ").append(complement);
        }
        mailing.append("\n");
        
        // Line 2: Neighborhood (if exists)
        if (neighborhood != null && !neighborhood.trim().isEmpty()) {
            mailing.append(neighborhood).append("\n");
        }
        
        // Line 3: City, State, ZIP
        mailing.append(city).append(", ").append(state).append(" ").append(zipCode).append("\n");
        
        // Line 4: Country
        mailing.append(country);
        
        return mailing.toString();
    }

    /**
     * Gets a short address format (street, city, state)
     */
    public String getShortFormat() {
        if (isEmpty()) {
            return "";
        }
        
        StringBuilder shortAddr = new StringBuilder();
        shortAddr.append(street);
        
        if (number != null && !number.trim().isEmpty()) {
            shortAddr.append(", ").append(number);
        }
        
        shortAddr.append(" - ").append(city).append(", ").append(state);
        
        return shortAddr.toString();
    }

    /**
     * Checks if this is a Brazilian address
     */
    public boolean isBrazilian() {
        return country != null && 
               (country.equalsIgnoreCase("Brasil") || country.equalsIgnoreCase("Brazil"));
    }

    /**
     * Checks if this is a US address
     */
    public boolean isUS() {
        return country != null && 
               (country.equalsIgnoreCase("United States") || 
                country.equalsIgnoreCase("USA") || 
                country.equalsIgnoreCase("US"));
    }

    /**
     * Gets the formatted ZIP code
     */
    public String getFormattedZipCode() {
        if (zipCode == null) {
            return "";
        }

        if (isBrazilian()) {
            return formatBrazilianZipCode(zipCode);
        } else if (isUS()) {
            return formatUSZipCode(zipCode);
        }
        
        return zipCode;
    }

    /**
     * Creates a new Address with updated street
     */
    public Address withStreet(String newStreet) {
        return new Address(normalizeString(newStreet), number, complement, 
                          neighborhood, city, state, zipCode, country);
    }

    /**
     * Creates a new Address with updated city
     */
    public Address withCity(String newCity) {
        return new Address(street, number, complement, neighborhood, 
                          normalizeString(newCity), state, zipCode, country);
    }

    /**
     * Creates a new Address with updated ZIP code
     */
    public Address withZipCode(String newZipCode) {
        return new Address(street, number, complement, neighborhood, 
                          city, state, normalizeZipCode(newZipCode, country), country);
    }

    /**
     * Validates required fields
     */
    private static void validateRequiredFields(String street, String city, 
                                             String state, String zipCode, String country) {
        if (street == null || street.trim().isEmpty()) {
            throw new IllegalArgumentException("Street cannot be null or empty");
        }
        if (city == null || city.trim().isEmpty()) {
            throw new IllegalArgumentException("City cannot be null or empty");
        }
        if (state == null || state.trim().isEmpty()) {
            throw new IllegalArgumentException("State cannot be null or empty");
        }
        if (zipCode == null || zipCode.trim().isEmpty()) {
            throw new IllegalArgumentException("ZIP code cannot be null or empty");
        }
        if (country == null || country.trim().isEmpty()) {
            throw new IllegalArgumentException("Country cannot be null or empty");
        }
    }

    /**
     * Normalizes string fields
     */
    private static String normalizeString(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * Normalizes and validates ZIP code
     */
    private static String normalizeZipCode(String zipCode, String country) {
        if (zipCode == null) {
            return null;
        }
        
        String clean = zipCode.replaceAll("\\s+", "");
        
        if (country != null) {
            if (country.equalsIgnoreCase("Brasil") || country.equalsIgnoreCase("Brazil")) {
                validateBrazilianZipCode(clean);
            } else if (country.equalsIgnoreCase("United States") || 
                      country.equalsIgnoreCase("USA") || 
                      country.equalsIgnoreCase("US")) {
                validateUSZipCode(clean);
            }
        }
        
        return clean;
    }

    /**
     * Validates Brazilian ZIP code (CEP)
     */
    private static void validateBrazilianZipCode(String cep) {
        if (!BRAZILIAN_ZIP_CODE.matcher(cep).matches()) {
            throw new IllegalArgumentException("Invalid Brazilian ZIP code format. Expected: 12345-678 or 12345678");
        }
    }

    /**
     * Validates US ZIP code
     */
    private static void validateUSZipCode(String zip) {
        if (!US_ZIP_CODE.matcher(zip).matches()) {
            throw new IllegalArgumentException("Invalid US ZIP code format. Expected: 12345 or 12345-6789");
        }
    }

    /**
     * Formats Brazilian ZIP code: 12345678 -> 12345-678
     */
    private static String formatBrazilianZipCode(String cep) {
        if (cep.length() == 8 && cep.matches("\\d{8}")) {
            return cep.substring(0, 5) + "-" + cep.substring(5);
        }
        return cep;
    }

    /**
     * Formats US ZIP code
     */
    private static String formatUSZipCode(String zip) {
        if (zip.length() == 9 && zip.matches("\\d{9}")) {
            return zip.substring(0, 5) + "-" + zip.substring(5);
        }
        return zip;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        return Objects.equals(street, address.street) &&
               Objects.equals(number, address.number) &&
               Objects.equals(complement, address.complement) &&
               Objects.equals(neighborhood, address.neighborhood) &&
               Objects.equals(city, address.city) &&
               Objects.equals(state, address.state) &&
               Objects.equals(zipCode, address.zipCode) &&
               Objects.equals(country, address.country);
    }

    @Override
    public int hashCode() {
        return Objects.hash(street, number, complement, neighborhood, 
                           city, state, zipCode, country);
    }

    @Override
    public String toString() {
        return isEmpty() ? "" : getShortFormat();
    }
}

