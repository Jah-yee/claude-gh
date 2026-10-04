package com.storelite.inventory.messaging;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/** Consumed from routing key {@code sale.completed}. Schema: pos-demo/api/events.md */
public record SaleCompleted(UUID eventId, UUID saleId, OffsetDateTime occurredAt, List<Line> lines) {

    public record Line(String sku, int quantity) {
    }
}
