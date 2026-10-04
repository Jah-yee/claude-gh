package com.storelite.sales.inventory;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

/** Synchronous call to inventory-service, which owns the catalog and prices. */
@Component
public class InventoryClient {

    private final RestClient http;

    public InventoryClient(RestClient.Builder builder, @Value("${storelite.inventory-url}") String baseUrl) {
        this.http = builder.baseUrl(baseUrl).build();
    }

    /** Current catalog keyed by SKU. */
    public Map<String, CatalogProduct> catalog() {
        try {
            List<CatalogProduct> products = http.get().uri("/api/products")
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            return products.stream().collect(Collectors.toMap(CatalogProduct::sku, Function.identity()));
        } catch (ResourceAccessException e) {
            throw new InventoryUnavailableException(e);
        }
    }
}
