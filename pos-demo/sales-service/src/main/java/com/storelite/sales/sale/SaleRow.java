package com.storelite.sales.sale;

import com.storelite.sales.api.model.Sale;
import com.storelite.sales.api.model.SaleLine;
import com.storelite.sales.api.model.TenderType;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record SaleRow(UUID id, OffsetDateTime createdAt, TenderType tenderType, long totalCents,
                      List<SaleLineRow> lines) {

    public Sale toApi() {
        return new Sale()
                .id(id)
                .createdAt(createdAt)
                .tenderType(tenderType)
                .totalCents(totalCents)
                .lines(lines.stream()
                        .map(l -> new SaleLine().sku(l.sku()).name(l.name())
                                .unitPriceCents(l.unitPriceCents()).quantity(l.quantity()))
                        .toList());
    }
}
