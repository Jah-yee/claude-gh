package com.storelite.inventory.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "pos.events";
    public static final String SALE_COMPLETED_KEY = "sale.completed";
    public static final String STOCK_LOW_KEY = "stock.low";
    public static final String SALE_COMPLETED_QUEUE = "inventory.sale-completed";

    @Bean
    TopicExchange posEvents() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    Queue saleCompletedQueue() {
        return QueueBuilder.durable(SALE_COMPLETED_QUEUE)
                .deadLetterExchange("")
                .deadLetterRoutingKey(SALE_COMPLETED_QUEUE + ".dlq")
                .build();
    }

    @Bean
    Queue saleCompletedDlq() {
        return QueueBuilder.durable(SALE_COMPLETED_QUEUE + ".dlq").build();
    }

    @Bean
    Binding saleCompletedBinding(Queue saleCompletedQueue, TopicExchange posEvents) {
        return BindingBuilder.bind(saleCompletedQueue).to(posEvents).with(SALE_COMPLETED_KEY);
    }

    /** JSON on the wire; the target type comes from the listener signature, not the producer's class name. */
    @Bean
    MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
        converter.setAlwaysConvertToInferredType(true);
        return converter;
    }
}
