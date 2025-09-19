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
 * Email Value Object
 * 
 * Represents an email address with validation.
 * Ensures email format is valid and provides domain-specific operations.
 * 
 * Design principles:
 * - Immutable
 * - Self-validating
 * - Value-based equality
 * - Rich behavior (domain operations)
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Email implements Serializable {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    private static final int MAX_LENGTH = 255;

    @Column(name = "email", nullable = false, length = MAX_LENGTH)
    private String value;

    /**
     * Creates an Email from a string value
     */
    public static Email of(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }

        String normalizedEmail = email.trim().toLowerCase();
        
        if (normalizedEmail.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Email cannot exceed " + MAX_LENGTH + " characters");
        }

        if (!EMAIL_PATTERN.matcher(normalizedEmail).matches()) {
            throw new IllegalArgumentException("Invalid email format: " + email);
        }

        return new Email(normalizedEmail);
    }

    /**
     * Gets the domain part of the email
     */
    public String getDomain() {
        int atIndex = value.indexOf('@');
        return atIndex > 0 ? value.substring(atIndex + 1) : "";
    }

    /**
     * Gets the local part of the email (before @)
     */
    public String getLocalPart() {
        int atIndex = value.indexOf('@');
        return atIndex > 0 ? value.substring(0, atIndex) : value;
    }

    /**
     * Checks if the email belongs to a specific domain
     */
    public boolean belongsToDomain(String domain) {
        if (domain == null || domain.trim().isEmpty()) {
            return false;
        }
        return getDomain().equalsIgnoreCase(domain.trim());
    }

    /**
     * Checks if the email is from a corporate domain (not common free email providers)
     */
    public boolean isCorporateEmail() {
        String domain = getDomain().toLowerCase();
        
        // Common free email providers
        return !domain.equals("gmail.com") &&
               !domain.equals("yahoo.com") &&
               !domain.equals("hotmail.com") &&
               !domain.equals("outlook.com") &&
               !domain.equals("live.com") &&
               !domain.equals("icloud.com") &&
               !domain.equals("protonmail.com") &&
               !domain.equals("aol.com");
    }

    /**
     * Creates a masked version of the email for display purposes
     * Example: john.doe@example.com -> j***e@example.com
     */
    public String getMasked() {
        String localPart = getLocalPart();
        String domain = getDomain();
        
        if (localPart.length() <= 2) {
            return localPart.charAt(0) + "***@" + domain;
        }
        
        return localPart.charAt(0) + 
               "*".repeat(Math.max(1, localPart.length() - 2)) + 
               localPart.charAt(localPart.length() - 1) + 
               "@" + domain;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Email email = (Email) o;
        return Objects.equals(value, email.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}

