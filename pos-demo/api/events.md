# Events

All events go through the RabbitMQ **topic exchange `pos.events`** as JSON messages (`content_type: application/json`).
Every event carries a unique `eventId` (UUID). Consumers use it to stay idempotent, because RabbitMQ delivers at least once.

| Routing key | Producer | Consumer queue | Purpose |
|---|---|---|---|
| `sale.completed` | sales-service | `inventory.sale-completed` | Decrement stock for each sold line |
| `stock.low` | inventory-service | `sales.stock-low` | Record a low-stock alert for the dashboard |

Each consumer queue has a dead-letter queue named `<queue>.dlq`. Messages that still fail after retries are routed there.

## SaleCompleted (`sale.completed`)

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "title": "SaleCompleted",
  "type": "object",
  "required": ["eventId", "saleId", "occurredAt", "lines"],
  "properties": {
    "eventId":    { "type": "string", "format": "uuid" },
    "saleId":     { "type": "string", "format": "uuid" },
    "occurredAt": { "type": "string", "format": "date-time" },
    "lines": {
      "type": "array",
      "minItems": 1,
      "items": {
        "type": "object",
        "required": ["sku", "quantity"],
        "properties": {
          "sku":      { "type": "string" },
          "quantity": { "type": "integer", "minimum": 1 }
        }
      }
    }
  }
}
```

Example:

```json
{
  "eventId": "6f1c0e8e-2b0a-4f7e-9b39-2f5a1d0c9a11",
  "saleId": "a3d2c1b0-1111-4c2d-8e9f-000000000001",
  "occurredAt": "2026-10-04T15:04:05Z",
  "lines": [{ "sku": "MILK-1L", "quantity": 2 }]
}
```

## LowStock (`stock.low`)

This event is published when a decrement takes a product's stock **from above its threshold to at or below it**. Further sales while the product is already low do not publish it again.

```json
{
  "$schema": "https://json-schema.org/draft/2020-12/schema",
  "title": "LowStock",
  "type": "object",
  "required": ["eventId", "sku", "remaining", "threshold", "occurredAt"],
  "properties": {
    "eventId":    { "type": "string", "format": "uuid" },
    "sku":        { "type": "string" },
    "remaining":  { "type": "integer" },
    "threshold":  { "type": "integer" },
    "occurredAt": { "type": "string", "format": "date-time" }
  }
}
```

Example:

```json
{
  "eventId": "0b7e7f6a-5d4c-4b3a-9a8b-7c6d5e4f3a2b",
  "sku": "EGGS-12",
  "remaining": 3,
  "threshold": 5,
  "occurredAt": "2026-10-04T15:04:06Z"
}
```
