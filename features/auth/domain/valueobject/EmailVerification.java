package com.marketplace.user.domain.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Objects;

/**
 * Email Verification Value Object
 * 
 * Represents the email verification state and process.
 * Handles verification tokens, expiration, and status tracking.
 * 
 * Design principles:
 * - Immutable
 * - Self-validating
 * - Secure token generation
 * - Time-based expiration
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EmailVerification implements Serializable {

    private static final int TOKEN_LENGTH = 32;
    private static final long VERIFICATION_EXPIRY_HOURS = 24; // 24 hours
    private static final String TOKEN_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    @Column(name = "email_verified", nullable = false)
    private boolean verified;

    @Column(name = "email_verification_token", length = 255)
    private String verificationToken;

    @Column(name = "email_verification_expires_at")
    private Instant verificationExpiresAt;

    /**
     * Creates a pending email verification with a new token
     */
    public static EmailVerification createPending() {
        String token = generateSecureToken();
        Instant expiresAt = Instant.now().plusSeconds(VERIFICATION_EXPIRY_HOURS * 3600);
        
        return new EmailVerification(false, token, expiresAt);
    }

    /**
     * Creates a verified email verification (no token needed)
     */
    public static EmailVerification createVerified() {
        return new EmailVerification(true, null, null);
    }

    /**
     * Creates an email verification from existing data
     */
    public static EmailVerification of(boolean verified, String token, Instant expiresAt) {
        return new EmailVerification(verified, token, expiresAt);
    }

    /**
     * Marks this verification as verified
     */
    public EmailVerification markAsVerified() {
        return new EmailVerification(true, null, null);
    }

    /**
     * Generates a new verification token (for resending verification)
     */
    public EmailVerification regenerateToken() {
        if (verified) {
            throw new IllegalStateException("Cannot regenerate token for already verified email");
        }
        
        String newToken = generateSecureToken();
        Instant newExpiresAt = Instant.now().plusSeconds(VERIFICATION_EXPIRY_HOURS * 3600);
        
        return new EmailVerification(false, newToken, newExpiresAt);
    }

    /**
     * Checks if the verification token is expired
     */
    public boolean isTokenExpired() {
        if (verified || verificationExpiresAt == null) {
            return false;
        }
        return Instant.now().isAfter(verificationExpiresAt);
    }

    /**
     * Checks if the verification token is valid (not expired and matches)
     */
    public boolean isTokenValid(String token) {
        if (verified || token == null || verificationToken == null) {
            return false;
        }
        
        if (isTokenExpired()) {
            return false;
        }
        
        return verificationToken.equals(token);
    }

    /**
     * Verifies the email with the provided token
     */
    public EmailVerification verify(String token) {
        if (verified) {
            throw new IllegalStateException("Email is already verified");
        }
        
        if (!isTokenValid(token)) {
            throw new IllegalArgumentException("Invalid or expired verification token");
        }
        
        return markAsVerified();
    }

    /**
     * Gets the remaining time until token expiration
     */
    public long getRemainingTimeSeconds() {
        if (verified || verificationExpiresAt == null) {
            return 0;
        }
        
        long remaining = verificationExpiresAt.getEpochSecond() - Instant.now().getEpochSecond();
        return Math.max(0, remaining);
    }

    /**
     * Gets the remaining time until token expiration in hours
     */
    public long getRemainingTimeHours() {
        return getRemainingTimeSeconds() / 3600;
    }

    /**
     * Checks if a new token can be generated (rate limiting)
     */
    public boolean canRegenerateToken() {
        if (verified) {
            return false;
        }
        
        // Allow regeneration if token is expired or will expire in less than 1 hour
        return isTokenExpired() || getRemainingTimeHours() < 1;
    }

    /**
     * Gets the verification status as a string
     */
    public String getStatus() {
        if (verified) {
            return "VERIFIED";
        } else if (isTokenExpired()) {
            return "EXPIRED";
        } else {
            return "PENDING";
        }
    }

    /**
     * Gets a masked version of the verification token for logging
     */
    public String getMaskedToken() {
        if (verificationToken == null || verificationToken.length() < 8) {
            return "****";
        }
        
        return verificationToken.substring(0, 4) + 
               "*".repeat(verificationToken.length() - 8) + 
               verificationToken.substring(verificationToken.length() - 4);
    }

    /**
     * Generates a cryptographically secure random token
     */
    private static String generateSecureToken() {
        SecureRandom random = new SecureRandom();
        StringBuilder token = new StringBuilder(TOKEN_LENGTH);
        
        for (int i = 0; i < TOKEN_LENGTH; i++) {
            int index = random.nextInt(TOKEN_CHARS.length());
            token.append(TOKEN_CHARS.charAt(index));
        }
        
        return token.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmailVerification that = (EmailVerification) o;
        return verified == that.verified &&
               Objects.equals(verificationToken, that.verificationToken) &&
               Objects.equals(verificationExpiresAt, that.verificationExpiresAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(verified, verificationToken, verificationExpiresAt);
    }

    @Override
    public String toString() {
        return "EmailVerification{" +
               "status=" + getStatus() +
               ", expiresAt=" + verificationExpiresAt +
               '}';
    }
}

