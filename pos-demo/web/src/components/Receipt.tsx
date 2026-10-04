import type { Sale } from '../api/client';
import { formatCents } from '../money';

export function Receipt({ sale, onClose }: { sale: Sale; onClose: () => void }) {
  return (
    <div className="backdrop" onClick={onClose}>
      <div className="panel receipt" role="dialog" aria-label="Receipt" onClick={(e) => e.stopPropagation()}>
        <h2>Receipt</h2>
        <p className="muted mono">
          #{sale.id.slice(0, 8)} · {new Date(sale.createdAt).toLocaleString()} · {sale.tenderType}
        </p>
        <table>
          <tbody>
            {sale.lines.map((l) => (
              <tr key={l.sku}>
                <td>{l.quantity} × {l.name}</td>
                <td className="num">{formatCents(l.unitPriceCents * l.quantity)}</td>
              </tr>
            ))}
          </tbody>
          <tfoot>
            <tr>
              <th>Total</th>
              <th className="num">{formatCents(sale.totalCents)}</th>
            </tr>
          </tfoot>
        </table>
        <button type="button" className="primary" onClick={onClose} autoFocus>
          New sale
        </button>
      </div>
    </div>
  );
}
