package com.marketplace.payment.domain.model;

import com.marketplace.payment.domain.event.PaymentConnectorRegisteredEvent;
import com.marketplace.payment.domain.event.PaymentConnectorSecretRotatedEvent;
import com.marketplace.payment.domain.event.PaymentConnectorStatusChangedEvent;
import com.marketplace.payment.domain.valueobject.IntegrationStatus;
import com.marketplace.payment.domain.valueobject.PaymentConnectorId;
import com.marketplace.payment.domain.valueobject.PaymentProvider;
import com.marketplace.payment.domain.valueobject.SecretReference;
import com.marketplace.shared.domain.model.AggregateRoot;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Payment connector aggregate storing gateway credentials and capabilities.
 */
@Entity
@Table(name = "PAY_CONNECTORS", indexes = {
    @Index(name = "idx_pay_tenant", columnList = "tenant_id"),
    @Index(name = "idx_pay_provider", columnList = "provider")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentConnector extends AggregateRoot<PaymentConnectorId> {

    @EmbeddedId
    private PaymentConnectorId id;

    @Column(name = "tenant_id", nullable = false, length = 36)
    private String tenantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 30)
    private PaymentProvider provider;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private IntegrationStatus status;

    @Column(name = "display_name", nullable = false, length = 120)
    private String displayName;

    @Embedded
    private SecretReference apiSecret;

    @Column(name = "sandbox", nullable = false)
    private boolean sandbox;

    @Column(name = "webhook_url", length = 200)
    private String webhookUrl;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "PAY_CONNECTOR_CURRENCIES", joinColumns = @JoinColumn(name = "connector_id"))
    @Column(name = "currency", length = 3)
    private Set<String> supportedCurrencies = new HashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "PAY_CONNECTOR_COUNTRIES", joinColumns = @JoinColumn(name = "connector_id"))
    @Column(name = "country", length = 2)
    private Set<String> supportedCountries = new HashSet<>();

    @Column(name = "last_heartbeat_at")
    private Instant lastHeartbeatAt;

    @Column(name = "last_sync_at")
    private Instant lastSyncAt;

    @Column(name = "error_message", length = 400)
    private String errorMessage;

    private PaymentConnector(
        PaymentConnectorId id,
        String tenantId,
        PaymentProvider provider,
        String displayName,
        SecretReference apiSecret,
        boolean sandbox,
        Set<String> currencies,
        Set<String> countries,
        String webhookUrl
    ) {
        this.id = id;
        this.tenantId = tenantId;
        this.provider = provider;
        this.displayName = displayName;
        this.apiSecret = apiSecret;
        this.sandbox = sandbox;
        if (currencies != null) {
            this.supportedCurrencies.addAll(currencies);
        }
        if (countries != null) {
            this.supportedCountries.addAll(countries);
        }
        this.webhookUrl = webhookUrl;
        this.status = IntegrationStatus.PENDING;
    }

    public static PaymentConnector register(
        String tenantId,
        PaymentProvider provider,
        String displayName,
        SecretReference apiSecret,
        boolean sandbox,
        Set<String> currencies,
        Set<String> countries,
        String webhookUrl
    ) {
        Objects.requireNonNull(tenantId, "tenantId is required");
        Objects.requireNonNull(provider, "provider is required");
        Objects.requireNonNull(displayName, "displayName is required");
        Objects.requireNonNull(apiSecret, "apiSecret is required");

        PaymentConnector connector = new PaymentConnector(
            PaymentConnectorId.generate(),
            tenantId.trim(),
            provider,
            displayName.trim(),
            apiSecret,
            sandbox,
            currencies,
            countries,
            webhookUrl != null ? webhookUrl.trim() : null
        );
        connector.addDomainEvent(new PaymentConnectorRegisteredEvent(
            connector.getId().asString(),
            connector.getProvider(),
            connector.isSandbox()
        ));
        connector.markAsCreated();
        return connector;
    }

    public void activate() {
        transitionStatus(IntegrationStatus.ACTIVE, null);
    }

    public void disable(String reason) {
        transitionStatus(IntegrationStatus.DISABLED, reason);
    }

    public void markError(String message) {
        this.errorMessage = message;
        transitionStatus(IntegrationStatus.ERROR, message);
    }

    public void rotateSecret(String newEncryptedValue) {
        this.apiSecret = apiSecret.rotate(newEncryptedValue);
        addDomainEvent(new PaymentConnectorSecretRotatedEvent(id.asString(), Instant.now()));
        markAsUpdated();
    }

    public void recordHeartbeat() {
        this.lastHeartbeatAt = Instant.now();
        markAsUpdated();
    }

    public void recordSync() {
        this.lastSyncAt = Instant.now();
        markAsUpdated();
    }

    private void transitionStatus(IntegrationStatus target, String reason) {
        if (this.status == target) {
            return;
        }
        this.status = target;
        addDomainEvent(new PaymentConnectorStatusChangedEvent(id.asString(), target, Instant.now(), reason));
        markAsUpdated();
    }

    @Override
    public void validate() {
        if (id == null) {
            throw new IllegalStateException("PaymentConnector id cannot be null");
        }
        if (tenantId == null || tenantId.trim().isEmpty()) {
            throw new IllegalStateException("tenantId cannot be null");
        }
        if (provider == null) {
            throw new IllegalStateException("provider cannot be null");
        }
        if (displayName == null || displayName.trim().isEmpty()) {
            throw new IllegalStateException("displayName cannot be null");
        }
        if (apiSecret == null) {
            throw new IllegalStateException("apiSecret cannot be null");
        }
        if (status == null) {
            throw new IllegalStateException("status cannot be null");
        }
    }
}
