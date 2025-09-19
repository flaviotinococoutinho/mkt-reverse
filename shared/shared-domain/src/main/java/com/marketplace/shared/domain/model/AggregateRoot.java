package com.marketplace.shared.domain.model;

import com.marketplace.shared.domain.event.DomainEvent;
import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Base class for all Aggregate Roots in the domain model.
 * Implements DDD patterns including domain events and auditing.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
public abstract class AggregateRoot<ID> {
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    @Version
    @Column(name = "version", nullable = false)
    private Long version;
    
    @Transient
    private final List<DomainEvent> domainEvents = new ArrayList<>();
    
    /**
     * Gets the unique identifier of this aggregate root.
     */
    public abstract ID getId();
    
    /**
     * Adds a domain event to be published.
     */
    protected void addDomainEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }
    
    /**
     * Gets all domain events that have been raised by this aggregate.
     */
    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }
    
    /**
     * Clears all domain events. Should be called after events are published.
     */
    public void clearDomainEvents() {
        this.domainEvents.clear();
    }
    
    /**
     * Marks the aggregate as created by adding a domain event.
     */
    protected void markAsCreated() {
        // Subclasses should override to add specific creation events
    }
    
    /**
     * Marks the aggregate as updated by adding a domain event.
     */
    protected void markAsUpdated() {
        // Subclasses should override to add specific update events
    }
    
    /**
     * Validates the aggregate's business rules.
     * Should be called before persisting.
     */
    public abstract void validate();
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        AggregateRoot<?> that = (AggregateRoot<?>) o;
        return Objects.equals(getId(), that.getId());
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
    
    @Override
    public String toString() {
        return String.format("%s{id=%s, version=%d}", 
            getClass().getSimpleName(), getId(), version);
    }
}

