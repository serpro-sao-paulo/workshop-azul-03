package com.datacorp.app.shared.events;

import java.time.Instant;
import java.util.UUID;

/**
 * Base class for all SIFAP domain events.
 *
 * <p>Events are published via {@link org.springframework.context.ApplicationEventPublisher}
 * (wired through Spring Modulith's durable event registry, ADR-004).
 * Consumers use {@link org.springframework.modulith.events.ApplicationModuleListener}
 * to process events asynchronously in a separate transaction.
 *
 * <p>source_legacy: Audit trail requirements REQ-036, REQ-037.
 */
public abstract class DomainEvent {

    private final UUID eventId;
    private final Instant occurredOn;

    protected DomainEvent() {
        this.eventId = UUID.randomUUID();
        this.occurredOn = Instant.now();
    }

    public UUID eventId() { return eventId; }
    public Instant occurredOn() { return occurredOn; }
}
