package com.marketplace.user.domain.repository;

import com.marketplace.user.domain.model.User;
import com.marketplace.user.domain.valueobject.*;
import org.junit.jupiter.api.Test;
import com.marketplace.user.infrastructure.persistence.JpaUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaUserRepository.class)
@Transactional
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User newUser() {
        return User.create(
            Email.of("testuser@example.com"),
            Password.of("Qz9$rtY1!"),
            PersonalInfo.of("John", "Doe"),
            Document.cpf("12345678909"),
            UserType.BUYER
        );
    }

    @Test
    void shouldSaveAndRetrieveUserByEmail() {
        User user = newUser();
        userRepository.save(user);

        Optional<User> retrieved = userRepository.findByEmail(user.getEmail());
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get()).isEqualTo(user);
    }

    @Test
    void shouldReturnEmptyWhenEmailDoesNotExist() {
        Optional<User> retrieved = userRepository.findByEmail(Email.of("nonexistent@example.com"));
        assertThat(retrieved).isEmpty();
    }

    @Test
    void shouldDeleteUser() {
        User user = newUser();
        userRepository.save(user);

        userRepository.delete(user);

        Optional<User> retrieved = userRepository.findByEmail(user.getEmail());
        assertThat(retrieved).isEmpty();
    }
}