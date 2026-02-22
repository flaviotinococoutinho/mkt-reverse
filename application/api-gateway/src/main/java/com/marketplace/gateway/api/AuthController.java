package com.marketplace.gateway.api;

import com.marketplace.user.domain.model.User;
import com.marketplace.user.domain.repository.UserRepository;
import com.marketplace.user.domain.valueobject.Document;
import com.marketplace.user.domain.valueobject.Password;
import com.marketplace.user.domain.valueobject.PersonalInfo;
import com.marketplace.user.domain.valueobject.UserType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> register(@Valid @RequestBody RegisterRequest req) {
        com.marketplace.user.domain.valueobject.Email email = com.marketplace.user.domain.valueobject.Email.of(req.email);
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email já cadastrado");
        }

        Document document = Document.of(req.documentNumber, req.documentType);
        if (userRepository.existsByDocument(document)) {
            throw new IllegalArgumentException("Documento já cadastrado");
        }

        PersonalInfo personalInfo = PersonalInfo.of(req.firstName, req.lastName, req.displayName);
        Password password = Password.of(req.password);

        User user = User.create(email, password, personalInfo, document, req.userType);

        // MVP: sem verificação de email/KYC — ativa direto para destravar o fluxo.
        user.activate();
        userRepository.save(user);

        return authResponse(user);
    }

    @PostMapping("/login")
    public Map<String, Object> login(@Valid @RequestBody LoginRequest req) {
        com.marketplace.user.domain.valueobject.Email email = com.marketplace.user.domain.valueobject.Email.of(req.email);
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Credenciais inválidas"));

        if (!user.getPassword().matches(req.password)) {
            user.recordFailedLogin();
            userRepository.save(user);
            throw new IllegalArgumentException("Credenciais inválidas");
        }

        if (user.isLocked()) {
            throw new IllegalArgumentException("Conta temporariamente bloqueada");
        }

        user.recordLogin();
        userRepository.save(user);

        return authResponse(user);
    }

    private Map<String, Object> authResponse(User user) {
        // MVP: token opaco (a API ainda está aberta no SecurityConfig)
        String token = "mvp-" + UUID.randomUUID();

        String role = user.isSupplier() ? "supplier" : "buyer";
        String name = user.getPersonalInfo().getDisplayName();

        return Map.of(
            "token", token,
            "user", Map.of(
                "id", user.getId().getValue().toString(),
                "name", name,
                "email", user.getEmail().getValue(),
                "role", role,
                "tenantId", "tenant-default"
            )
        );
    }

    public record RegisterRequest(
        @NotBlank @Email String email,
        @NotBlank String password,
        @NotBlank String firstName,
        @NotBlank String lastName,
        String displayName,
        @NotBlank String documentNumber,
        @NotNull Document.DocumentType documentType,
        @NotNull UserType userType
    ) {
        public RegisterRequest {
            if (displayName == null || displayName.isBlank()) {
                displayName = (firstName + " " + lastName).trim();
            }
        }
    }

    public record LoginRequest(
        @NotBlank @Email String email,
        @NotBlank String password
    ) {}
}
