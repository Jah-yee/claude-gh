package com.storelite.inventory.messaging;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Published with routing key {@code stock.low}. Schema: pos-demo/api/events.md */
public record LowStock(UUID eventId, String sku, int remaining, int threshold, OffsetDateTime occurredAt) {
}
