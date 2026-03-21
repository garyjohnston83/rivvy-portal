import { render, screen, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import VaultPage from './VaultPage';

function renderVaultPage() {
  return render(
    <MemoryRouter>
      <VaultPage />
    </MemoryRouter>
  );
}

const mockCountsResponse = {
  orgCounts: { logos: 5, fonts: 3, guidelines: 2, visuals: 8 },
  projectCounts: null,
};

describe('VaultPage', () => {
  let originalFetch: typeof global.fetch;

  beforeEach(() => {
    originalFetch = global.fetch;
  });

  afterEach(() => {
    global.fetch = originalFetch;
  });

  it('renders data-testid="page-vault" on the outermost div', () => {
    global.fetch = vi.fn(() =>
      Promise.resolve({
        ok: true,
        json: () => Promise.resolve(mockCountsResponse),
      })
    ) as unknown as typeof global.fetch;

    renderVaultPage();

    expect(screen.getByTestId('page-vault')).toBeInTheDocument();
  });

  it('renders all four category tiles after successful fetch', async () => {
    global.fetch = vi.fn(() =>
      Promise.resolve({
        ok: true,
        json: () => Promise.resolve(mockCountsResponse),
      })
    ) as unknown as typeof global.fetch;

    renderVaultPage();

    await waitFor(() => {
      expect(screen.getByText('Logos')).toBeInTheDocument();
    });

    expect(screen.getByText('Fonts')).toBeInTheDocument();
    expect(screen.getByText('Guidelines')).toBeInTheDocument();
    expect(screen.getByText('Visuals')).toBeInTheDocument();
  });

  it('shows loading state initially before fetch resolves', () => {
    global.fetch = vi.fn(
      () => new Promise(() => {})
    ) as unknown as typeof global.fetch;

    renderVaultPage();

    expect(screen.getByText('Loading...')).toBeInTheDocument();
  });

  it('shows error message with role="alert" on fetch failure', async () => {
    global.fetch = vi.fn(() =>
      Promise.reject(new Error('Network error'))
    ) as unknown as typeof global.fetch;

    renderVaultPage();

    await waitFor(() => {
      expect(screen.getByRole('alert')).toBeInTheDocument();
    });

    expect(screen.getByRole('alert')).toHaveTextContent('Network error');
  });

  // --- Gap test: VaultPage renders tiles with 0 counts when API returns empty/zero counts ---
  it('renders tiles with 0 counts when API returns zero counts', async () => {
    const zeroCountsResponse = {
      orgCounts: { logos: 0, fonts: 0, guidelines: 0, visuals: 0 },
      projectCounts: null,
    };

    global.fetch = vi.fn(() =>
      Promise.resolve({
        ok: true,
        json: () => Promise.resolve(zeroCountsResponse),
      })
    ) as unknown as typeof global.fetch;

    renderVaultPage();

    await waitFor(() => {
      expect(screen.getByText('Logos')).toBeInTheDocument();
    });

    const zeroCountElements = screen.getAllByText('0 org assets');
    expect(zeroCountElements).toHaveLength(4);
  });

  // --- Gap test: VaultPage passes correct orgCount values from fetch response to each tile ---
  it('passes correct orgCount values from fetch response to each tile', async () => {
    global.fetch = vi.fn(() =>
      Promise.resolve({
        ok: true,
        json: () => Promise.resolve(mockCountsResponse),
      })
    ) as unknown as typeof global.fetch;

    renderVaultPage();

    await waitFor(() => {
      expect(screen.getByText('Logos')).toBeInTheDocument();
    });

    expect(screen.getByLabelText('Logos: 5 org assets')).toBeInTheDocument();
    expect(screen.getByLabelText('Fonts: 3 org assets')).toBeInTheDocument();
    expect(screen.getByLabelText('Guidelines: 2 org assets')).toBeInTheDocument();
    expect(screen.getByLabelText('Visuals: 8 org assets')).toBeInTheDocument();
  });

  // --- Gap test: VaultPage calls fetch with credentials: 'include' ---
  it('fetches counts endpoint with credentials include', async () => {
    global.fetch = vi.fn(() =>
      Promise.resolve({
        ok: true,
        json: () => Promise.resolve(mockCountsResponse),
      })
    ) as unknown as typeof global.fetch;

    renderVaultPage();

    await waitFor(() => {
      expect(global.fetch).toHaveBeenCalled();
    });

    expect(global.fetch).toHaveBeenCalledWith('/api/brand-assets/counts', {
      credentials: 'include',
    });
  });
});
