import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { Basket } from './Basket';

const noop = () => {};

describe('<Basket>', () => {
  it('disables checkout when empty', () => {
    render(<Basket lines={[]} onAdd={noop} onDecrement={noop} onCheckout={noop} />);
    expect(screen.getByRole('button', { name: 'Pay cash' })).toBeDisabled();
    expect(screen.getByRole('button', { name: 'Pay card' })).toBeDisabled();
  });

  it('shows the total and checks out with the chosen tender', async () => {
    const onCheckout = vi.fn();
    render(
      <Basket
        lines={[{ sku: 'MILK-1L', name: 'Whole Milk 1L', priceCents: 249, quantity: 2 }]}
        onAdd={noop}
        onDecrement={noop}
        onCheckout={onCheckout}
      />,
    );

    expect(screen.getByTestId('basket-total')).toHaveTextContent('$4.98');
    await userEvent.click(screen.getByRole('button', { name: 'Pay card' }));
    expect(onCheckout).toHaveBeenCalledWith('CARD');
  });

  it('shows checkout errors', () => {
    render(
      <Basket
        lines={[{ sku: 'X', name: 'X', priceCents: 1, quantity: 1 }]}
        error="Unknown SKU(s): X"
        onAdd={noop}
        onDecrement={noop}
        onCheckout={noop}
      />,
    );
    expect(screen.getByRole('alert')).toHaveTextContent('Unknown SKU(s): X');
  });
});
