package com.marketplace.user.domain.valueobject;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EmailTest {

    @Test
    void ofNormalizesAndExtractsParts() {
        Email email = Email.of("  John.Doe@Example.com ");

        assertThat(email.toString()).isEqualTo("john.doe@example.com");
        assertThat(email.getLocalPart()).isEqualTo("john.doe");
        assertThat(email.getDomain()).isEqualTo("example.com");
        assertThat(email.belongsToDomain("example.com")).isTrue();
        assertThat(email.isCorporateEmail()).isTrue();
    }

    @Test
    void ofRejectsInvalidEmail() {
        assertThatThrownBy(() -> Email.of("invalid-email"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid email format");
    }

    @Test
    void getMaskedReturnsMaskedValue() {
        Email email = Email.of("ab@example.com");

        assertThat(email.getMasked()).isEqualTo("a***@example.com");
    }
}
