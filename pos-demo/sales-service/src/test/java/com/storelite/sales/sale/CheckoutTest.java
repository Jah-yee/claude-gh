package com.storelite.sales.sale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.storelite.sales.api.model.SaleLineRequest;
import com.storelite.sales.inventory.CatalogProduct;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CheckoutTest {

    private final Map<String, CatalogProduct> catalog = Map.of(
            "MILK-1L", new CatalogProduct("MILK-1L", "Whole Milk 1L", 249),
            "EGGS-12", new CatalogProduct("EGGS-12", "Large Eggs (12)", 449));

    @Test
    void pricesLinesFromCatalogAndTotalsInCents() {
        List<SaleLineRow> lines = Checkout.price(List.of(line("MILK-1L", 2), line("EGGS-12", 1)), catalog);

        assertThat(lines).containsExactly(
                new SaleLineRow("MILK-1L", "Whole Milk 1L", 249, 2),
                new SaleLineRow("EGGS-12", "Large Eggs (12)", 449, 1));
        assertThat(Checkout.total(lines)).isEqualTo(947);
    }

    @Test
    void mergesRepeatedSkus() {
        List<SaleLineRow> lines = Checkout.price(List.of(line("MILK-1L", 1), line("EGGS-12", 1), line("MILK-1L", 2)),
                catalog);

        assertThat(lines).extracting(SaleLineRow::sku, SaleLineRow::quantity)
                .containsExactly(org.assertj.core.groups.Tuple.tuple("MILK-1L", 3),
                        org.assertj.core.groups.Tuple.tuple("EGGS-12", 1));
    }

    @Test
    void rejectsUnknownSkus() {
        assertThatThrownBy(() -> Checkout.price(List.of(line("MILK-1L", 1), line("NOPE", 1)), catalog))
                .isInstanceOf(UnknownSkuException.class)
                .hasMessageContaining("NOPE");
    }

    private static SaleLineRequest line(String sku, int quantity) {
        return new SaleLineRequest().sku(sku).quantity(quantity);
    }
}
