import type { Product } from './api/client';

export type BasketLine = { sku: string; name: string; priceCents: number; quantity: number };

export type BasketAction =
  | { type: 'add'; product: Pick<Product, 'sku' | 'name' | 'priceCents'> }
  | { type: 'decrement'; sku: string }
  | { type: 'clear' };

export function basketReducer(lines: BasketLine[], action: BasketAction): BasketLine[] {
  switch (action.type) {
    case 'add': {
      const { sku, name, priceCents } = action.product;
      return lines.some((l) => l.sku === sku)
        ? lines.map((l) => (l.sku === sku ? { ...l, quantity: l.quantity + 1 } : l))
        : [...lines, { sku, name, priceCents, quantity: 1 }];
    }
    case 'decrement':
      return lines
        .map((l) => (l.sku === action.sku ? { ...l, quantity: l.quantity - 1 } : l))
        .filter((l) => l.quantity > 0);
    case 'clear':
      return [];
  }
}

export function basketTotal(lines: BasketLine[]): number {
  return lines.reduce((sum, l) => sum + l.priceCents * l.quantity, 0);
}
