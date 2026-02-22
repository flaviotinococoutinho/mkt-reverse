package com.marketplace.user.infrastructure.persistence;

import com.marketplace.user.domain.model.User;
import com.marketplace.user.domain.repository.UserRepository;
import com.marketplace.user.domain.valueobject.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * MVP JPA adapter for the domain UserRepository.
 *
 * Important: domain repository is intentionally rich. For MVP we implement the subset we need
 * and leave the rest as UnsupportedOperationException until the use-cases demand them.
 */
@Repository
public class JpaUserRepository implements UserRepository {

    private final SpringDataUserJpaRepository jpa;

    public JpaUserRepository(SpringDataUserJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public User save(User user) {
        return jpa.save(user);
    }

    @Override
    public List<User> saveAll(List<User> users) {
        return jpa.saveAll(users);
    }

    @Override
    public Optional<User> findById(UserId userId) {
        return jpa.findById(userId);
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        return jpa.findByEmail(email);
    }

    @Override
    public Optional<User> findByDocument(Document document) {
        return jpa.findByDocument(document);
    }

    @Override
    public void delete(User user) {
        jpa.delete(user);
    }

    @Override
    public void deleteById(UserId userId) {
        jpa.deleteById(userId);
    }

    // ---------------------------
    // Not needed for MVP yet
    // ---------------------------

    @Override public List<User> findByStatus(UserStatus status) { throw new UnsupportedOperationException(); }
    @Override public List<User> findByUserType(UserType userType) { throw new UnsupportedOperationException(); }
    @Override public List<User> findByRole(UserRole role) { throw new UnsupportedOperationException(); }
    @Override public List<User> findByKycLevel(KycLevel kycLevel) { throw new UnsupportedOperationException(); }
    @Override public List<User> findByKycStatus(KycStatus kycStatus) { throw new UnsupportedOperationException(); }
    @Override public Page<User> findAll(Pageable pageable) { throw new UnsupportedOperationException(); }
    @Override public Page<User> findByStatus(UserStatus status, Pageable pageable) { throw new UnsupportedOperationException(); }
    @Override public Page<User> findByUserType(UserType userType, Pageable pageable) { throw new UnsupportedOperationException(); }
    @Override public List<User> findByCreatedAtAfter(Instant createdAt) { throw new UnsupportedOperationException(); }
    @Override public List<User> findByLastLoginAtBefore(Instant lastLoginAt) { throw new UnsupportedOperationException(); }
    @Override public List<User> findByLoginAttemptsGreaterThan(int attempts) { throw new UnsupportedOperationException(); }
    @Override public List<User> findLockedUsers() { throw new UnsupportedOperationException(); }
    @Override public List<User> findByEmailNotVerified() { throw new UnsupportedOperationException(); }
    @Override public List<User> findByKycExpired() { throw new UnsupportedOperationException(); }
    @Override public List<User> findByCity(String city) { throw new UnsupportedOperationException(); }
    @Override public List<User> findByState(String state) { throw new UnsupportedOperationException(); }
    @Override public List<User> findByCountry(String country) { throw new UnsupportedOperationException(); }
    @Override public Page<User> searchByName(String name, Pageable pageable) { throw new UnsupportedOperationException(); }
    @Override public Page<User> searchByEmailPattern(String emailPattern, Pageable pageable) { throw new UnsupportedOperationException(); }
    @Override public Page<User> search(UserSearchCriteria criteria, Pageable pageable) { throw new UnsupportedOperationException(); }
    @Override public long countByStatus(UserStatus status) { throw new UnsupportedOperationException(); }
    @Override public long countByUserType(UserType userType) { throw new UnsupportedOperationException(); }
    @Override public long countByKycLevel(KycLevel kycLevel) { throw new UnsupportedOperationException(); }
    @Override public long countCreatedToday() { throw new UnsupportedOperationException(); }
    @Override public long countActiveUsers() { throw new UnsupportedOperationException(); }
    @Override public boolean existsByEmail(Email email) { return jpa.findByEmail(email).isPresent(); }
    @Override public boolean existsByDocument(Document document) { return jpa.findByDocument(document).isPresent(); }
    @Override public boolean existsByPhoneNumber(PhoneNumber phoneNumber) { throw new UnsupportedOperationException(); }

    @Override
    public boolean existsById(UserId userId) {
        return jpa.existsById(userId);
    }

    @Override
    public UserStatistics getStatistics() {
        long total = jpa.count();
        // MVP: compute only total reliably. Others will be implemented as queries when needed.
        return new UserStatistics(
            total,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0
        );
    }
}
