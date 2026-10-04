import type { components as SalesComponents } from './sales';
import type { components as InventoryComponents } from './inventory';

// Types come from the OpenAPI specs in pos-demo/api (run `npm run gen`).
export type Product = InventoryComponents['schemas']['Product'];
export type Sale = SalesComponents['schemas']['Sale'];
export type SalesSummary = SalesComponents['schemas']['SalesSummary'];
export type Alert = SalesComponents['schemas']['Alert'];
export type CreateSaleRequest = SalesComponents['schemas']['CreateSaleRequest'];
export type TenderType = SalesComponents['schemas']['TenderType'];
type Problem = SalesComponents['schemas']['Problem'];

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await fetch(path, {
    ...init,
    headers: { 'Content-Type': 'application/json', ...init?.headers },
  });
  if (!res.ok) {
    const problem: Problem | undefined = await res.json().catch(() => undefined);
    throw new Error(problem?.detail ?? problem?.title ?? `${res.status} ${res.statusText}`);
  }
  return res.json() as Promise<T>;
}

export const api = {
  products: () => request<Product[]>('/api/products'),
  restock: (sku: string, quantity: number) =>
    request<Product>(`/api/products/${encodeURIComponent(sku)}/restock`, {
      method: 'POST',
      body: JSON.stringify({ quantity }),
    }),
  createSale: (body: CreateSaleRequest) =>
    request<Sale>('/api/sales', { method: 'POST', body: JSON.stringify(body) }),
  sales: () => request<Sale[]>('/api/sales'),
  summary: () => request<SalesSummary>('/api/sales/summary'),
  alerts: () => request<Alert[]>('/api/alerts'),
};
