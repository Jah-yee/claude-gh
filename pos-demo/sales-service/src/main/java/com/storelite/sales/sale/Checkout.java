package com.storelite.sales.sale;

import com.storelite.sales.api.model.SaleLineRequest;
import com.storelite.sales.inventory.CatalogProduct;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Pure pricing logic: turns requested lines into priced sale lines. */
final class Checkout {

    private Checkout() {
    }

    /** Merges repeated SKUs (keeping first-seen order) and snapshots name and price from the catalog. */
    static List<SaleLineRow> price(List<SaleLineRequest> requested, Map<String, CatalogProduct> catalog) {
        Map<String, Integer> quantities = new LinkedHashMap<>();
        for (SaleLineRequest line : requested) {
            quantities.merge(line.getSku(), line.getQuantity(), Integer::sum);
        }

        List<String> unknown = quantities.keySet().stream().filter(sku -> !catalog.containsKey(sku)).toList();
        if (!unknown.isEmpty()) {
            throw new UnknownSkuException(unknown);
        }

        return quantities.entrySet().stream()
                .map(e -> {
                    CatalogProduct p = catalog.get(e.getKey());
                    return new SaleLineRow(p.sku(), p.name(), p.priceCents(), e.getValue());
                })
                .toList();
    }

    static long total(List<SaleLineRow> lines) {
        return lines.stream().mapToLong(SaleLineRow::subtotalCents).sum();
    }
}
