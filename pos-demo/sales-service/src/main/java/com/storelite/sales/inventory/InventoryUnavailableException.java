package com.storelite.sales.inventory;

public class InventoryUnavailableException extends RuntimeException {
    public InventoryUnavailableException(Throwable cause) {
        super("Inventory service is unavailable", cause);
    }
}
