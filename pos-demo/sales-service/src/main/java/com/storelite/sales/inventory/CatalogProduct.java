package com.storelite.sales.inventory;

/** The subset of inventory-service's Product that checkout needs. */
public record CatalogProduct(String sku, String name, long priceCents) {
}
