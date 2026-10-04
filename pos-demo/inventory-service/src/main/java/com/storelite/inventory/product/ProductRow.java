package com.storelite.inventory.product;

import com.storelite.inventory.api.model.Product;

/** A row of inventory.product. Kept separate from the generated API model. */
public record ProductRow(String sku, String name, long priceCents, int stock, int lowStockThreshold) {

    public boolean isLow() {
        return stock <= lowStockThreshold;
    }

    public Product toApi() {
        return new Product()
                .sku(sku)
                .name(name)
                .priceCents(priceCents)
                .stock(stock)
                .lowStockThreshold(lowStockThreshold)
                .lowStock(isLow());
    }
}
