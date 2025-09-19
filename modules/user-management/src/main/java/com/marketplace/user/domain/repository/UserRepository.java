package com.marketplace.user.domain.repository;

import com.marketplace.user.domain.model.User;
import com.marketplace.user.domain.valueobject.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * User Repository Interface
 * 
 * Defines the contract for User aggregate persistence operations.
 * Follows Repository pattern from DDD with rich query methods.
 * 
 * Design principles:
 * - Domain-focused interface (no infrastructure concerns)
 * - Rich query methods for business use cases
 * - Optional return types for null safety
 * - Pagination support for large datasets
 */
public interface UserRepository {

    /**
     * Saves a user (create or update)
     */
    User save(User user);

    /**
     * Saves multiple users in batch
     */
    List<User> saveAll(List<User> users);

    /**
     * Finds a user by ID
     */
    Optional<User> findById(UserId userId);

    /**
     * Finds a user by email
     */
    Optional<User> findByEmail(Email email);

    /**
     * Finds a user by document
     */
    Optional<User> findByDocument(Document document);

    /**
     * Finds users by status
     */
    List<User> findByStatus(UserStatus status);

    /**
     * Finds users by type
     */
    List<User> findByUserType(UserType userType);

    /**
     * Finds users by role
     */
    List<User> findByRole(UserRole role);

    /**
     * Finds users by KYC level
     */
    List<User> findByKycLevel(KycLevel kycLevel);

    /**
     * Finds users by KYC status
     */
    List<User> findByKycStatus(KycStatus kycStatus);

    /**
     * Finds all users with pagination
     */
    Page<User> findAll(Pageable pageable);

    /**
     * Finds users by status with pagination
     */
    Page<User> findByStatus(UserStatus status, Pageable pageable);

    /**
     * Finds users by type with pagination
     */
    Page<User> findByUserType(UserType userType, Pageable pageable);

    /**
     * Finds users created after a specific date
     */
    List<User> findByCreatedAtAfter(Instant createdAt);

    /**
     * Finds users with last login before a specific date
     */
    List<User> findByLastLoginAtBefore(Instant lastLoginAt);

    /**
     * Finds users with failed login attempts above threshold
     */
    List<User> findByLoginAttemptsGreaterThan(int attempts);

    /**
     * Finds locked users
     */
    List<User> findLockedUsers();

    /**
     * Finds users with unverified email
     */
    List<User> findByEmailNotVerified();

    /**
     * Finds users with expired KYC
     */
    List<User> findByKycExpired();

    /**
     * Finds users by city
     */
    List<User> findByCity(String city);

    /**
     * Finds users by state
     */
    List<User> findByState(String state);

    /**
     * Finds users by country
     */
    List<User> findByCountry(String country);

    /**
     * Searches users by name (first name, last name, or display name)
     */
    Page<User> searchByName(String name, Pageable pageable);

    /**
     * Searches users by email pattern
     */
    Page<User> searchByEmailPattern(String emailPattern, Pageable pageable);

    /**
     * Advanced search with multiple criteria
     */
    Page<User> search(UserSearchCriteria criteria, Pageable pageable);

    /**
     * Counts users by status
     */
    long countByStatus(UserStatus status);

    /**
     * Counts users by type
     */
    long countByUserType(UserType userType);

    /**
     * Counts users by KYC level
     */
    long countByKycLevel(KycLevel kycLevel);

    /**
     * Counts users created today
     */
    long countCreatedToday();

    /**
     * Counts active users (logged in within last 30 days)
     */
    long countActiveUsers();

    /**
     * Checks if email exists
     */
    boolean existsByEmail(Email email);

    /**
     * Checks if document exists
     */
    boolean existsByDocument(Document document);

    /**
     * Checks if phone number exists
     */
    boolean existsByPhoneNumber(PhoneNumber phoneNumber);

    /**
     * Deletes a user by ID
     */
    void deleteById(UserId userId);

    /**
     * Deletes a user
     */
    void delete(User user);

    /**
     * Checks if user exists by ID
     */
    boolean existsById(UserId userId);

    /**
     * Gets user statistics
     */
    UserStatistics getStatistics();

    /**
     * User Search Criteria for advanced search
     */
    class UserSearchCriteria {
        private String name;
        private String email;
        private UserType userType;
        private UserStatus status;
        private KycLevel kycLevel;
        private KycStatus kycStatus;
        private String city;
        private String state;
        private String country;
        private Instant createdAfter;
        private Instant createdBefore;
        private Instant lastLoginAfter;
        private Instant lastLoginBefore;
        private Boolean emailVerified;
        private Boolean kycVerified;

        // Getters and setters
        public String getName() { return name; }
        public UserSearchCriteria setName(String name) { this.name = name; return this; }

        public String getEmail() { return email; }
        public UserSearchCriteria setEmail(String email) { this.email = email; return this; }

        public UserType getUserType() { return userType; }
        public UserSearchCriteria setUserType(UserType userType) { this.userType = userType; return this; }

        public UserStatus getStatus() { return status; }
        public UserSearchCriteria setStatus(UserStatus status) { this.status = status; return this; }

        public KycLevel getKycLevel() { return kycLevel; }
        public UserSearchCriteria setKycLevel(KycLevel kycLevel) { this.kycLevel = kycLevel; return this; }

        public KycStatus getKycStatus() { return kycStatus; }
        public UserSearchCriteria setKycStatus(KycStatus kycStatus) { this.kycStatus = kycStatus; return this; }

        public String getCity() { return city; }
        public UserSearchCriteria setCity(String city) { this.city = city; return this; }

        public String getState() { return state; }
        public UserSearchCriteria setState(String state) { this.state = state; return this; }

        public String getCountry() { return country; }
        public UserSearchCriteria setCountry(String country) { this.country = country; return this; }

        public Instant getCreatedAfter() { return createdAfter; }
        public UserSearchCriteria setCreatedAfter(Instant createdAfter) { this.createdAfter = createdAfter; return this; }

        public Instant getCreatedBefore() { return createdBefore; }
        public UserSearchCriteria setCreatedBefore(Instant createdBefore) { this.createdBefore = createdBefore; return this; }

        public Instant getLastLoginAfter() { return lastLoginAfter; }
        public UserSearchCriteria setLastLoginAfter(Instant lastLoginAfter) { this.lastLoginAfter = lastLoginAfter; return this; }

        public Instant getLastLoginBefore() { return lastLoginBefore; }
        public UserSearchCriteria setLastLoginBefore(Instant lastLoginBefore) { this.lastLoginBefore = lastLoginBefore; return this; }

        public Boolean getEmailVerified() { return emailVerified; }
        public UserSearchCriteria setEmailVerified(Boolean emailVerified) { this.emailVerified = emailVerified; return this; }

        public Boolean getKycVerified() { return kycVerified; }
        public UserSearchCriteria setKycVerified(Boolean kycVerified) { this.kycVerified = kycVerified; return this; }
    }

    /**
     * User Statistics for dashboard and reporting
     */
    class UserStatistics {
        private final long totalUsers;
        private final long activeUsers;
        private final long pendingUsers;
        private final long suspendedUsers;
        private final long bannedUsers;
        private final long buyersCount;
        private final long suppliersCount;
        private final long hybridCount;
        private final long basicKycCount;
        private final long enhancedKycCount;
        private final long fullKycCount;
        private final long usersCreatedToday;
        private final long usersCreatedThisWeek;
        private final long usersCreatedThisMonth;

        public UserStatistics(long totalUsers, long activeUsers, long pendingUsers, 
                            long suspendedUsers, long bannedUsers, long buyersCount, 
                            long suppliersCount, long hybridCount, long basicKycCount, 
                            long enhancedKycCount, long fullKycCount, long usersCreatedToday, 
                            long usersCreatedThisWeek, long usersCreatedThisMonth) {
            this.totalUsers = totalUsers;
            this.activeUsers = activeUsers;
            this.pendingUsers = pendingUsers;
            this.suspendedUsers = suspendedUsers;
            this.bannedUsers = bannedUsers;
            this.buyersCount = buyersCount;
            this.suppliersCount = suppliersCount;
            this.hybridCount = hybridCount;
            this.basicKycCount = basicKycCount;
            this.enhancedKycCount = enhancedKycCount;
            this.fullKycCount = fullKycCount;
            this.usersCreatedToday = usersCreatedToday;
            this.usersCreatedThisWeek = usersCreatedThisWeek;
            this.usersCreatedThisMonth = usersCreatedThisMonth;
        }

        // Getters
        public long getTotalUsers() { return totalUsers; }
        public long getActiveUsers() { return activeUsers; }
        public long getPendingUsers() { return pendingUsers; }
        public long getSuspendedUsers() { return suspendedUsers; }
        public long getBannedUsers() { return bannedUsers; }
        public long getBuyersCount() { return buyersCount; }
        public long getSuppliersCount() { return suppliersCount; }
        public long getHybridCount() { return hybridCount; }
        public long getBasicKycCount() { return basicKycCount; }
        public long getEnhancedKycCount() { return enhancedKycCount; }
        public long getFullKycCount() { return fullKycCount; }
        public long getUsersCreatedToday() { return usersCreatedToday; }
        public long getUsersCreatedThisWeek() { return usersCreatedThisWeek; }
        public long getUsersCreatedThisMonth() { return usersCreatedThisMonth; }

        // Calculated metrics
        public double getActiveUserPercentage() {
            return totalUsers > 0 ? (activeUsers * 100.0) / totalUsers : 0.0;
        }

        public double getKycCompletionRate() {
            long kycCompleted = basicKycCount + enhancedKycCount + fullKycCount;
            return totalUsers > 0 ? (kycCompleted * 100.0) / totalUsers : 0.0;
        }

        public double getBuyerSupplierRatio() {
            return suppliersCount > 0 ? (double) buyersCount / suppliersCount : 0.0;
        }
    }
}

