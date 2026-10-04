package com.storelite.sales.sale;

import com.storelite.sales.api.model.CreateSaleRequest;
import com.storelite.sales.inventory.InventoryClient;
import com.storelite.sales.messaging.SaleCompleted;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SaleService {

    private static final int RECENT_LIMIT = 50;

    private final InventoryClient inventory;
    private final SaleRepository sales;
    private final ApplicationEventPublisher events;
    private final ZoneId zone = ZoneId.systemDefault();

    public SaleService(InventoryClient inventory, SaleRepository sales, ApplicationEventPublisher events) {
        this.inventory = inventory;
        this.sales = sales;
        this.events = events;
    }

    /** Prices the basket, stores the sale, and publishes SaleCompleted once the transaction commits. */
    @Transactional
    public SaleRow checkout(CreateSaleRequest request) {
        List<SaleLineRow> lines = Checkout.price(request.getLines(), inventory.catalog());
        SaleRow sale = new SaleRow(UUID.randomUUID(), OffsetDateTime.now().truncatedTo(ChronoUnit.MICROS),
                request.getTenderType(), Checkout.total(lines), lines);
        sales.insert(sale);
        events.publishEvent(SaleCompleted.of(sale));
        return sale;
    }

    public List<SaleRow> recent(LocalDate day) {
        return sales.findBetween(startOf(day), startOf(day.plusDays(1)), RECENT_LIMIT);
    }

    public SaleRepository.Summary summary(LocalDate day) {
        return sales.summarize(startOf(day), startOf(day.plusDays(1)));
    }

    public LocalDate today() {
        return LocalDate.now(zone);
    }

    private OffsetDateTime startOf(LocalDate day) {
        return day.atStartOfDay(zone).toOffsetDateTime();
    }
}
