package com.marketplace.user.domain.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Password Value Object
 * 
 * Represents a secure password with hashing and validation.
 * Handles password strength validation, hashing, and comparison.
 * 
 * Design principles:
 * - Immutable
 * - Secure (never stores plain text)
 * - Self-validating
 * - Cryptographically secure hashing
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Password implements Serializable {

    private static final String DEFAULT_ALGORITHM = "SHA-256";
    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 128;
    
    // Password must contain at least one uppercase, one lowercase, one digit, and one special character
    private static final Pattern STRONG_PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{"+ MIN_LENGTH + "," + MAX_LENGTH + "}$"
    );

    @Column(name = "password_hash", nullable = false, length = 255)
    private String hashedPassword;

    @Column(name = "password_salt", nullable = false, length = 255)
    private String salt;

    @Column(name = "password_algorithm", nullable = false, length = 50)
    private String algorithm;

    /**
     * Creates a Password from a plain text password
     */
    public static Password of(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }

        validatePasswordStrength(plainPassword);

        String salt = generateSalt();
        String hashedPassword = hashPassword(plainPassword, salt, DEFAULT_ALGORITHM);

        return new Password(hashedPassword, salt, DEFAULT_ALGORITHM);
    }

    /**
     * Creates a Password from existing hash, salt, and algorithm (for loading from database)
     */
    public static Password fromHash(String hashedPassword, String salt, String algorithm) {
        if (hashedPassword == null || hashedPassword.isEmpty()) {
            throw new IllegalArgumentException("Hashed password cannot be null or empty");
        }
        if (salt == null || salt.isEmpty()) {
            throw new IllegalArgumentException("Salt cannot be null or empty");
        }
        if (algorithm == null || algorithm.isEmpty()) {
            throw new IllegalArgumentException("Algorithm cannot be null or empty");
        }

        return new Password(hashedPassword, salt, algorithm);
    }

    /**
     * Verifies if the provided plain password matches this password
     */
    public boolean matches(String plainPassword) {
        if (plainPassword == null) {
            return false;
        }

        String hashedInput = hashPassword(plainPassword, this.salt, this.algorithm);
        return MessageDigest.isEqual(
            this.hashedPassword.getBytes(),
            hashedInput.getBytes()
        );
    }

    /**
     * Verifies if the provided password object matches this password
     */
    public boolean matches(Password other) {
        if (other == null) {
            return false;
        }

        return MessageDigest.isEqual(
            this.hashedPassword.getBytes(),
            other.hashedPassword.getBytes()
        ) && Objects.equals(this.salt, other.salt) &&
            Objects.equals(this.algorithm, other.algorithm);
    }

    /**
     * Checks if the password needs to be rehashed (algorithm upgrade)
     */
    public boolean needsRehash() {
        return !DEFAULT_ALGORITHM.equals(this.algorithm);
    }

    /**
     * Validates password strength according to security policy
     */
    private static void validatePasswordStrength(String password) {
        if (password.length() < MIN_LENGTH) {
            throw new IllegalArgumentException("Password must be at least " + MIN_LENGTH + " characters long");
        }

        if (password.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Password cannot exceed " + MAX_LENGTH + " characters");
        }

        if (!STRONG_PASSWORD_PATTERN.matcher(password).matches()) {
            throw new IllegalArgumentException(
                "Password must contain at least one uppercase letter, one lowercase letter, " +
                "one digit, and one special character (@$!%*?&)"
            );
        }

        // Check for common weak passwords
        if (isCommonPassword(password)) {
            throw new IllegalArgumentException("Password is too common and not secure");
        }
    }

    /**
     * Checks if the password is in the list of common weak passwords
     */
    private static boolean isCommonPassword(String password) {
        String lowerPassword = password.toLowerCase();
        
        // Common weak passwords
        String[] commonPasswords = {
            "password", "123456", "password123", "admin", "qwerty",
            "letmein", "welcome", "monkey", "dragon", "master",
            "123456789", "12345678", "1234567", "password1"
        };

        for (String common : commonPasswords) {
            if (lowerPassword.contains(common)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Generates a cryptographically secure random salt
     */
    private static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[32]; // 256 bits
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Hashes a password with salt using the specified algorithm
     */
    private static String hashPassword(String password, String salt, String algorithm) {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            
            // Add salt to prevent rainbow table attacks
            String saltedPassword = password + salt;
            
            // Perform multiple iterations for additional security
            byte[] hash = saltedPassword.getBytes();
            for (int i = 0; i < 10000; i++) { // 10,000 iterations
                digest.reset();
                hash = digest.digest(hash);
            }
            
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hashing algorithm not available: " + algorithm, e);
        }
    }

    /**
     * Returns the password strength level
     */
    public PasswordStrength getStrength(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            return PasswordStrength.VERY_WEAK;
        }

        int score = 0;

        // Length
        if (plainPassword.length() >= 8) score++;
        if (plainPassword.length() >= 12) score++;
        if (plainPassword.length() >= 16) score++;

        // Character types
        if (plainPassword.matches(".*[a-z].*")) score++;
        if (plainPassword.matches(".*[A-Z].*")) score++;
        if (plainPassword.matches(".*\\d.*")) score++;
        if (plainPassword.matches(".*[@$!%*?&].*")) score++;

        // Additional complexity
        if (plainPassword.matches(".*[^A-Za-z\\d@$!%*?&].*")) score++; // Other special chars

        return switch (score) {
            case 0, 1, 2 -> PasswordStrength.VERY_WEAK;
            case 3, 4 -> PasswordStrength.WEAK;
            case 5, 6 -> PasswordStrength.MEDIUM;
            case 7 -> PasswordStrength.STRONG;
            default -> PasswordStrength.VERY_STRONG;
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Password password = (Password) o;
        return Objects.equals(hashedPassword, password.hashedPassword) &&
               Objects.equals(salt, password.salt) &&
               Objects.equals(algorithm, password.algorithm);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hashedPassword, salt, algorithm);
    }

    @Override
    public String toString() {
        return "[PROTECTED]"; // Never expose password details
    }

    /**
     * Password strength levels
     */
    public enum PasswordStrength {
        VERY_WEAK,
        WEAK,
        MEDIUM,
        STRONG,
        VERY_STRONG
    }
}

