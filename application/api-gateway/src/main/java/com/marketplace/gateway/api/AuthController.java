package com.marketplace.gateway.api;

import com.marketplace.gateway.config.JwtTokenProvider;
import com.marketplace.shared.events.DomainEventPublisher;
import com.marketplace.user.domain.model.User;
import com.marketplace.user.domain.repository.UserRepository;
import com.marketplace.user.domain.valueobject.Document;
import com.marketplace.user.domain.valueobject.Password;
import com.marketplace.user.domain.valueobject.PersonalInfo;
import com.marketplace.user.domain.valueobject.UserType;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final DomainEventPublisher domainEventPublisher;

    // MVP: Token blacklist (em produção, usar Redis ou banco)
    private final java.util.Set<String> invalidatedTokens = ConcurrentHashMap.newKeySet();

    public AuthController(
            UserRepository userRepository,
            JwtTokenProvider jwtTokenProvider,
            DomainEventPublisher domainEventPublisher) {
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.domainEventPublisher = domainEventPublisher;
    }

    private User saveAndPublish(User user) {
        User saved = userRepository.save(user);
        domainEventPublisher.publishAll(user.getDomainEvents());
        user.clearDomainEvents();
        return saved;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
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

        user.activate();
        saveAndPublish(user);

        return generateAuthResponse(user);
    }

    @PostMapping("/login")
    @Transactional
    public Map<String, Object> login(@Valid @RequestBody LoginRequest req) {
        com.marketplace.user.domain.valueobject.Email email = com.marketplace.user.domain.valueobject.Email.of(req.email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Credenciais inválidas"));

        // Lockout ANTES do match de senha (senão não barra brute-force) e
        // status ANTES de emitir token (BANNED/SUSPENDED não logam).
        if (user.isLocked()) {
            throw new IllegalArgumentException("Conta temporariamente bloqueada");
        }
        if (!user.isActive()) {
            throw new IllegalArgumentException("Conta não está ativa");
        }

        if (!user.getPassword().matches(req.password)) {
            user.recordFailedLogin();
            saveAndPublish(user);
            throw new IllegalArgumentException("Credenciais inválidas");
        }

        user.recordLogin();
        saveAndPublish(user);

        return generateAuthResponse(user);
    }

    @PostMapping("/refresh")
    public Map<String, Object> refresh(@Valid @RequestBody RefreshRequest req) {
        Map<String, Object> validation = jwtTokenProvider.validateToken(req.refreshToken());
        if (!(Boolean) validation.getOrDefault("valid", false)) {
            throw new IllegalArgumentException("Refresh token inválido ou expirado");
        }
        // Um access token NÃO serve como refresh token.
        if (!jwtTokenProvider.isRefreshToken(req.refreshToken())) {
            throw new IllegalArgumentException("Token informado não é um refresh token");
        }

        String userId = jwtTokenProvider.getUserIdFromToken(req.refreshToken());

        java.util.UUID uuid = java.util.UUID.fromString(userId);
        User user = userRepository.findById(com.marketplace.user.domain.valueobject.UserId.of(uuid))
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));

        if (user.isLocked() || !user.isActive()) {
            throw new IllegalArgumentException("Conta não pode renovar sessão");
        }

        return generateAuthResponse(user);
    }

    @PostMapping("/logout")
    public Map<String, Object> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            invalidatedTokens.add(token);
        }
        return Map.of("message", "Logout realizado com sucesso");
    }

    @GetMapping("/me")
    public Map<String, Object> me(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        
        Map<String, Object> validation = jwtTokenProvider.validateToken(token);
        if (!(Boolean) validation.getOrDefault("valid", false)) {
            throw new IllegalArgumentException("Token inválido");
        }

        String userId = (String) validation.get("userId");
        String email = (String) validation.get("email");
        String role = (String) validation.get("role");
        String tenantId = (String) validation.get("tenantId");

        return Map.of(
                "id", userId,
                "email", email,
                "role", role,
                "tenantId", tenantId
        );
    }

    private Map<String, Object> generateAuthResponse(User user) {
        String userId = user.getId().getValue().toString();
        String email = user.getEmail().getValue();
        String role = user.isSupplier() ? "supplier" : "buyer";
        String tenantId = "tenant-default";

        String accessToken = jwtTokenProvider.generateAccessToken(userId, email, role, tenantId);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userId);

        String name = user.getPersonalInfo().getDisplayName();

        return Map.of(
                "accessToken", accessToken,
                "refreshToken", refreshToken,
                "user", Map.of(
                        "id", userId,
                        "name", name,
                        "email", email,
                        "role", role,
                        "tenantId", tenantId
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

    public record RefreshRequest(
            @NotBlank String refreshToken
    ) {}
}
