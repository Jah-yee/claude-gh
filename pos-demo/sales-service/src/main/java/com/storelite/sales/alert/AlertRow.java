package com.storelite.sales.alert;

import com.storelite.sales.api.model.Alert;
import java.time.OffsetDateTime;

public record AlertRow(long id, String sku, int remaining, OffsetDateTime createdAt) {

    public Alert toApi() {
        return new Alert().id(id).sku(sku).remaining(remaining).createdAt(createdAt);
    }
}
