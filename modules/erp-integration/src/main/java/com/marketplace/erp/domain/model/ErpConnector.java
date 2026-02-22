package com.marketplace.erp.domain.model;

import com.marketplace.erp.domain.event.ErpConnectorCredentialRotatedEvent;
import com.marketplace.erp.domain.event.ErpConnectorRegisteredEvent;
import com.marketplace.erp.domain.event.ErpSyncCompletedEvent;
import com.marketplace.erp.domain.valueobject.ApiCredential;
import com.marketplace.erp.domain.valueobject.ErpConnectorId;
import com.marketplace.erp.domain.valueobject.ErpSystem;
import com.marketplace.erp.domain.valueobject.IntegrationSchedule;
import com.marketplace.erp.domain.valueobject.SyncStatus;
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
 * ERP connector aggregate representing downstream system integrations.
 */
@Entity
@Table(name = "ERP_CONNECTORS", indexes = {
    @Index(name = "idx_erp_tenant", columnList = "tenant_id"),
    @Index(name = "idx_erp_system", columnList = "system")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ErpConnector extends AggregateRoot<ErpConnectorId> {

    @EmbeddedId
    private ErpConnectorId id;

    @Column(name = "tenant_id", nullable = false, length = 36)
    private String tenantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "system", nullable = false, length = 40)
    private ErpSystem system;

    @Column(name = "endpoint", nullable = false, length = 200)
    private String endpoint;

    @Embedded
    private ApiCredential credential;

    @Embedded
    private IntegrationSchedule schedule;

    @Enumerated(EnumType.STRING)
    @Column(name = "sync_status", nullable = false, length = 20)
    private SyncStatus lastSyncStatus;

    @Column(name = "last_sync_at")
    private Instant lastSyncAt;

    @Column(name = "last_sync_error", length = 400)
    private String lastSyncError;

    @Column(name = "bidirectional", nullable = false)
    private boolean bidirectional;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "ERP_CONNECTOR_MODULES", joinColumns = @JoinColumn(name = "connector_id"))
    @Column(name = "module", length = 60)
    private Set<String> enabledModules = new HashSet<>();

    private ErpConnector(
        ErpConnectorId id,
        String tenantId,
        ErpSystem system,
        String endpoint,
        ApiCredential credential,
        IntegrationSchedule schedule,
        boolean bidirectional,
        Set<String> modules
    ) {
        this.id = id;
        this.tenantId = tenantId;
        this.system = system;
        this.endpoint = endpoint;
        this.credential = credential;
        this.schedule = schedule;
        this.bidirectional = bidirectional;
        if (modules != null) {
            this.enabledModules.addAll(modules);
        }
        this.lastSyncStatus = SyncStatus.IDLE;
    }

    public static ErpConnector register(
        String tenantId,
        ErpSystem system,
        String endpoint,
        ApiCredential credential,
        IntegrationSchedule schedule,
        boolean bidirectional,
        Set<String> modules
    ) {
        Objects.requireNonNull(tenantId, "tenantId is required");
        Objects.requireNonNull(system, "system is required");
        Objects.requireNonNull(endpoint, "endpoint is required");
        Objects.requireNonNull(credential, "credential is required");
        Objects.requireNonNull(schedule, "schedule is required");

        ErpConnector connector = new ErpConnector(
            ErpConnectorId.generate(),
            tenantId.trim(),
            system,
            endpoint.trim(),
            credential,
            schedule,
            bidirectional,
            modules
        );
        connector.addDomainEvent(new ErpConnectorRegisteredEvent(
            connector.getId().asString(),
            connector.getSystem(),
            connector.isBidirectional()
        ));
        connector.markAsCreated();
        return connector;
    }

    public void markSyncSuccess() {
        this.lastSyncStatus = SyncStatus.SUCCESS;
        this.lastSyncAt = Instant.now();
        this.lastSyncError = null;
        this.schedule = schedule.executedNow();
        addDomainEvent(new ErpSyncCompletedEvent(id.asString(), true, lastSyncAt, null));
        markAsUpdated();
    }

    public void markSyncFailure(String error) {
        this.lastSyncStatus = SyncStatus.FAILED;
        this.lastSyncAt = Instant.now();
        this.lastSyncError = error;
        this.schedule = schedule.executedNow();
        addDomainEvent(new ErpSyncCompletedEvent(id.asString(), false, lastSyncAt, error));
        markAsUpdated();
    }

    public void rotateCredential(String newSecret) {
        this.credential = credential.rotate(newSecret);
        addDomainEvent(new ErpConnectorCredentialRotatedEvent(id.asString(), Instant.now()));
        markAsUpdated();
    }

    public void enableModule(String module) {
        enabledModules.add(module);
        markAsUpdated();
    }

    public boolean isSyncDue(Instant reference) {
        return schedule.isDue(reference);
    }

    @Override
    public void validate() {
        if (id == null) {
            throw new IllegalStateException("ErpConnector id cannot be null");
        }
        if (tenantId == null || tenantId.trim().isEmpty()) {
            throw new IllegalStateException("tenantId cannot be null");
        }
        if (system == null) {
            throw new IllegalStateException("system cannot be null");
        }
        if (endpoint == null || endpoint.trim().isEmpty()) {
            throw new IllegalStateException("endpoint cannot be null");
        }
        if (credential == null) {
            throw new IllegalStateException("credential cannot be null");
        }
        if (schedule == null) {
            throw new IllegalStateException("schedule cannot be null");
        }
    }
}
