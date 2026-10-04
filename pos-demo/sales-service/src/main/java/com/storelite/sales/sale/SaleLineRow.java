package com.storelite.sales.sale;

public record SaleLineRow(String sku, String name, long unitPriceCents, int quantity) {

    public long subtotalCents() {
        return unitPriceCents * quantity;
    }
}
