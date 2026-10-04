package com.storelite.inventory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.storelite.inventory.messaging.LowStock;
import com.storelite.inventory.messaging.RabbitConfig;
import com.storelite.inventory.messaging.SaleCompleted;
import com.storelite.inventory.product.ProductRepository;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AnonymousQueue;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class InventoryIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    @ServiceConnection
    static RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:3.13-management-alpine");

    @Autowired MockMvc mvc;
    @Autowired RabbitTemplate rabbitTemplate;
    @Autowired AmqpAdmin amqpAdmin;
    @Autowired TopicExchange posEvents;
    @Autowired ProductRepository products;

    @Test
    void listsSeededCatalog() throws Exception {
        mvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(12))
                .andExpect(jsonPath("$[?(@.sku == 'MILK-1L')].priceCents").value(249));
    }

    @Test
    void unknownSkuIsProblemDetail404() throws Exception {
        mvc.perform(get("/api/products/NOPE"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void restockAddsToStockAndValidatesQuantity() throws Exception {
        int before = products.findBySku("WATER-24").orElseThrow().stock();

        mvc.perform(post("/api/products/WATER-24/restock")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"quantity\": 6}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(before + 6));

        mvc.perform(post("/api/products/WATER-24/restock")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"quantity\": 0}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void saleCompletedDecrementsStockOnceAndPublishesLowStock() {
        // Listen for LowStock on a throwaway queue bound to the shared exchange.
        Queue lowStockQueue = new AnonymousQueue();
        amqpAdmin.declareQueue(lowStockQueue);
        amqpAdmin.declareBinding(bind(lowStockQueue));

        // EGGS-12 is seeded with stock 8 and threshold 5: selling 3 lands exactly on the threshold.
        SaleCompleted sale = new SaleCompleted(UUID.randomUUID(), UUID.randomUUID(), OffsetDateTime.now(),
                List.of(new SaleCompleted.Line("EGGS-12", 3)));
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.SALE_COMPLETED_KEY, sale);

        await().atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> assertThat(products.findBySku("EGGS-12").orElseThrow().stock()).isEqualTo(5));

        LowStock lowStock = rabbitTemplate.receiveAndConvert(lowStockQueue.getName(), 10_000,
                new ParameterizedTypeReference<LowStock>() {});
        assertThat(lowStock).isNotNull();
        assertThat(lowStock.sku()).isEqualTo("EGGS-12");
        assertThat(lowStock.remaining()).isEqualTo(5);
        assertThat(lowStock.threshold()).isEqualTo(5);

        // Redelivery of the same event must not decrement again.
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.SALE_COMPLETED_KEY, sale);
        await().during(Duration.ofSeconds(2)).atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> assertThat(products.findBySku("EGGS-12").orElseThrow().stock()).isEqualTo(5));
    }

    private Binding bind(Queue queue) {
        return BindingBuilder.bind(queue).to(posEvents).with(RabbitConfig.STOCK_LOW_KEY);
    }
}
