import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import VaultCategoryTile from './VaultCategoryTile';

function renderTile(props = {}) {
  const defaultProps = {
    icon: <span>icon</span>,
    label: 'Logos',
    orgCount: 12,
  };
  return render(
    <VaultCategoryTile {...defaultProps} {...props} />
  );
}

describe('VaultCategoryTile', () => {
  it('renders the label prop as visible text', () => {
    renderTile({ label: 'Fonts' });

    expect(screen.getByText('Fonts')).toBeInTheDocument();
  });

  it('renders the orgCount value', () => {
    renderTile({ orgCount: 7 });

    expect(screen.getByText('7 org assets')).toBeInTheDocument();
  });

  it('renders projectCount when provided', () => {
    renderTile({ projectCount: 3 });

    expect(screen.getByText('3 project assets')).toBeInTheDocument();
  });

  it('does not render project count text when projectCount prop is omitted', () => {
    renderTile();

    expect(screen.queryByText(/project assets/)).not.toBeInTheDocument();
  });

  it('has correct aria-label including category name and counts', () => {
    const { unmount } = renderTile({ label: 'Logos', orgCount: 12 });

    expect(screen.getByLabelText('Logos: 12 org assets')).toBeInTheDocument();
    unmount();

    renderTile({ label: 'Logos', orgCount: 12, projectCount: 3 });

    expect(screen.getByLabelText('Logos: 12 org assets, 3 project assets')).toBeInTheDocument();
  });

  // --- Gap test: Card-like styling matches DashboardTile ---
  it('applies card-like styling with border, borderRadius, and boxShadow matching DashboardTile', () => {
    renderTile();

    const tile = screen.getByLabelText('Logos: 12 org assets');
    expect(tile.style.border).toContain('1px solid');
    expect(tile.style.borderRadius).toBe('2px');
    expect(tile.style.boxShadow).toBeTruthy();
  });

  // --- Gap test: Icon has aria-hidden="true" ---
  it('renders icon wrapper with aria-hidden="true"', () => {
    renderTile({ icon: <span data-testid="test-icon">icon</span> });

    const iconElement = screen.getByTestId('test-icon');
    const iconWrapper = iconElement.parentElement;
    expect(iconWrapper).toHaveAttribute('aria-hidden', 'true');
  });
});
