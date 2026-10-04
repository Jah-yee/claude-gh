package com.storelite.inventory.messaging;

import com.storelite.inventory.stock.StockService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class SaleCompletedListener {

    private final StockService stock;

    public SaleCompletedListener(StockService stock) {
        this.stock = stock;
    }

    @RabbitListener(queues = RabbitConfig.SALE_COMPLETED_QUEUE)
    public void onSaleCompleted(SaleCompleted event) {
        stock.applySale(event);
    }
}
