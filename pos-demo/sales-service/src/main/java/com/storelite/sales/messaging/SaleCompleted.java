package com.storelite.sales.messaging;

import com.storelite.sales.sale.SaleRow;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/** Published with routing key {@code sale.completed}. Schema: pos-demo/api/events.md */
public record SaleCompleted(UUID eventId, UUID saleId, OffsetDateTime occurredAt, List<Line> lines) {

    public record Line(String sku, int quantity) {
    }

    public static SaleCompleted of(SaleRow sale) {
        return new SaleCompleted(UUID.randomUUID(), sale.id(), sale.createdAt(),
                sale.lines().stream().map(l -> new Line(l.sku(), l.quantity())).toList());
    }
}
