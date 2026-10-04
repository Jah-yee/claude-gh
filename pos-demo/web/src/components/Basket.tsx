import { basketTotal, type BasketLine } from '../basket';
import type { TenderType } from '../api/client';
import { formatCents } from '../money';

type Props = {
  lines: BasketLine[];
  busy?: boolean;
  error?: string;
  onAdd: (line: BasketLine) => void;
  onDecrement: (sku: string) => void;
  onCheckout: (tender: TenderType) => void;
};

export function Basket({ lines, busy = false, error, onAdd, onDecrement, onCheckout }: Props) {
  const empty = lines.length === 0;
  return (
    <aside className="panel basket" aria-label="Basket">
      <h2>Basket</h2>
      {empty ? (
        <p className="muted">Tap a product to add it.</p>
      ) : (
        <ul className="basket-lines">
          {lines.map((l) => (
            <li key={l.sku}>
              <span className="grow">{l.name}</span>
              <span className="qty">
                <button type="button" aria-label={`Remove one ${l.name}`} onClick={() => onDecrement(l.sku)}>
                  −
                </button>
                <span aria-label={`${l.name} quantity`}>{l.quantity}</span>
                <button type="button" aria-label={`Add one ${l.name}`} onClick={() => onAdd(l)}>
                  +
                </button>
              </span>
              <span className="num">{formatCents(l.priceCents * l.quantity)}</span>
            </li>
          ))}
        </ul>
      )}
      <div className="basket-total">
        <span>Total</span>
        <strong data-testid="basket-total">{formatCents(basketTotal(lines))}</strong>
      </div>
      {error && <p className="error" role="alert">{error}</p>}
      <div className="row">
        <button type="button" className="primary" disabled={empty || busy} onClick={() => onCheckout('CASH')}>
          Pay cash
        </button>
        <button type="button" className="primary" disabled={empty || busy} onClick={() => onCheckout('CARD')}>
          Pay card
        </button>
      </div>
    </aside>
  );
}
