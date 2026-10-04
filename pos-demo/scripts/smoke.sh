#!/usr/bin/env bash
# End-to-end check against a running stack (make up): sell a product down to its low-stock
# threshold and wait for the alert to travel inventory-service -> RabbitMQ -> sales-service.
set -euo pipefail

BASE=${BASE:-http://localhost:5173}
SKU=${SKU:-EGGS-12}

product() { curl -fsS "$BASE/api/products/$SKU"; }

stock=$(product | jq .stock)
threshold=$(product | jq .lowStockThreshold)
if (( stock <= threshold )); then
  echo "Restocking $SKU so the sale crosses the threshold"
  curl -fsS -X POST -H 'Content-Type: application/json' -d "{\"quantity\": $((threshold - stock + 3))}" \
    "$BASE/api/products/$SKU/restock" >/dev/null
  stock=$(product | jq .stock)
fi
qty=$((stock - threshold))
alerts_before=$(curl -fsS "$BASE/api/alerts" | jq "[.[] | select(.sku == \"$SKU\")] | length")

echo "Selling $qty x $SKU (stock $stock, threshold $threshold)"
sale=$(curl -fsS -X POST -H 'Content-Type: application/json' \
  -d "{\"tenderType\": \"CASH\", \"lines\": [{\"sku\": \"$SKU\", \"quantity\": $qty}]}" "$BASE/api/sales")
echo "Sale $(jq -r .id <<<"$sale") total $(jq .totalCents <<<"$sale") cents"

for _ in $(seq 1 20); do
  now_stock=$(product | jq .stock)
  alerts=$(curl -fsS "$BASE/api/alerts" | jq "[.[] | select(.sku == \"$SKU\")] | length")
  if (( now_stock == threshold && alerts > alerts_before )); then
    echo "OK: stock is $now_stock and a low-stock alert was recorded"
    exit 0
  fi
  sleep 0.5
done
echo "FAIL: stock=$now_stock alerts=$alerts (before: $alerts_before)" >&2
exit 1
