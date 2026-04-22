package com.marketplace.user.domain.valueobject;

import lombok.Getter;

import java.util.Set;

/**
 * User Role Enumeration
 * 
 * Represents the different roles a user can have in the system.
 * Roles define permissions and access levels for various system features.
 * 
 * Design principles:
 * - Role-based access control (RBAC)
 * - Hierarchical permissions
 * - Clear separation of concerns
 * - Extensible for future roles
 */
@Getter
public enum UserRole {
    
    /**
     * Basic User - Default role for all users
     * Basic system access and profile management
     */
    USER("User", "Basic user with standard system access", 1, Set.of(
        "profile:read", "profile:update", "notifications:read", "support:create"
    )),
    
    /**
     * Buyer - Can create sourcing events and manage purchases
     * Access to buyer-specific features like RFQs and reverse auctions
     */
    BUYER("Buyer", "Can create sourcing events and manage purchases", 2, Set.of(
        "sourcing:create", "sourcing:read", "sourcing:update", "sourcing:delete",
        "auction:create", "auction:read", "rfq:create", "rfq:read", "rfq:update",
        "contract:read", "contract:sign", "supplier:search", "supplier:contact"
    )),
    
    /**
     * Supplier - Can respond to sourcing events and submit proposals
     * Access to supplier-specific features like bidding and proposal management
     */
    SUPPLIER("Supplier", "Can respond to sourcing events and submit proposals", 2, Set.of(
        "sourcing:read", "sourcing:respond", "auction:participate", "auction:bid",
        "rfq:respond", "proposal:create", "proposal:read", "proposal:update",
        "contract:read", "contract:sign", "buyer:contact"
    )),
    
    /**
     * Moderator - Can moderate content and resolve disputes
     * Limited administrative access for content moderation
     */
    MODERATOR("Moderator", "Can moderate content and resolve basic disputes", 3, Set.of(
        "content:moderate", "dispute:read", "dispute:resolve", "user:read",
        "report:read", "report:resolve", "message:moderate"
    )),
    
    /**
     * Admin - Full system administration access
     * Can manage users, system settings, and all platform features
     */
    ADMIN("Administrator", "Full system administration access", 4, Set.of(
        "user:create", "user:read", "user:update", "user:delete", "user:suspend",
        "system:configure", "system:monitor", "analytics:read", "audit:read",
        "role:assign", "role:revoke", "dispute:resolve", "content:moderate",
        "notification:send", "maintenance:perform"
    )),
    
    /**
     * Super Admin - Highest level access including system-critical operations
     * Can perform system-critical operations and manage other admins
     */
    SUPER_ADMIN("Super Administrator", "Highest level system access", 5, Set.of(
        "system:critical", "admin:manage", "security:configure", "backup:manage",
        "deployment:manage", "database:access", "logs:access", "metrics:access"
    ));

    private final String displayName;
    private final String description;
    private final int level;
    private final Set<String> permissions;

    UserRole(String displayName, String description, int level, Set<String> permissions) {
        this.displayName = displayName;
        this.description = description;
        this.level = level;
        this.permissions = permissions;
    }

    /**
     * Checks if this role has a specific permission
     */
    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }

    /**
     * Checks if this role has any of the specified permissions
     */
    public boolean hasAnyPermission(String... permissions) {
        for (String permission : permissions) {
            if (hasPermission(permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if this role has all of the specified permissions
     */
    public boolean hasAllPermissions(String... permissions) {
        for (String permission : permissions) {
            if (!hasPermission(permission)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if this role is higher level than another role
     */
    public boolean isHigherThan(UserRole other) {
        return this.level > other.level;
    }

    /**
     * Checks if this role is lower level than another role
     */
    public boolean isLowerThan(UserRole other) {
        return this.level < other.level;
    }

    /**
     * Checks if this role is at the same level as another role
     */
    public boolean isSameLevelAs(UserRole other) {
        return this.level == other.level;
    }

    /**
     * Checks if this is an administrative role
     */
    public boolean isAdministrative() {
        return this == ADMIN || this == SUPER_ADMIN || this == MODERATOR;
    }

    /**
     * Checks if this is a business role (buyer/supplier)
     */
    public boolean isBusiness() {
        return this == BUYER || this == SUPPLIER;
    }

    /**
     * Checks if this role can manage users
     */
    public boolean canManageUsers() {
        return hasPermission("user:update") || hasPermission("user:delete");
    }

    /**
     * Checks if this role can access system administration
     */
    public boolean canAccessSystemAdmin() {
        return hasPermission("system:configure") || hasPermission("system:monitor");
    }

    /**
     * Checks if this role can moderate content
     */
    public boolean canModerateContent() {
        return hasPermission("content:moderate");
    }

    /**
     * Checks if this role can resolve disputes
     */
    public boolean canResolveDisputes() {
        return hasPermission("dispute:resolve");
    }

    /**
     * Gets the default roles for a user type
     */
    public static Set<UserRole> getDefaultRolesForUserType(UserType userType) {
        return switch (userType) {
            case BUYER -> Set.of(USER, BUYER);
            case SUPPLIER -> Set.of(USER, SUPPLIER);
            case HYBRID -> Set.of(USER, BUYER, SUPPLIER);
            case ADMIN -> Set.of(USER, ADMIN);
        };
    }

    /**
     * Gets roles that can be assigned by this role
     */
    public Set<UserRole> getAssignableRoles() {
        return switch (this) {
            case USER -> Set.of();
            case BUYER, SUPPLIER -> Set.of();
            case MODERATOR -> Set.of(USER);
            case ADMIN -> Set.of(USER, BUYER, SUPPLIER, MODERATOR);
            case SUPER_ADMIN -> Set.of(USER, BUYER, SUPPLIER, MODERATOR, ADMIN);
        };
    }

    /**
     * Gets the minimum role required to assign this role
     */
    public UserRole getMinimumRoleToAssign() {
        return switch (this) {
            case USER -> MODERATOR;
            case BUYER, SUPPLIER -> ADMIN;
            case MODERATOR -> ADMIN;
            case ADMIN -> SUPER_ADMIN;
            case SUPER_ADMIN -> SUPER_ADMIN;
        };
    }

    /**
     * Gets the CSS class for UI styling
     */
    public String getCssClass() {
        return switch (this) {
            case USER -> "role-user";
            case BUYER -> "role-buyer";
            case SUPPLIER -> "role-supplier";
            case MODERATOR -> "role-moderator";
            case ADMIN -> "role-admin";
            case SUPER_ADMIN -> "role-super-admin";
        };
    }

    /**
     * Gets the color code for UI display
     */
    public String getColorCode() {
        return switch (this) {
            case USER -> "#6c757d";
            case BUYER -> "#007bff";
            case SUPPLIER -> "#28a745";
            case MODERATOR -> "#ffc107";
            case ADMIN -> "#dc3545";
            case SUPER_ADMIN -> "#6f42c1";
        };
    }

    /**
     * Creates a UserRole from string (case-insensitive)
     */
    public static UserRole fromString(String role) {
        if (role == null || role.trim().isEmpty()) {
            throw new IllegalArgumentException("User role cannot be null or empty");
        }

        try {
            return UserRole.valueOf(role.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid user role: " + role);
        }
    }

    /**
     * Checks if the given string is a valid user role
     */
    public static boolean isValid(String role) {
        try {
            fromString(role);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Gets all business roles (non-administrative)
     */
    public static Set<UserRole> getBusinessRoles() {
        return Set.of(USER, BUYER, SUPPLIER);
    }

    /**
     * Gets all administrative roles
     */
    public static Set<UserRole> getAdministrativeRoles() {
        return Set.of(MODERATOR, ADMIN, SUPER_ADMIN);
    }

    /**
     * Gets roles by minimum level
     */
    public static Set<UserRole> getRolesByMinimumLevel(int minimumLevel) {
        return Set.of(values()).stream()
            .filter(role -> role.level >= minimumLevel)
            .collect(java.util.stream.Collectors.toSet());
    }

    @Override
    public String toString() {
        return displayName;
    }
}

