package com.storelite.inventory.stock;

import com.storelite.inventory.messaging.LowStock;
import com.storelite.inventory.messaging.SaleCompleted;
import com.storelite.inventory.product.ProductRepository;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockService {

    private static final Logger log = LoggerFactory.getLogger(StockService.class);

    private final ProductRepository products;
    private final JdbcClient jdbc;
    private final ApplicationEventPublisher events;

    public StockService(ProductRepository products, JdbcClient jdbc, ApplicationEventPublisher events) {
        this.products = products;
        this.jdbc = jdbc;
        this.events = events;
    }

    /**
     * Applies a sale to stock levels. Idempotent: the event id is recorded in the same transaction,
     * so a redelivered message is a no-op. LowStock events are published only after commit.
     * Stock may go negative: the goods were physically sold, so the count was simply wrong.
     */
    @Transactional
    public void applySale(SaleCompleted sale) {
        int inserted = jdbc.sql("INSERT INTO inventory.processed_events (event_id) VALUES (?) ON CONFLICT DO NOTHING")
                .param(sale.eventId())
                .update();
        if (inserted == 0) {
            log.info("Skipping duplicate SaleCompleted event {}", sale.eventId());
            return;
        }

        for (SaleCompleted.Line line : sale.lines()) {
            products.adjustStock(line.sku(), -line.quantity()).ifPresentOrElse(
                    after -> {
                        int before = after.stock() + line.quantity();
                        if (LowStockRule.crossedThreshold(before, after.stock(), after.lowStockThreshold())) {
                            events.publishEvent(new LowStock(UUID.randomUUID(), after.sku(), after.stock(),
                                    after.lowStockThreshold(), OffsetDateTime.now()));
                        }
                    },
                    () -> log.warn("Sale {} references unknown SKU {}; ignoring line", sale.saleId(), line.sku()));
        }
    }
}
