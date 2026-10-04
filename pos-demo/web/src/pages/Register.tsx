import { useReducer, useState } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { api, type Sale, type TenderType } from '../api/client';
import { basketReducer } from '../basket';
import { Basket } from '../components/Basket';
import { Receipt } from '../components/Receipt';
import { formatCents } from '../money';

export function Register() {
  const queryClient = useQueryClient();
  const products = useQuery({ queryKey: ['products'], queryFn: api.products });
  const [lines, dispatch] = useReducer(basketReducer, []);
  const [receipt, setReceipt] = useState<Sale | null>(null);

  const checkout = useMutation({
    mutationFn: (tenderType: TenderType) =>
      api.createSale({ tenderType, lines: lines.map(({ sku, quantity }) => ({ sku, quantity })) }),
    onSuccess: (sale) => {
      setReceipt(sale);
      dispatch({ type: 'clear' });
      // Stock is decremented asynchronously by inventory-service; refresh soon after.
      setTimeout(() => queryClient.invalidateQueries(), 750);
    },
  });

  return (
    <div className="register">
      <section className="panel">
        <h2>Products</h2>
        {products.isPending && <p className="muted">Loading…</p>}
        {products.isError && <p className="error">{products.error.message}</p>}
        <div className="product-grid">
          {products.data?.map((p) => (
            <button
              key={p.sku}
              type="button"
              className="product"
              onClick={() => dispatch({ type: 'add', product: p })}
            >
              <span className="name">{p.name}</span>
              <span className="price">{formatCents(p.priceCents)}</span>
              <span className={p.lowStock ? 'stock low' : 'stock'}>{p.stock} in stock</span>
            </button>
          ))}
        </div>
      </section>
      <Basket
        lines={lines}
        busy={checkout.isPending}
        error={checkout.error?.message}
        onAdd={(l) => dispatch({ type: 'add', product: l })}
        onDecrement={(sku) => dispatch({ type: 'decrement', sku })}
        onCheckout={(tender) => checkout.mutate(tender)}
      />
      {receipt && <Receipt sale={receipt} onClose={() => setReceipt(null)} />}
    </div>
  );
}
