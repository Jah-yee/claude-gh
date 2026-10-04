package com.storelite.sales.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Sends SaleCompleted to RabbitMQ only once the sale has committed. If the broker is down at that
 * moment the event is lost; a transactional outbox would close that gap.
 */
@Component
public class SaleCompletedPublisher {

    private final RabbitTemplate rabbit;

    public SaleCompletedPublisher(RabbitTemplate rabbit) {
        this.rabbit = rabbit;
    }

    @TransactionalEventListener
    public void publish(SaleCompleted event) {
        rabbit.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.SALE_COMPLETED_KEY, event);
    }
}
