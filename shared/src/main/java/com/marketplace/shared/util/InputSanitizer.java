package com.marketplace.shared.util;

import org.apache.commons.text.StringEscapeUtils;

/**
 * Input sanitization utilities.
 * 
 * Provides XSS protection by escaping HTML entities.
 * 
 * Object Calisthenics:
 * - Single responsibility class
 * - Immutable methods
 * - No magic numbers
 */
public final class InputSanitizer {

    private InputSanitizer() {
        // Utility class - no instantiation
    }

    /**
     * Escapes HTML for safe display.
     * Prevents XSS attacks.
     */
    public static String escapeHtml(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return StringEscapeUtils.escapeHtml4(input);
    }

    /**
     * Escapes HTML for attribute values.
     */
    public static String escapeHtmlAttribute(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return StringEscapeUtils.escapeHtml4(input)
                .replace("'", "&#39;")
                .replace("\"", "&#34;");
    }

    /**
     * Escapes for JavaScript strings.
     */
    public static String escapeJavaScript(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return input
                .replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    /**
     * Trims and validates max length.
     */
    public static String sanitize(String input, int maxLength) {
        if (input == null) {
            return null;
        }
        var trimmed = input.trim();
        if (trimmed.length() > maxLength) {
            return trimmed.substring(0, maxLength);
        }
        return trimmed;
    }

    /**
     * Validates alphanumeric input.
     */
    public static boolean isAlphanumeric(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        return input.matches("^[a-zA-Z0-9]+$");
    }

    /**
     * Validates numeric input.
     */
    public static boolean isNumeric(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        return input.matches("^[0-9]+$");
    }
}