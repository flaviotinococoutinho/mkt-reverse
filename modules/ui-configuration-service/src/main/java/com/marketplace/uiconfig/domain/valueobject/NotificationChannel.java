package com.marketplace.uiconfig.domain.valueobject;

/**
 * Notification delivery channel.
 */
public enum NotificationChannel {
    
    /**
     * Email notification.
     * Requires subject and body templates.
     */
    EMAIL,
    
    /**
     * SMS notification.
     * Only body template (limited characters).
     */
    SMS,
    
    /**
     * Push notification (mobile/web).
     * Requires title and body.
     */
    PUSH,
    
    /**
     * In-app notification.
     * Displayed within the application.
     */
    IN_APP,
    
    /**
     * WhatsApp notification.
     * Requires WhatsApp Business API integration.
     */
    WHATSAPP;
    
    /**
     * Checks if this channel requires a subject/title.
     * 
     * @return true if subject is required
     */
    public boolean requiresSubject() {
        return this == EMAIL || this == PUSH;
    }
    
    /**
     * Checks if this channel has character limits.
     * 
     * @return true if character limited
     */
    public boolean hasCharacterLimit() {
        return this == SMS || this == PUSH;
    }
    
    /**
     * Gets the maximum character limit for this channel.
     * 
     * @return maximum characters, or -1 if no limit
     */
    public int maxCharacters() {
        return switch (this) {
            case SMS -> 160;
            case PUSH -> 200;
            default -> -1;
        };
    }
    
    /**
     * Checks if this channel supports HTML content.
     * 
     * @return true if HTML is supported
     */
    public boolean supportsHtml() {
        return this == EMAIL || this == IN_APP;
    }
    
    /**
     * Checks if this channel supports rich media (images, videos).
     * 
     * @return true if rich media is supported
     */
    public boolean supportsRichMedia() {
        return this == EMAIL || this == IN_APP || this == WHATSAPP;
    }
}
