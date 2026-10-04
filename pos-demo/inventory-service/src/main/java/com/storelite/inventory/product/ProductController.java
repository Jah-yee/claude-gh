package com.storelite.inventory.product;

import com.storelite.inventory.api.ProductsApi;
import com.storelite.inventory.api.model.Product;
import com.storelite.inventory.api.model.RestockRequest;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductController implements ProductsApi {

    private final ProductRepository products;

    public ProductController(ProductRepository products) {
        this.products = products;
    }

    @Override
    public ResponseEntity<List<Product>> listProducts() {
        return ResponseEntity.ok(products.findAll().stream().map(ProductRow::toApi).toList());
    }

    @Override
    public ResponseEntity<Product> getProduct(String sku) {
        return products.findBySku(sku)
                .map(row -> ResponseEntity.ok(row.toApi()))
                .orElseThrow(() -> new ProductNotFoundException(sku));
    }

    @Override
    public ResponseEntity<Product> restockProduct(String sku, RestockRequest request) {
        return products.adjustStock(sku, request.getQuantity())
                .map(row -> ResponseEntity.ok(row.toApi()))
                .orElseThrow(() -> new ProductNotFoundException(sku));
    }

    @ExceptionHandler(ProductNotFoundException.class)
    ProblemDetail handleNotFound(ProductNotFoundException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }
}
