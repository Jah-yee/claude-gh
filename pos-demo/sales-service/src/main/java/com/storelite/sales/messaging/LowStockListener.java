package com.storelite.sales.messaging;

import com.storelite.sales.alert.AlertRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class LowStockListener {

    private final AlertRepository alerts;

    public LowStockListener(AlertRepository alerts) {
        this.alerts = alerts;
    }

    @RabbitListener(queues = RabbitConfig.STOCK_LOW_QUEUE)
    public void onLowStock(LowStock event) {
        alerts.record(event);
    }
}
