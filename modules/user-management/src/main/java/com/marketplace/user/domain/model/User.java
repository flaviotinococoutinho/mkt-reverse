package com.marketplace.user.domain.model;

import com.marketplace.shared.domain.model.AggregateRoot;
import com.marketplace.shared.valueobject.Money;
import com.marketplace.user.domain.event.UserCreatedEvent;
import com.marketplace.user.domain.event.UserProfileUpdatedEvent;
import com.marketplace.user.domain.event.UserStatusChangedEvent;
import com.marketplace.user.domain.valueobject.*;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * User Aggregate Root
 * 
 * Represents a user in the marketplace system.
 * Encapsulates all business rules related to user management,
 * authentication, profile management, and role assignment.
 * 
 * Follows DDD principles:
 * - Aggregate Root pattern
 * - Rich domain model with business logic
 * - Value objects for complex data
 * - Domain events for state changes
 */
@Entity
@Table(name = "USR_USERS", indexes = {
    @Index(name = "idx_user_email", columnList = "email", unique = true),
    @Index(name = "idx_user_document", columnList = "document_number", unique = true),
    @Index(name = "idx_user_status", columnList = "status"),
    @Index(name = "idx_user_type", columnList = "user_type")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends AggregateRoot<UserId> {

    @EmbeddedId
    private UserId id;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "email", nullable = false, unique = true, length = 255))
    })
    private Email email;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "hashedPassword", column = @Column(name = "password_hash", nullable = false, length = 255)),
        @AttributeOverride(name = "salt", column = @Column(name = "password_salt", nullable = false, length = 255)),
        @AttributeOverride(name = "algorithm", column = @Column(name = "password_algorithm", nullable = false, length = 50))
    })
    private Password password;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "firstName", column = @Column(name = "first_name", nullable = false, length = 100)),
        @AttributeOverride(name = "lastName", column = @Column(name = "last_name", nullable = false, length = 100)),
        @AttributeOverride(name = "displayName", column = @Column(name = "display_name", length = 200))
    })
    private PersonalInfo personalInfo;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "number", column = @Column(name = "document_number", nullable = false, unique = true, length = 20)),
        @AttributeOverride(name = "type", column = @Column(name = "document_type", nullable = false, length = 10))
    })
    private Document document;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "phoneNumber", column = @Column(name = "phone_number", length = 20)),
        @AttributeOverride(name = "countryCode", column = @Column(name = "phone_country_code", length = 5)),
        @AttributeOverride(name = "verified", column = @Column(name = "phone_verified", nullable = false))
    })
    private PhoneNumber phoneNumber;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "street", column = @Column(name = "address_street", length = 255)),
        @AttributeOverride(name = "number", column = @Column(name = "address_number", length = 20)),
        @AttributeOverride(name = "complement", column = @Column(name = "address_complement", length = 100)),
        @AttributeOverride(name = "neighborhood", column = @Column(name = "address_neighborhood", length = 100)),
        @AttributeOverride(name = "city", column = @Column(name = "address_city", length = 100)),
        @AttributeOverride(name = "state", column = @Column(name = "address_state", length = 50)),
        @AttributeOverride(name = "zipCode", column = @Column(name = "address_zip_code", length = 20)),
        @AttributeOverride(name = "country", column = @Column(name = "address_country", length = 50))
    })
    private Address address;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false, length = 20)
    private UserType userType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private UserStatus status;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(
        name = "USR_USER_ROLES",
        joinColumns = @JoinColumn(name = "user_id"),
        indexes = @Index(name = "idx_user_roles_user_id", columnList = "user_id")
    )
    @Column(name = "role", length = 30)
    private Set<UserRole> roles = new HashSet<>();

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "verified", column = @Column(name = "email_verified", nullable = false)),
        @AttributeOverride(name = "verificationToken", column = @Column(name = "email_verification_token", length = 255)),
        @AttributeOverride(name = "verificationExpiresAt", column = @Column(name = "email_verification_expires_at"))
    })
    private EmailVerification emailVerification;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "level", column = @Column(name = "kyc_level", nullable = false)),
        @AttributeOverride(name = "status", column = @Column(name = "kyc_status", nullable = false, length = 20)),
        @AttributeOverride(name = "verifiedAt", column = @Column(name = "kyc_verified_at")),
        @AttributeOverride(name = "documents", column = @Column(name = "kyc_documents", columnDefinition = "TEXT"))
    })
    private KycVerification kycVerification;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Column(name = "login_attempts", nullable = false)
    private Integer loginAttempts = 0;

    @Column(name = "locked_until")
    private Instant lockedUntil;

    @Column(name = "terms_accepted_at")
    private Instant termsAcceptedAt;

    @Column(name = "privacy_policy_accepted_at")
    private Instant privacyPolicyAcceptedAt;

    // Factory method for creating new users
    public static User create(
            Email email,
            Password password,
            PersonalInfo personalInfo,
            Document document,
            UserType userType) {
        
        User user = new User();
        user.id = UserId.generate();
        user.email = email;
        user.password = password;
        user.personalInfo = personalInfo;
        user.document = document;
        user.userType = userType;
        user.status = UserStatus.PENDING_VERIFICATION;
        user.emailVerification = EmailVerification.createPending();
        user.kycVerification = KycVerification.createPending();
        
        // Add default role based on user type
        user.roles.add(userType == UserType.BUYER ? UserRole.BUYER : UserRole.SUPPLIER);
        
        // Raise domain event
        user.addDomainEvent(new UserCreatedEvent(
            user.id.getValue().toString(),
            user.email.getValue(),
            user.userType,
            user.personalInfo.getDisplayName()
        ));
        
        user.markAsCreated();
        return user;
    }

    // Business methods following DDD principles

    public void updateProfile(PersonalInfo newPersonalInfo, PhoneNumber newPhoneNumber, Address newAddress) {
        validateActiveStatus();
        
        PersonalInfo oldPersonalInfo = this.personalInfo;
        this.personalInfo = newPersonalInfo;
        this.phoneNumber = newPhoneNumber;
        this.address = newAddress;
        
        addDomainEvent(new UserProfileUpdatedEvent(
            this.id.getValue().toString(),
            oldPersonalInfo,
            newPersonalInfo
        ));
        
        markAsUpdated();
    }

    /**
     * Changes the user's password after validating the current password.
     * 
     * @param currentPlainPassword the current password in plain text
     * @param newPlainPassword the new password in plain text
     * @throws IllegalArgumentException if current password is incorrect
     * @throws IllegalStateException if user is not active
     */
    public void changePassword(String currentPlainPassword, String newPlainPassword) {
        validateActiveStatus();
        
        if (!this.password.matchesPlainText(currentPlainPassword)) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        
        this.password = Password.of(newPlainPassword);
        markAsUpdated();
    }

    public void activate() {
        if (this.status == UserStatus.ACTIVE) {
            throw new IllegalStateException("User is already active");
        }
        
        UserStatus oldStatus = this.status;
        this.status = UserStatus.ACTIVE;
        
        addDomainEvent(new UserStatusChangedEvent(
            this.id.getValue().toString(),
            oldStatus,
            this.status
        ));
        
        markAsUpdated();
    }

    public void suspend(String reason) {
        validateActiveStatus();
        
        UserStatus oldStatus = this.status;
        this.status = UserStatus.SUSPENDED;
        
        addDomainEvent(new UserStatusChangedEvent(
            this.id.getValue().toString(),
            oldStatus,
            this.status,
            reason
        ));
        
        markAsUpdated();
    }

    public void deactivate() {
        validateActiveStatus();
        
        UserStatus oldStatus = this.status;
        this.status = UserStatus.INACTIVE;
        
        addDomainEvent(new UserStatusChangedEvent(
            this.id.getValue().toString(),
            oldStatus,
            this.status
        ));
        
        markAsUpdated();
    }

    public void verifyEmail() {
        this.emailVerification = this.emailVerification.markAsVerified();
        
        // If email is verified and KYC is complete, activate user
        if (this.kycVerification.isVerified() && this.status == UserStatus.PENDING_VERIFICATION) {
            activate();
        }
        
        markAsUpdated();
    }

    public void completeKyc(KycLevel level) {
        this.kycVerification = this.kycVerification.complete(level);
        
        // If KYC is complete and email is verified, activate user
        if (this.emailVerification.isVerified() && this.status == UserStatus.PENDING_VERIFICATION) {
            activate();
        }
        
        markAsUpdated();
    }

    public void addRole(UserRole role) {
        validateActiveStatus();
        
        if (this.roles.add(role)) {
            markAsUpdated();
        }
    }

    public void removeRole(UserRole role) {
        validateActiveStatus();
        
        // Ensure user always has at least one role
        if (this.roles.size() <= 1) {
            throw new IllegalStateException("User must have at least one role");
        }
        
        if (this.roles.remove(role)) {
            markAsUpdated();
        }
    }

    public void recordLogin() {
        this.lastLoginAt = Instant.now();
        this.loginAttempts = 0;
        this.lockedUntil = null;
        markAsUpdated();
    }

    public void recordFailedLogin() {
        this.loginAttempts++;
        
        // Lock account after 5 failed attempts for 30 minutes
        if (this.loginAttempts >= 5) {
            this.lockedUntil = Instant.now().plusSeconds(1800); // 30 minutes
        }
        
        markAsUpdated();
    }

    public void acceptTerms() {
        this.termsAcceptedAt = Instant.now();
        markAsUpdated();
    }

    public void acceptPrivacyPolicy() {
        this.privacyPolicyAcceptedAt = Instant.now();
        markAsUpdated();
    }

    // Query methods

    public boolean isActive() {
        return this.status == UserStatus.ACTIVE;
    }

    public boolean isLocked() {
        return this.lockedUntil != null && this.lockedUntil.isAfter(Instant.now());
    }

    public boolean hasRole(UserRole role) {
        return this.roles.contains(role);
    }

    public boolean isBuyer() {
        return this.userType == UserType.BUYER;
    }

    public boolean isSupplier() {
        return this.userType == UserType.SUPPLIER;
    }

    public boolean isEmailVerified() {
        return this.emailVerification.isVerified();
    }

    public boolean isKycVerified() {
        return this.kycVerification.isVerified();
    }

    public boolean hasAcceptedTerms() {
        return this.termsAcceptedAt != null;
    }

    public boolean hasAcceptedPrivacyPolicy() {
        return this.privacyPolicyAcceptedAt != null;
    }

    // Validation methods

    @Override
    public void validate() {
        if (id == null) {
            throw new IllegalStateException("User ID cannot be null");
        }
        if (email == null) {
            throw new IllegalStateException("Email cannot be null");
        }
        if (password == null) {
            throw new IllegalStateException("Password cannot be null");
        }
        if (personalInfo == null) {
            throw new IllegalStateException("Personal info cannot be null");
        }
        if (document == null) {
            throw new IllegalStateException("Document cannot be null");
        }
        if (userType == null) {
            throw new IllegalStateException("User type cannot be null");
        }
        if (status == null) {
            throw new IllegalStateException("Status cannot be null");
        }
        if (roles.isEmpty()) {
            throw new IllegalStateException("User must have at least one role");
        }
    }

    private void validateActiveStatus() {
        if (!isActive()) {
            throw new IllegalStateException("User must be active to perform this operation");
        }
    }

    @Override
    protected void markAsCreated() {
        // Additional creation logic if needed
    }

    @Override
    protected void markAsUpdated() {
        // Additional update logic if needed
    }
}

