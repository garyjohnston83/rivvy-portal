import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { describe, it, expect } from 'vitest';
import DashboardPage from './DashboardPage';

function renderDashboardPage() {
  return render(
    <MemoryRouter>
      <DashboardPage />
    </MemoryRouter>
  );
}

describe('DashboardPage', () => {
  it('renders with data-testid="page-dashboard" on the outermost wrapper div', () => {
    renderDashboardPage();

    expect(screen.getByTestId('page-dashboard')).toBeInTheDocument();
  });

  it('renders exactly 3 DashboardTile instances as links', () => {
    renderDashboardPage();

    const links = screen.getAllByRole('link');
    expect(links).toHaveLength(3);
  });

  it('renders tiles in the correct order: New Brief, Screening Room, Brand Vault', () => {
    renderDashboardPage();

    const headings = screen.getAllByRole('heading');
    expect(headings[0]).toHaveTextContent('New Brief');
    expect(headings[1]).toHaveTextContent('Screening Room');
    expect(headings[2]).toHaveTextContent('Brand Vault');
  });

  it('links each tile to the correct route', () => {
    renderDashboardPage();

    const links = screen.getAllByRole('link');
    expect(links[0]).toHaveAttribute('href', '/new-brief');
    expect(links[1]).toHaveAttribute('href', '/screening');
    expect(links[2]).toHaveAttribute('href', '/vault');
  });

  it('displays the correct description text for each tile', () => {
    renderDashboardPage();

    expect(screen.getByText('Submit a new creative project request')).toBeInTheDocument();
    expect(screen.getByText('Review and comment on video deliverables')).toBeInTheDocument();
    expect(screen.getByText('Manage your brand assets and guidelines')).toBeInTheDocument();
  });
});
