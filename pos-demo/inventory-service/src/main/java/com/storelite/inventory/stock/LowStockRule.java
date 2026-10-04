package com.storelite.inventory.stock;

final class LowStockRule {

    private LowStockRule() {
    }

    /**
     * True only when a decrement moves stock from above the threshold to at or below it,
     * so a product that is already low does not raise a new alert on every sale.
     */
    static boolean crossedThreshold(int before, int after, int threshold) {
        return before > threshold && after <= threshold;
    }
}
