package com.marketplace.shared.domain.event;

import lombok.Builder;
import lombok.Value;

import java.util.Collections;
import java.util.Map;

/**
 * Metadata associated with domain events.
 * Contains additional context information about the event.
 */
@Value
@Builder
public class EventMetadata {
    
    Map<String, Object> properties;
    String userId;
    String sessionId;
    String ipAddress;
    String userAgent;
    String source;
    
    public static EventMetadata empty() {
        return EventMetadata.builder()
            .properties(Collections.emptyMap())
            .build();
    }
    
    public static EventMetadata of(Map<String, Object> properties) {
        return EventMetadata.builder()
            .properties(properties != null ? properties : Collections.emptyMap())
            .build();
    }
    
    public Object getProperty(String key) {
        return properties.get(key);
    }
    
    public String getStringProperty(String key) {
        Object value = getProperty(key);
        return value != null ? value.toString() : null;
    }
    
    public boolean hasProperty(String key) {
        return properties.containsKey(key);
    }
}

