import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import { describe, it, expect } from 'vitest';
import RootLayout from '../layouts/RootLayout';
import DashboardPage from '../pages/DashboardPage';
import DashboardTile from '../components/DashboardTile';

function renderFullDashboard() {
  return render(
    <MemoryRouter initialEntries={['/dashboard']}>
      <Routes>
        <Route element={<RootLayout />}>
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/new-brief" element={<div data-testid="page-new-brief">New Brief Page</div>} />
          <Route path="/screening" element={<div data-testid="page-screening">Screening Page</div>} />
          <Route path="/vault" element={<div data-testid="page-vault">Vault Page</div>} />
        </Route>
      </Routes>
    </MemoryRouter>
  );
}

describe('Client Dashboard Integration', () => {
  it('renders the full layout with header, tile grid, and footer in correct order', () => {
    renderFullDashboard();

    const header = screen.getByRole('banner');
    const dashboardGrid = screen.getByTestId('page-dashboard');
    const footer = screen.getByRole('contentinfo');

    expect(header).toBeInTheDocument();
    expect(dashboardGrid).toBeInTheDocument();
    expect(footer).toBeInTheDocument();

    // Verify DOM order: header before dashboard, dashboard before footer
    expect(header.compareDocumentPosition(dashboardGrid) & Node.DOCUMENT_POSITION_FOLLOWING).toBeTruthy();
    expect(dashboardGrid.compareDocumentPosition(footer) & Node.DOCUMENT_POSITION_FOLLOWING).toBeTruthy();
  });

  it('renders exactly 3 tiles with no fourth tile or placeholder element in the grid', () => {
    renderFullDashboard();

    const links = screen.getAllByRole('link');
    // 3 tile links + 1 brand link in header = 4 total links
    const tileLinks = links.filter(link => {
      const href = link.getAttribute('href');
      return href === '/new-brief' || href === '/screening' || href === '/vault';
    });
    expect(tileLinks).toHaveLength(3);

    // Verify no fourth child in the grid container
    const gridContainer = screen.getByTestId('page-dashboard').querySelector('.dashboard-grid');
    expect(gridContainer).toBeTruthy();
    expect(gridContainer!.children).toHaveLength(3);
  });

  it('supports keyboard tab order: New Brief, Screening Room, Brand Vault', () => {
    renderFullDashboard();

    const links = screen.getAllByRole('link');
    const tileLinks = links.filter(link => {
      const href = link.getAttribute('href');
      return href === '/new-brief' || href === '/screening' || href === '/vault';
    });

    // Verify the tab order matches visual order by checking DOM position
    expect(tileLinks[0]).toHaveAttribute('href', '/new-brief');
    expect(tileLinks[1]).toHaveAttribute('href', '/screening');
    expect(tileLinks[2]).toHaveAttribute('href', '/vault');

    // DOM order determines tab order - verify first tile comes before second, second before third
    expect(tileLinks[0].compareDocumentPosition(tileLinks[1]) & Node.DOCUMENT_POSITION_FOLLOWING).toBeTruthy();
    expect(tileLinks[1].compareDocumentPosition(tileLinks[2]) & Node.DOCUMENT_POSITION_FOLLOWING).toBeTruthy();
  });

  it('has a responsive grid class with the dashboard-grid CSS class applied', () => {
    renderFullDashboard();

    const gridContainer = screen.getByTestId('page-dashboard').querySelector('.dashboard-grid');
    expect(gridContainer).toBeTruthy();
    expect(gridContainer!.className).toContain('dashboard-grid');
  });

  it('changes DashboardTile style on hover via onMouseEnter/onMouseLeave', async () => {
    const user = userEvent.setup();
    render(
      <MemoryRouter>
        <DashboardTile label="Test" description="Desc" to="/test" />
      </MemoryRouter>
    );

    const link = screen.getByRole('link');
    const initialBoxShadow = link.style.boxShadow;

    await user.hover(link);
    const hoveredBoxShadow = link.style.boxShadow;
    expect(hoveredBoxShadow).not.toBe(initialBoxShadow);

    await user.unhover(link);
    expect(link.style.boxShadow).toBe(initialBoxShadow);
  });

  it('navigates to the tile destination when a tile is clicked', async () => {
    const user = userEvent.setup();
    renderFullDashboard();

    const newBriefLink = screen.getByRole('link', { name: /New Brief/i });
    await user.click(newBriefLink);

    expect(screen.getByTestId('page-new-brief')).toBeInTheDocument();
  });

  it('tile descriptions are visible and accessible in the DOM', () => {
    renderFullDashboard();

    const desc1 = screen.getByText('Submit a new creative project request');
    const desc2 = screen.getByText('Review and comment on video deliverables');
    const desc3 = screen.getByText('Manage your brand assets and guidelines');

    // Verify descriptions are visible (not hidden with display:none or visibility:hidden)
    expect(desc1).toBeVisible();
    expect(desc2).toBeVisible();
    expect(desc3).toBeVisible();
  });
});
