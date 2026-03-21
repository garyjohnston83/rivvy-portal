import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { describe, it, expect } from 'vitest';
import DashboardTile from './DashboardTile';

function renderTile(props = {}) {
  const defaultProps = {
    label: 'Test Tile',
    description: 'A test description',
    to: '/test-route',
  };
  return render(
    <MemoryRouter>
      <DashboardTile {...defaultProps} {...props} />
    </MemoryRouter>
  );
}

describe('DashboardTile', () => {
  it('renders the label prop as a heading element', () => {
    renderTile({ label: 'New Brief' });

    expect(screen.getByRole('heading', { name: 'New Brief' })).toBeInTheDocument();
  });

  it('renders the description prop as secondary text beneath the label', () => {
    renderTile({ description: 'Submit a new creative project request' });

    expect(screen.getByText('Submit a new creative project request')).toBeInTheDocument();
    const description = screen.getByText('Submit a new creative project request');
    expect(description.tagName).toBe('P');
  });

  it('renders as a React Router Link with the correct href', () => {
    renderTile({ to: '/new-brief' });

    const link = screen.getByRole('link');
    expect(link).toHaveAttribute('href', '/new-brief');
  });

  it('applies card-like styling with border, border-radius, and box-shadow', () => {
    renderTile();

    const link = screen.getByRole('link');
    expect(link.style.border).toContain('1px solid');
    expect(link.style.borderRadius).toBe('2px');
    expect(link.style.boxShadow).toBeTruthy();
  });
});
