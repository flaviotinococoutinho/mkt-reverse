package com.marketplace.user.domain.valueobject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.time.Instant;
import java.util.*;

/**
 * KYC (Know Your Customer) Verification Value Object
 * 
 * Represents the KYC verification state, level, and associated documents.
 * Handles different verification levels and compliance requirements.
 * 
 * Design principles:
 * - Immutable
 * - Self-validating
 * - Compliance-aware
 * - Document tracking
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class KycVerification implements Serializable {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Enumerated(EnumType.STRING)
    @Column(name = "kyc_level", nullable = false)
    private KycLevel level;

    @Enumerated(EnumType.STRING)
    @Column(name = "kyc_status", nullable = false, length = 20)
    private KycStatus status;

    @Column(name = "kyc_verified_at")
    private Instant verifiedAt;

    @Column(name = "kyc_documents", columnDefinition = "TEXT")
    private String documents; // JSON string of document references

    /**
     * Creates a pending KYC verification
     */
    public static KycVerification createPending() {
        return new KycVerification(KycLevel.NONE, KycStatus.PENDING, null, null);
    }

    /**
     * Creates a KYC verification with specific level and status
     */
    public static KycVerification of(KycLevel level, KycStatus status, Instant verifiedAt, Map<String, String> documents) {
        String documentsJson = null;
        if (documents != null && !documents.isEmpty()) {
            try {
                documentsJson = objectMapper.writeValueAsString(documents);
            } catch (JsonProcessingException e) {
                log.warn("Failed to serialize KYC documents", e);
                documentsJson = "{}";
            }
        }
        
        return new KycVerification(level, status, verifiedAt, documentsJson);
    }

    /**
     * Completes KYC verification with the specified level
     */
    public KycVerification complete(KycLevel level) {
        if (this.status == KycStatus.VERIFIED) {
            throw new IllegalStateException("KYC is already verified");
        }
        
        return new KycVerification(level, KycStatus.VERIFIED, Instant.now(), this.documents);
    }

    /**
     * Rejects KYC verification
     */
    public KycVerification reject(String reason) {
        if (this.status == KycStatus.VERIFIED) {
            throw new IllegalStateException("Cannot reject already verified KYC");
        }
        
        // Add rejection reason to documents
        Map<String, String> currentDocs = getDocumentsMap();
        currentDocs.put("rejection_reason", reason);
        currentDocs.put("rejected_at", Instant.now().toString());
        
        try {
            String documentsJson = objectMapper.writeValueAsString(currentDocs);
            return new KycVerification(this.level, KycStatus.REJECTED, null, documentsJson);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize rejection reason", e);
            return new KycVerification(this.level, KycStatus.REJECTED, null, this.documents);
        }
    }

    /**
     * Adds a document to the KYC verification
     */
    public KycVerification addDocument(String documentType, String documentReference) {
        if (documentType == null || documentType.trim().isEmpty()) {
            throw new IllegalArgumentException("Document type cannot be null or empty");
        }
        if (documentReference == null || documentReference.trim().isEmpty()) {
            throw new IllegalArgumentException("Document reference cannot be null or empty");
        }
        
        Map<String, String> currentDocs = getDocumentsMap();
        currentDocs.put(documentType, documentReference);
        currentDocs.put(documentType + "_uploaded_at", Instant.now().toString());
        
        try {
            String documentsJson = objectMapper.writeValueAsString(currentDocs);
            return new KycVerification(this.level, this.status, this.verifiedAt, documentsJson);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize KYC documents", e);
            return this;
        }
    }

    /**
     * Removes a document from the KYC verification
     */
    public KycVerification removeDocument(String documentType) {
        Map<String, String> currentDocs = getDocumentsMap();
        currentDocs.remove(documentType);
        currentDocs.remove(documentType + "_uploaded_at");
        
        try {
            String documentsJson = currentDocs.isEmpty() ? null : objectMapper.writeValueAsString(currentDocs);
            return new KycVerification(this.level, this.status, this.verifiedAt, documentsJson);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize KYC documents", e);
            return this;
        }
    }

    /**
     * Gets the documents as a map
     */
    public Map<String, String> getDocumentsMap() {
        if (documents == null || documents.trim().isEmpty()) {
            return new HashMap<>();
        }
        
        try {
            return objectMapper.readValue(documents, new TypeReference<Map<String, String>>() {});
        } catch (JsonProcessingException e) {
            log.warn("Failed to deserialize KYC documents", e);
            return new HashMap<>();
        }
    }

    /**
     * Checks if KYC is verified
     */
    public boolean isVerified() {
        return status == KycStatus.VERIFIED;
    }

    /**
     * Checks if KYC is pending
     */
    public boolean isPending() {
        return status == KycStatus.PENDING;
    }

    /**
     * Checks if KYC is rejected
     */
    public boolean isRejected() {
        return status == KycStatus.REJECTED;
    }

    /**
     * Checks if KYC is in review
     */
    public boolean isInReview() {
        return status == KycStatus.IN_REVIEW;
    }

    /**
     * Checks if a specific document type is present
     */
    public boolean hasDocument(String documentType) {
        return getDocumentsMap().containsKey(documentType);
    }

    /**
     * Gets a specific document reference
     */
    public String getDocument(String documentType) {
        return getDocumentsMap().get(documentType);
    }

    /**
     * Gets all document types
     */
    public Set<String> getDocumentTypes() {
        return getDocumentsMap().keySet().stream()
            .filter(key -> !key.endsWith("_uploaded_at") && !key.equals("rejection_reason") && !key.equals("rejected_at"))
            .collect(java.util.stream.Collectors.toSet());
    }

    /**
     * Checks if the required documents for the level are present
     */
    public boolean hasRequiredDocuments() {
        Set<String> required = level.getRequiredDocuments();
        Set<String> available = getDocumentTypes();
        
        return available.containsAll(required);
    }

    /**
     * Gets missing required documents
     */
    public Set<String> getMissingDocuments() {
        Set<String> required = level.getRequiredDocuments();
        Set<String> available = getDocumentTypes();
        
        Set<String> missing = new HashSet<>(required);
        missing.removeAll(available);
        
        return missing;
    }

    /**
     * Checks if KYC level meets the minimum requirement
     */
    public boolean meetsMinimumLevel(KycLevel minimumLevel) {
        return this.level.ordinal() >= minimumLevel.ordinal();
    }

    /**
     * Gets the verification age in days
     */
    public long getVerificationAgeDays() {
        if (verifiedAt == null) {
            return 0;
        }
        
        return (Instant.now().getEpochSecond() - verifiedAt.getEpochSecond()) / (24 * 3600);
    }

    /**
     * Checks if verification is expired (based on level requirements)
     */
    public boolean isExpired() {
        if (!isVerified()) {
            return false;
        }
        
        long maxAgeDays = level.getMaxValidityDays();
        return maxAgeDays > 0 && getVerificationAgeDays() > maxAgeDays;
    }

    /**
     * Gets the rejection reason if rejected
     */
    public String getRejectionReason() {
        if (!isRejected()) {
            return null;
        }
        
        return getDocumentsMap().get("rejection_reason");
    }

    /**
     * Gets the progress percentage (0-100)
     */
    public int getProgressPercentage() {
        if (isVerified()) {
            return 100;
        }
        
        Set<String> required = level.getRequiredDocuments();
        if (required.isEmpty()) {
            return status == KycStatus.PENDING ? 0 : 50;
        }
        
        Set<String> available = getDocumentTypes();
        int progress = (available.size() * 100) / required.size();
        
        return Math.min(progress, isInReview() ? 90 : 80);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KycVerification that = (KycVerification) o;
        return level == that.level &&
               status == that.status &&
               Objects.equals(verifiedAt, that.verifiedAt) &&
               Objects.equals(documents, that.documents);
    }

    @Override
    public int hashCode() {
        return Objects.hash(level, status, verifiedAt, documents);
    }

    @Override
    public String toString() {
        return "KycVerification{" +
               "level=" + level +
               ", status=" + status +
               ", progress=" + getProgressPercentage() + "%" +
               '}';
    }
}

