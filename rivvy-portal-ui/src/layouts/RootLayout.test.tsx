import { render, screen } from '@testing-library/react';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import { describe, it, expect } from 'vitest';
import RootLayout from './RootLayout';

function renderLayout(initialRoute = '/test') {
  return render(
    <MemoryRouter initialEntries={[initialRoute]}>
      <Routes>
        <Route element={<RootLayout />}>
          <Route path="/test" element={<div data-testid="child-content">Child Page</div>} />
          <Route path="/dashboard" element={<div data-testid="dashboard-content">Dashboard</div>} />
        </Route>
      </Routes>
    </MemoryRouter>
  );
}

describe('RootLayout', () => {
  it('renders a header element containing the brand link', () => {
    renderLayout();

    const header = screen.getByRole('banner');
    expect(header).toBeInTheDocument();
    const brandLink = screen.getByRole('link', { name: /Rivvy Studios/i });
    expect(brandLink).toBeInTheDocument();
    expect(header.contains(brandLink)).toBe(true);
  });

  it('renders the brand link with href to /dashboard', () => {
    renderLayout();

    const brandLink = screen.getByRole('link', { name: /Rivvy Studios/i });
    expect(brandLink).toHaveAttribute('href', '/dashboard');
  });

  it('renders the header with "Sign out" text', () => {
    renderLayout();

    const header = screen.getByRole('banner');
    expect(header).toHaveTextContent('Sign out');
  });

  it('renders a footer element containing "Rivvy Studios" copyright text', () => {
    renderLayout();

    const footer = screen.getByRole('contentinfo');
    expect(footer).toBeInTheDocument();
    expect(footer).toHaveTextContent('RIVVY STUDIOS');
  });

  it('renders Outlet content between the header and footer', () => {
    renderLayout();

    const header = screen.getByRole('banner');
    const child = screen.getByTestId('child-content');
    const footer = screen.getByRole('contentinfo');

    // Verify DOM ordering: header before child, child before footer
    const wrapper = header.parentElement!;
    const children = Array.from(wrapper.children);
    const headerIndex = children.indexOf(header);
    const footerIndex = children.indexOf(footer);

    // Child content should be in the DOM between header and footer
    expect(headerIndex).toBeLessThan(footerIndex);
    expect(child).toBeInTheDocument();

    // Verify child is rendered between header and footer by checking document position
    const comparison = header.compareDocumentPosition(child);
    expect(comparison & Node.DOCUMENT_POSITION_FOLLOWING).toBeTruthy();

    const comparison2 = child.compareDocumentPosition(footer);
    expect(comparison2 & Node.DOCUMENT_POSITION_FOLLOWING).toBeTruthy();
  });
});
