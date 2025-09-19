package com.marketplace.user.domain.valueobject;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Document Value Object
 * 
 * Represents official identification documents (CPF, CNPJ, RG, etc.).
 * Handles validation, formatting, and business rules for different document types.
 * 
 * Design principles:
 * - Immutable
 * - Self-validating with specific rules per document type
 * - Rich behavior for document operations
 * - Brazilian document standards compliant
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Document implements Serializable {

    @Column(name = "document_number", nullable = false, length = 20)
    private String number;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 10)
    private DocumentType type;

    /**
     * Creates a Document with validation
     */
    public static Document of(String number, DocumentType type) {
        if (number == null || number.trim().isEmpty()) {
            throw new IllegalArgumentException("Document number cannot be null or empty");
        }
        if (type == null) {
            throw new IllegalArgumentException("Document type cannot be null");
        }

        String cleanNumber = cleanDocumentNumber(number);
        validateDocument(cleanNumber, type);

        return new Document(cleanNumber, type);
    }

    /**
     * Creates a CPF document
     */
    public static Document cpf(String cpf) {
        return of(cpf, DocumentType.CPF);
    }

    /**
     * Creates a CNPJ document
     */
    public static Document cnpj(String cnpj) {
        return of(cnpj, DocumentType.CNPJ);
    }

    /**
     * Creates an RG document
     */
    public static Document rg(String rg) {
        return of(rg, DocumentType.RG);
    }

    /**
     * Creates a Passport document
     */
    public static Document passport(String passport) {
        return of(passport, DocumentType.PASSPORT);
    }

    /**
     * Gets the formatted document number
     */
    public String getFormattedNumber() {
        return switch (type) {
            case CPF -> formatCpf(number);
            case CNPJ -> formatCnpj(number);
            case RG -> formatRg(number);
            case PASSPORT -> number.toUpperCase();
            case OTHER -> number;
        };
    }

    /**
     * Gets the masked document number for display
     */
    public String getMaskedNumber() {
        return switch (type) {
            case CPF -> maskCpf(number);
            case CNPJ -> maskCnpj(number);
            case RG -> maskRg(number);
            case PASSPORT -> maskPassport(number);
            case OTHER -> maskGeneric(number);
        };
    }

    /**
     * Checks if this is a personal document (CPF, RG, Passport)
     */
    public boolean isPersonalDocument() {
        return type == DocumentType.CPF || type == DocumentType.RG || type == DocumentType.PASSPORT;
    }

    /**
     * Checks if this is a business document (CNPJ)
     */
    public boolean isBusinessDocument() {
        return type == DocumentType.CNPJ;
    }

    /**
     * Gets the document issuer/authority
     */
    public String getIssuer() {
        return switch (type) {
            case CPF -> "Receita Federal";
            case CNPJ -> "Receita Federal";
            case RG -> "Secretaria de Segurança Pública";
            case PASSPORT -> "Polícia Federal";
            case OTHER -> "Não especificado";
        };
    }

    /**
     * Cleans document number removing formatting
     */
    private static String cleanDocumentNumber(String number) {
        return number.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
    }

    /**
     * Validates document according to its type
     */
    private static void validateDocument(String cleanNumber, DocumentType type) {
        switch (type) {
            case CPF -> validateCpf(cleanNumber);
            case CNPJ -> validateCnpj(cleanNumber);
            case RG -> validateRg(cleanNumber);
            case PASSPORT -> validatePassport(cleanNumber);
            case OTHER -> validateOther(cleanNumber);
        }
    }

    /**
     * Validates CPF using official algorithm
     */
    private static void validateCpf(String cpf) {
        if (cpf.length() != 11) {
            throw new IllegalArgumentException("CPF must have 11 digits");
        }

        if (!cpf.matches("\\d{11}")) {
            throw new IllegalArgumentException("CPF must contain only digits");
        }

        // Check for known invalid CPFs (all same digits)
        if (cpf.matches("(\\d)\\1{10}")) {
            throw new IllegalArgumentException("Invalid CPF format");
        }

        // Validate check digits
        if (!isValidCpfCheckDigits(cpf)) {
            throw new IllegalArgumentException("Invalid CPF check digits");
        }
    }

    /**
     * Validates CNPJ using official algorithm
     */
    private static void validateCnpj(String cnpj) {
        if (cnpj.length() != 14) {
            throw new IllegalArgumentException("CNPJ must have 14 digits");
        }

        if (!cnpj.matches("\\d{14}")) {
            throw new IllegalArgumentException("CNPJ must contain only digits");
        }

        // Check for known invalid CNPJs (all same digits)
        if (cnpj.matches("(\\d)\\1{13}")) {
            throw new IllegalArgumentException("Invalid CNPJ format");
        }

        // Validate check digits
        if (!isValidCnpjCheckDigits(cnpj)) {
            throw new IllegalArgumentException("Invalid CNPJ check digits");
        }
    }

    /**
     * Validates RG format
     */
    private static void validateRg(String rg) {
        if (rg.length() < 7 || rg.length() > 12) {
            throw new IllegalArgumentException("RG must have between 7 and 12 characters");
        }

        if (!rg.matches("[0-9A-Z]+")) {
            throw new IllegalArgumentException("RG must contain only digits and letters");
        }
    }

    /**
     * Validates Passport format
     */
    private static void validatePassport(String passport) {
        if (passport.length() < 6 || passport.length() > 12) {
            throw new IllegalArgumentException("Passport must have between 6 and 12 characters");
        }

        if (!passport.matches("[A-Z0-9]+")) {
            throw new IllegalArgumentException("Passport must contain only uppercase letters and digits");
        }
    }

    /**
     * Validates other document types
     */
    private static void validateOther(String document) {
        if (document.length() < 3 || document.length() > 20) {
            throw new IllegalArgumentException("Document must have between 3 and 20 characters");
        }
    }

    /**
     * Validates CPF check digits
     */
    private static boolean isValidCpfCheckDigits(String cpf) {
        // First check digit
        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += Character.getNumericValue(cpf.charAt(i)) * (10 - i);
        }
        int firstDigit = 11 - (sum % 11);
        if (firstDigit >= 10) firstDigit = 0;

        if (firstDigit != Character.getNumericValue(cpf.charAt(9))) {
            return false;
        }

        // Second check digit
        sum = 0;
        for (int i = 0; i < 10; i++) {
            sum += Character.getNumericValue(cpf.charAt(i)) * (11 - i);
        }
        int secondDigit = 11 - (sum % 11);
        if (secondDigit >= 10) secondDigit = 0;

        return secondDigit == Character.getNumericValue(cpf.charAt(10));
    }

    /**
     * Validates CNPJ check digits
     */
    private static boolean isValidCnpjCheckDigits(String cnpj) {
        // First check digit
        int[] weights1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int sum = 0;
        for (int i = 0; i < 12; i++) {
            sum += Character.getNumericValue(cnpj.charAt(i)) * weights1[i];
        }
        int firstDigit = 11 - (sum % 11);
        if (firstDigit >= 10) firstDigit = 0;

        if (firstDigit != Character.getNumericValue(cnpj.charAt(12))) {
            return false;
        }

        // Second check digit
        int[] weights2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        sum = 0;
        for (int i = 0; i < 13; i++) {
            sum += Character.getNumericValue(cnpj.charAt(i)) * weights2[i];
        }
        int secondDigit = 11 - (sum % 11);
        if (secondDigit >= 10) secondDigit = 0;

        return secondDigit == Character.getNumericValue(cnpj.charAt(13));
    }

    /**
     * Formats CPF: 12345678901 -> 123.456.789-01
     */
    private static String formatCpf(String cpf) {
        return cpf.replaceAll("(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.$2.$3-$4");
    }

    /**
     * Formats CNPJ: 12345678000195 -> 12.345.678/0001-95
     */
    private static String formatCnpj(String cnpj) {
        return cnpj.replaceAll("(\\d{2})(\\d{3})(\\d{3})(\\d{4})(\\d{2})", "$1.$2.$3/$4-$5");
    }

    /**
     * Formats RG: 123456789 -> 12.345.678-9
     */
    private static String formatRg(String rg) {
        if (rg.length() == 9) {
            return rg.replaceAll("(\\d{2})(\\d{3})(\\d{3})(\\w{1})", "$1.$2.$3-$4");
        }
        return rg;
    }

    /**
     * Masks CPF: 12345678901 -> 123.456.***-**
     */
    private static String maskCpf(String cpf) {
        return cpf.substring(0, 3) + "." + cpf.substring(3, 6) + ".***-**";
    }

    /**
     * Masks CNPJ: 12345678000195 -> 12.345.***/**01-**
     */
    private static String maskCnpj(String cnpj) {
        return cnpj.substring(0, 2) + "." + cnpj.substring(2, 5) + ".***/**" + cnpj.substring(10, 12) + "-**";
    }

    /**
     * Masks RG: 123456789 -> 12.345.***-*
     */
    private static String maskRg(String rg) {
        if (rg.length() >= 7) {
            return rg.substring(0, 2) + "." + rg.substring(2, 5) + ".***-*";
        }
        return "***";
    }

    /**
     * Masks Passport: ABC123456 -> ABC***456
     */
    private static String maskPassport(String passport) {
        if (passport.length() >= 6) {
            return passport.substring(0, 3) + "***" + passport.substring(passport.length() - 3);
        }
        return "***";
    }

    /**
     * Masks generic document
     */
    private static String maskGeneric(String document) {
        if (document.length() <= 3) {
            return "***";
        }
        return document.substring(0, 2) + "*".repeat(document.length() - 4) + document.substring(document.length() - 2);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Document document = (Document) o;
        return Objects.equals(number, document.number) && type == document.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(number, type);
    }

    @Override
    public String toString() {
        return getFormattedNumber();
    }

    /**
     * Document Type Enumeration
     */
    public enum DocumentType {
        CPF("CPF", "Cadastro de Pessoa Física"),
        CNPJ("CNPJ", "Cadastro Nacional da Pessoa Jurídica"),
        RG("RG", "Registro Geral"),
        PASSPORT("Passport", "Passaporte"),
        OTHER("Other", "Outro documento");

        @Getter
        private final String code;
        @Getter
        private final String description;

        DocumentType(String code, String description) {
            this.code = code;
            this.description = description;
        }

        @Override
        public String toString() {
            return description;
        }
    }
}

