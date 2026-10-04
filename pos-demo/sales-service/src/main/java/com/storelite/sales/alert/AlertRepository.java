package com.storelite.sales.alert;

import com.storelite.sales.messaging.LowStock;
import java.util.List;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class AlertRepository {

    private final JdbcClient jdbc;

    public AlertRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /** Idempotent: a redelivered event (same eventId) is ignored. */
    public void record(LowStock event) {
        jdbc.sql("""
                INSERT INTO sales.alert (event_id, sku, remaining, created_at) VALUES (?, ?, ?, ?)
                ON CONFLICT (event_id) DO NOTHING""")
                .params(event.eventId(), event.sku(), event.remaining(), event.occurredAt())
                .update();
    }

    public List<AlertRow> latest(int limit) {
        return jdbc.sql("SELECT id, sku, remaining, created_at FROM sales.alert ORDER BY created_at DESC, id DESC LIMIT ?")
                .param(limit)
                .query(AlertRow.class)
                .list();
    }
}
