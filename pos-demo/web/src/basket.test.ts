import { basketReducer, basketTotal, type BasketLine } from './basket';

const milk = { sku: 'MILK-1L', name: 'Whole Milk 1L', priceCents: 249 };
const eggs = { sku: 'EGGS-12', name: 'Large Eggs (12)', priceCents: 449 };

describe('basketReducer', () => {
  it('adds new products and increments existing ones', () => {
    let lines: BasketLine[] = [];
    lines = basketReducer(lines, { type: 'add', product: milk });
    lines = basketReducer(lines, { type: 'add', product: eggs });
    lines = basketReducer(lines, { type: 'add', product: milk });
    expect(lines).toEqual([
      { ...milk, quantity: 2 },
      { ...eggs, quantity: 1 },
    ]);
  });

  it('removes a line when its quantity reaches zero', () => {
    const lines = basketReducer([{ ...milk, quantity: 1 }], { type: 'decrement', sku: 'MILK-1L' });
    expect(lines).toEqual([]);
  });

  it('totals in cents', () => {
    expect(basketTotal([{ ...milk, quantity: 2 }, { ...eggs, quantity: 1 }])).toBe(947);
  });
});
