package com.storelite.sales.sale;

import com.storelite.sales.api.SalesApi;
import com.storelite.sales.api.model.CreateSaleRequest;
import com.storelite.sales.api.model.Sale;
import com.storelite.sales.api.model.SalesSummary;
import com.storelite.sales.inventory.InventoryUnavailableException;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SaleController implements SalesApi {

    private final SaleService sales;

    public SaleController(SaleService sales) {
        this.sales = sales;
    }

    @Override
    public ResponseEntity<Sale> createSale(CreateSaleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sales.checkout(request).toApi());
    }

    @Override
    public ResponseEntity<List<Sale>> listSales(LocalDate date) {
        LocalDate day = date != null ? date : sales.today();
        return ResponseEntity.ok(sales.recent(day).stream().map(SaleRow::toApi).toList());
    }

    @Override
    public ResponseEntity<SalesSummary> getSalesSummary(LocalDate date) {
        LocalDate day = date != null ? date : sales.today();
        SaleRepository.Summary summary = sales.summary(day);
        return ResponseEntity.ok(new SalesSummary().date(day).count(summary.count()).totalCents(summary.totalCents()));
    }

    @ExceptionHandler(UnknownSkuException.class)
    ProblemDetail handleUnknownSku(UnknownSkuException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage());
    }

    @ExceptionHandler(InventoryUnavailableException.class)
    ProblemDetail handleInventoryDown(InventoryUnavailableException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
    }
}
