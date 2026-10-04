package com.storelite.sales.messaging;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Consumed from routing key {@code stock.low}. Schema: pos-demo/api/events.md */
public record LowStock(UUID eventId, String sku, int remaining, int threshold, OffsetDateTime occurredAt) {
}
