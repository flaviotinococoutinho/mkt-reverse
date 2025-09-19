package com.marketplace.user.domain.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

/**
 * Personal Information Value Object
 * 
 * Represents personal information of a user including names and display preferences.
 * Handles name validation and formatting according to business rules.
 * 
 * Design principles:
 * - Immutable
 * - Self-validating
 * - Rich behavior for name operations
 * - Value-based equality
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PersonalInfo implements Serializable {

    private static final int MAX_NAME_LENGTH = 100;
    private static final int MAX_DISPLAY_NAME_LENGTH = 200;

    @Column(name = "first_name", nullable = false, length = MAX_NAME_LENGTH)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = MAX_NAME_LENGTH)
    private String lastName;

    @Column(name = "display_name", length = MAX_DISPLAY_NAME_LENGTH)
    private String displayName;

    /**
     * Creates PersonalInfo with first and last name
     */
    public static PersonalInfo of(String firstName, String lastName) {
        return of(firstName, lastName, null);
    }

    /**
     * Creates PersonalInfo with first name, last name, and display name
     */
    public static PersonalInfo of(String firstName, String lastName, String displayName) {
        validateName(firstName, "First name");
        validateName(lastName, "Last name");

        String normalizedFirstName = normalizeName(firstName);
        String normalizedLastName = normalizeName(lastName);
        String normalizedDisplayName = displayName != null ? 
            normalizeDisplayName(displayName) : 
            generateDefaultDisplayName(normalizedFirstName, normalizedLastName);

        return new PersonalInfo(normalizedFirstName, normalizedLastName, normalizedDisplayName);
    }

    /**
     * Gets the full name (first + last)
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Gets the initials (first letter of first and last name)
     */
    public String getInitials() {
        StringBuilder initials = new StringBuilder();
        
        if (firstName != null && !firstName.isEmpty()) {
            initials.append(Character.toUpperCase(firstName.charAt(0)));
        }
        
        if (lastName != null && !lastName.isEmpty()) {
            initials.append(Character.toUpperCase(lastName.charAt(0)));
        }
        
        return initials.toString();
    }

    /**
     * Gets the formal name (Last, First)
     */
    public String getFormalName() {
        return lastName + ", " + firstName;
    }

    /**
     * Creates a new PersonalInfo with updated first name
     */
    public PersonalInfo withFirstName(String newFirstName) {
        return PersonalInfo.of(newFirstName, this.lastName, this.displayName);
    }

    /**
     * Creates a new PersonalInfo with updated last name
     */
    public PersonalInfo withLastName(String newLastName) {
        return PersonalInfo.of(this.firstName, newLastName, this.displayName);
    }

    /**
     * Creates a new PersonalInfo with updated display name
     */
    public PersonalInfo withDisplayName(String newDisplayName) {
        return PersonalInfo.of(this.firstName, this.lastName, newDisplayName);
    }

    /**
     * Checks if the person has a custom display name (different from full name)
     */
    public boolean hasCustomDisplayName() {
        return displayName != null && !displayName.equals(getFullName());
    }

    /**
     * Validates a name field
     */
    private static void validateName(String name, String fieldName) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be null or empty");
        }

        String trimmedName = name.trim();
        
        if (trimmedName.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException(fieldName + " cannot exceed " + MAX_NAME_LENGTH + " characters");
        }

        // Check for invalid characters (only letters, spaces, hyphens, and apostrophes allowed)
        if (!trimmedName.matches("^[a-zA-ZÀ-ÿ\\s'-]+$")) {
            throw new IllegalArgumentException(fieldName + " contains invalid characters");
        }

        // Check for reasonable name patterns
        if (trimmedName.matches("^[\\s'-]+$")) {
            throw new IllegalArgumentException(fieldName + " must contain at least one letter");
        }
    }

    /**
     * Normalizes a name by trimming, capitalizing properly
     */
    private static String normalizeName(String name) {
        if (name == null) return null;
        
        String trimmed = name.trim();
        if (trimmed.isEmpty()) return trimmed;

        // Split by spaces and capitalize each part
        String[] parts = trimmed.toLowerCase().split("\\s+");
        StringBuilder normalized = new StringBuilder();

        for (int i = 0; i < parts.length; i++) {
            if (i > 0) {
                normalized.append(" ");
            }
            normalized.append(capitalizeName(parts[i]));
        }

        return normalized.toString();
    }

    /**
     * Capitalizes a name part handling special cases like O'Connor, McDonald, etc.
     */
    private static String capitalizeName(String namePart) {
        if (namePart == null || namePart.isEmpty()) {
            return namePart;
        }

        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;

        for (char c : namePart.toCharArray()) {
            if (Character.isLetter(c)) {
                if (capitalizeNext) {
                    result.append(Character.toUpperCase(c));
                    capitalizeNext = false;
                } else {
                    result.append(Character.toLowerCase(c));
                }
            } else {
                result.append(c);
                // Capitalize after apostrophe or hyphen
                capitalizeNext = (c == '\'' || c == '-');
            }
        }

        return result.toString();
    }

    /**
     * Normalizes display name
     */
    private static String normalizeDisplayName(String displayName) {
        if (displayName == null) return null;
        
        String trimmed = displayName.trim();
        if (trimmed.isEmpty()) return null;
        
        if (trimmed.length() > MAX_DISPLAY_NAME_LENGTH) {
            throw new IllegalArgumentException("Display name cannot exceed " + MAX_DISPLAY_NAME_LENGTH + " characters");
        }

        return trimmed;
    }

    /**
     * Generates default display name from first and last name
     */
    private static String generateDefaultDisplayName(String firstName, String lastName) {
        return firstName + " " + lastName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PersonalInfo that = (PersonalInfo) o;
        return Objects.equals(firstName, that.firstName) &&
               Objects.equals(lastName, that.lastName) &&
               Objects.equals(displayName, that.displayName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName, displayName);
    }

    @Override
    public String toString() {
        return displayName != null ? displayName : getFullName();
    }
}

