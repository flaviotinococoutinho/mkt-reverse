package com.marketplace.gateway.security;

import com.marketplace.sourcing.domain.repository.SourcingEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * Security service for Sourcing operations.
 * 
 * Implements Object Calisthenics:
 * - Small focused methods
 * - Immutable validation results
 * - No magic numbers
 */
@Service("sourcingSecurityService")
@RequiredArgsConstructor
public class SourcingSecurityService {

    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_BUYER = "ROLE_BUYER";
    private static final String ROLE_SUPPLIER = "ROLE_SUPPLIER";

    private final SourcingEventRepository eventRepository;

    /**
     * Validates if the current user is the owner of the event.
     * Used for accepting proposals - only event owner can accept.
     */
    public boolean isEventOwner(String eventId, String userId) {
        return eventRepository.findById(eventId)
                .map(event -> event.getBuyerId().equals(userId))
                .orElse(false);
    }

    /**
     * Validates if user can access the event.
     * - Admins can access everything
     * - Buyers can access their own events
     * - Suppliers can access public events
     */
    public boolean canAccessEvent(String eventId, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        var authorities = auth.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .toList();

        // Admin has full access
        if (authorities.contains(ROLE_ADMIN)) {
            return true;
        }

        // Find event and check access
        var event = eventRepository.findById(eventId);
        if (event.isEmpty()) {
            return false;
        }

        var buyerId = event.get().getBuyerId();
        var userId = auth.getName();

        // Buyer can access own events
        if (authorities.contains(ROLE_BUYER) && buyerId.equals(userId)) {
            return true;
        }

        // Suppliers can access public events
        var isPublic = "PUBLIC".equals(event.get().getVisibility());
        return authorities.contains(ROLE_SUPPLIER) && isPublic;
    }

    /**
     * Validates tenant access - user can only access their tenant's data.
     */
    public boolean canAccessTenant(String tenantId, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        var authorities = auth.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .toList();

        // Admin can access any tenant
        if (authorities.contains(ROLE_ADMIN)) {
            return true;
        }

        // Get user's tenant from principal
        var principal = auth.getPrincipal();
        if (principal instanceof SourcingPrincipal sourcingPrincipal) {
            return sourcingPrincipal.getTenantId().equals(tenantId);
        }

        return false;
    }

    /**
     * Custom Spring Security Principal for marketplace users.
     */
    public record SourcingPrincipal(
            String userId,
            String tenantId,
            String organizationId,
            String role
    ) {}
}