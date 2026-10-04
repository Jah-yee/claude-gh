package com.storelite.sales.alert;

import com.storelite.sales.api.AlertsApi;
import com.storelite.sales.api.model.Alert;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AlertController implements AlertsApi {

    private static final int LATEST_LIMIT = 20;

    private final AlertRepository alerts;

    public AlertController(AlertRepository alerts) {
        this.alerts = alerts;
    }

    @Override
    public ResponseEntity<List<Alert>> listAlerts() {
        return ResponseEntity.ok(alerts.latest(LATEST_LIMIT).stream().map(AlertRow::toApi).toList());
    }
}
