package com.storelite.sales.messaging;

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
    public static final String STOCK_LOW_QUEUE = "sales.stock-low";

    @Bean
    TopicExchange posEvents() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    Queue stockLowQueue() {
        return QueueBuilder.durable(STOCK_LOW_QUEUE)
                .deadLetterExchange("")
                .deadLetterRoutingKey(STOCK_LOW_QUEUE + ".dlq")
                .build();
    }

    @Bean
    Queue stockLowDlq() {
        return QueueBuilder.durable(STOCK_LOW_QUEUE + ".dlq").build();
    }

    @Bean
    Binding stockLowBinding(Queue stockLowQueue, TopicExchange posEvents) {
        return BindingBuilder.bind(stockLowQueue).to(posEvents).with(STOCK_LOW_KEY);
    }

    /** JSON on the wire; the target type comes from the listener signature, not the producer's class name. */
    @Bean
    MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
        converter.setAlwaysConvertToInferredType(true);
        return converter;
    }
}
