package com.marketplace.user.infrastructure.persistence;

import com.marketplace.user.domain.model.User;
import com.marketplace.user.domain.valueobject.Document;
import com.marketplace.user.domain.valueobject.Email;
import com.marketplace.user.domain.valueobject.UserId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface SpringDataUserJpaRepository extends JpaRepository<User, UserId> {

    Optional<User> findByEmail(Email email);

    Optional<User> findByDocument(Document document);
}
