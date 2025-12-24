package com.mrh.buscharter.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Base class untuk semua domain events.
 */
public abstract class DomainEvent {
    
    private final String eventId;
    private final LocalDateTime occurredAt;
    private final Long tenantId;

    protected DomainEvent(Long tenantId) {
        this.eventId = UUID.randomUUID().toString();
        this.occurredAt = LocalDateTime.now();
        this.tenantId = tenantId;
    }

    public String getEventId() {
        return eventId;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public Long getTenantId() {
        return tenantId;
    }

    /**
     * Nama event untuk logging dan routing.
     */
    public abstract String getEventName();
}
