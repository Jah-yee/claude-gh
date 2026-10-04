package com.storelite.inventory.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/** Sends LowStock to RabbitMQ only once the stock update has committed. */
@Component
public class LowStockPublisher {

    private final RabbitTemplate rabbit;

    public LowStockPublisher(RabbitTemplate rabbit) {
        this.rabbit = rabbit;
    }

    @TransactionalEventListener
    public void publish(LowStock event) {
        rabbit.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.STOCK_LOW_KEY, event);
    }
}
