package com.marketplace.user.domain.model;

import com.marketplace.user.domain.valueobject.*;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

class UserTest {

    private User newUser() {
        return User.create(
            Email.of("buyer@example.com"),
            Password.of("S3cur3@Token99"),
            PersonalInfo.of("Joao", "Silva"),
            Document.cpf("39053344705"),
            UserType.BUYER
        );
    }

    @Test
    void shouldCreateUserWithPendingVerification() {
        User user = newUser();

        assertThat(user.getId()).isNotNull();
        assertThat(user.getStatus()).isEqualTo(UserStatus.PENDING_VERIFICATION);
        assertThat(user.getRoles()).contains(UserRole.BUYER);
        assertThat(user.getEmailVerification().isVerified()).isFalse();
        assertThat(user.getKycVerification().isVerified()).isFalse();
    }

    @Test
    void shouldActivateAfterEmailAndKycVerification() {
        User user = newUser();

        user.verifyEmail();
        assertThat(user.getStatus()).isEqualTo(UserStatus.PENDING_VERIFICATION);

        user.completeKyc(KycLevel.ENHANCED);

        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.isEmailVerified()).isTrue();
        assertThat(user.isKycVerified()).isTrue();
    }

    @Test
    void shouldLockAfterFiveFailedLogins() {
        User user = newUser();
        user.activate();

        for (int i = 0; i < 5; i++) {
            user.recordFailedLogin();
        }

        assertThat(user.getLoginAttempts()).isEqualTo(5);
        assertThat(user.isLocked()).isTrue();
    }

    @Test
    void shouldNotAllowRemovingLastRole() {
        User user = newUser();
        user.activate();

        assertThatThrownBy(() -> user.removeRole(UserRole.BUYER))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("at least one role");
    }

    @Test
    void shouldEmitEventsOnStatusChange() {
        User user = newUser();
        user.activate();

        assertThat(user.getDomainEvents())
            .anyMatch(e -> e.getEventType().equals("UserStatusChangedEvent"));

        user.clearDomainEvents();
        user.suspend("manual-check");

        assertThat(user.getDomainEvents())
            .anyMatch(e -> e.getEventType().equals("UserStatusChangedEvent"));
    }

    @Test
    void shouldChangePasswordWhenCurrentMatches() {
        User user = newUser();
        user.activate();

        user.changePassword("S3cur3@Token99", "An0ther@StrongPwd");

        assertThat(user.getPassword().matchesPlainText("An0ther@StrongPwd")).isTrue();
    }

    @Test
    void shouldFailChangePasswordWhenCurrentDoesNotMatch() {
        User user = newUser();
        user.activate();

        assertThatThrownBy(() -> user.changePassword("Wr0ng@Passwd", "NewStr0ng@Pass"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Current password is incorrect");
    }
}
