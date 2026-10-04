package com.storelite.inventory.product;

import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class ProductRepository {

    private static final String COLUMNS = "sku, name, price_cents, stock, low_stock_threshold";

    private final JdbcClient jdbc;

    public ProductRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public List<ProductRow> findAll() {
        return jdbc.sql("SELECT " + COLUMNS + " FROM inventory.product ORDER BY name")
                .query(ProductRow.class)
                .list();
    }

    public Optional<ProductRow> findBySku(String sku) {
        return jdbc.sql("SELECT " + COLUMNS + " FROM inventory.product WHERE sku = ?")
                .param(sku)
                .query(ProductRow.class)
                .optional();
    }

    /** Atomically adds {@code delta} (may be negative) to stock and returns the updated row. */
    public Optional<ProductRow> adjustStock(String sku, int delta) {
        return jdbc.sql("UPDATE inventory.product SET stock = stock + ? WHERE sku = ? RETURNING " + COLUMNS)
                .params(delta, sku)
                .query(ProductRow.class)
                .optional();
    }
}
