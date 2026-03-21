import { render, screen, waitFor, act } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import NewBriefPage from './NewBriefPage';

// Mock useNavigate
const mockNavigate = vi.fn();
vi.mock('react-router-dom', async () => {
  const actual = await vi.importActual('react-router-dom');
  return {
    ...actual,
    useNavigate: () => mockNavigate,
  };
});

const mockBriefResponse = {
  id: 'aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee',
  orgId: '11111111-2222-3333-4444-555555555555',
  submittedById: '66666666-7777-8888-9999-000000000000',
  title: 'Untitled Brief',
  description: null,
  status: 'DRAFT',
  priority: 'NORMAL',
  desiredDueDate: null,
  budget: null,
  creativeDirection: null,
  metadata: {},
  references: {},
  createdAt: '2026-03-17T10:30:00Z',
  updatedAt: null,
};

function renderNewBriefPage() {
  return render(
    <MemoryRouter>
      <NewBriefPage />
    </MemoryRouter>
  );
}

function mockPostSuccess() {
  return vi.spyOn(globalThis, 'fetch').mockResolvedValue(
    new Response(JSON.stringify(mockBriefResponse), {
      status: 201,
      headers: { 'Content-Type': 'application/json' },
    })
  );
}

describe('NewBriefPage', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
    mockNavigate.mockReset();
  });

  // --- Task Group 5 Tests ---

  it('renders form fields after draft creation', async () => {
    mockPostSuccess();

    renderNewBriefPage();

    await waitFor(() => {
      expect(screen.getByLabelText('Title')).toBeInTheDocument();
    });

    expect(screen.getByLabelText('Description')).toBeInTheDocument();
    expect(screen.getByLabelText('Priority')).toBeInTheDocument();
    expect(screen.getByLabelText('Desired Due Date')).toBeInTheDocument();
    expect(screen.getByLabelText('Budget')).toBeInTheDocument();
    expect(screen.getByLabelText('Creative Direction')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Cancel' })).toBeInTheDocument();
  });

  it('calls POST /api/briefs on mount to auto-create draft', async () => {
    const fetchSpy = mockPostSuccess();

    renderNewBriefPage();

    await waitFor(() => {
      expect(fetchSpy).toHaveBeenCalledWith('/api/briefs', {
        method: 'POST',
        credentials: 'include',
      });
    });
  });

  it('shows loading state while draft is being created', async () => {
    let resolveFetch: (value: Response) => void;
    vi.spyOn(globalThis, 'fetch').mockImplementation(
      () =>
        new Promise<Response>((resolve) => {
          resolveFetch = resolve;
        })
    );

    renderNewBriefPage();

    expect(screen.getByText('Creating brief...')).toBeInTheDocument();

    // Resolve to clean up
    resolveFetch!(
      new Response(JSON.stringify(mockBriefResponse), {
        status: 201,
        headers: { 'Content-Type': 'application/json' },
      })
    );

    await waitFor(() => {
      expect(screen.queryByText('Creating brief...')).not.toBeInTheDocument();
    });
  });

  it('shows error banner if draft creation fails', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response(JSON.stringify({ error: 'Internal Server Error' }), {
        status: 500,
        headers: { 'Content-Type': 'application/json' },
      })
    );

    renderNewBriefPage();

    await waitFor(() => {
      expect(screen.getByRole('alert')).toHaveTextContent(
        'Failed to create brief. Please try again.'
      );
    });
  });

  it('populates form fields from server response', async () => {
    mockPostSuccess();

    renderNewBriefPage();

    await waitFor(() => {
      expect(screen.getByLabelText('Title')).toHaveValue('Untitled Brief');
    });

    expect(screen.getByLabelText('Priority')).toHaveValue('NORMAL');
  });

  it('priority dropdown has Normal, High, Urgent options', async () => {
    mockPostSuccess();

    renderNewBriefPage();

    await waitFor(() => {
      expect(screen.getByLabelText('Priority')).toBeInTheDocument();
    });

    const select = screen.getByLabelText('Priority');
    const options = select.querySelectorAll('option');

    expect(options).toHaveLength(3);
    expect(options[0]).toHaveTextContent('Normal');
    expect(options[0]).toHaveValue('NORMAL');
    expect(options[1]).toHaveTextContent('High');
    expect(options[1]).toHaveValue('HIGH');
    expect(options[2]).toHaveTextContent('Urgent');
    expect(options[2]).toHaveValue('URGENT');
  });

  // --- Task Group 6 Tests ---

  it('autosaves via PUT after user stops editing for ~1.5s', async () => {
    const user = userEvent.setup();

    const fetchSpy = mockPostSuccess();

    renderNewBriefPage();

    // Wait for draft creation with real timers
    await waitFor(() => {
      expect(screen.getByLabelText('Title')).toHaveValue('Untitled Brief');
    });

    // Now switch to fake timers for the debounce
    vi.useFakeTimers({ shouldAdvanceTime: true });

    // Reset call tracking to focus on the PUT
    fetchSpy.mockClear();
    fetchSpy.mockResolvedValue(
      new Response(JSON.stringify({ ...mockBriefResponse, title: 'My Project' }), {
        status: 200,
        headers: { 'Content-Type': 'application/json' },
      })
    );

    // Type into the title field
    const titleInput = screen.getByLabelText('Title');
    await user.clear(titleInput);
    await user.type(titleInput, 'My Project');

    // Advance past debounce delay
    await act(async () => {
      await vi.advanceTimersByTimeAsync(2000);
    });

    expect(fetchSpy).toHaveBeenCalledWith(
      `/api/briefs/${mockBriefResponse.id}`,
      expect.objectContaining({
        method: 'PUT',
        credentials: 'include',
        headers: { 'Content-Type': 'application/json' },
      })
    );

    // Verify the body contains the updated title
    const putCall = fetchSpy.mock.calls.find(
      (call) => typeof call[1] === 'object' && call[1]?.method === 'PUT'
    );
    expect(putCall).toBeDefined();
    const body = JSON.parse(putCall![1]!.body as string);
    expect(body.title).toBe('My Project');

    vi.useRealTimers();
  });

  it('does not autosave if briefId is null', async () => {
    vi.useFakeTimers({ shouldAdvanceTime: true });

    // Mock fetch to hang forever (draft never created)
    const fetchSpy = vi.spyOn(globalThis, 'fetch').mockImplementation(
      () => new Promise<Response>(() => { /* never resolves */ })
    );

    renderNewBriefPage();

    // The loading state is shown, briefId is null
    expect(screen.getByText('Creating brief...')).toBeInTheDocument();

    // Advance timers significantly
    await act(async () => {
      await vi.advanceTimersByTimeAsync(5000);
    });

    // Only the initial POST should have been called, no PUT
    expect(fetchSpy).toHaveBeenCalledTimes(1);
    expect(fetchSpy).toHaveBeenCalledWith('/api/briefs', {
      method: 'POST',
      credentials: 'include',
    });

    vi.useRealTimers();
  });

  it('shows "Saving..." indicator during autosave', async () => {
    const user = userEvent.setup();

    let resolvePut!: (value: Response) => void;

    mockPostSuccess();

    renderNewBriefPage();

    // Wait for draft creation with real timers
    await waitFor(() => {
      expect(screen.getByLabelText('Title')).toHaveValue('Untitled Brief');
    });

    // Switch to fake timers for debounce
    vi.useFakeTimers({ shouldAdvanceTime: true });

    // Now mock the PUT to be pending
    vi.spyOn(globalThis, 'fetch').mockImplementation(
      () =>
        new Promise<Response>((resolve) => {
          resolvePut = resolve;
        })
    );

    // Type to trigger autosave
    const titleInput = screen.getByLabelText('Title');
    await user.clear(titleInput);
    await user.type(titleInput, 'Test');

    // Advance past debounce delay to trigger autosave
    await act(async () => {
      await vi.advanceTimersByTimeAsync(2000);
    });

    // Saving... should be visible while PUT is in flight
    expect(screen.getByText('Saving...')).toBeInTheDocument();

    // Resolve the PUT to clean up
    await act(async () => {
      resolvePut(
        new Response(JSON.stringify({ ...mockBriefResponse, title: 'Test' }), {
          status: 200,
          headers: { 'Content-Type': 'application/json' },
        })
      );
    });

    vi.useRealTimers();
  });

  it('shows "Saved" indicator after successful autosave', async () => {
    const user = userEvent.setup();

    mockPostSuccess();

    renderNewBriefPage();

    // Wait for draft creation with real timers
    await waitFor(() => {
      expect(screen.getByLabelText('Title')).toHaveValue('Untitled Brief');
    });

    // Switch to fake timers for debounce
    vi.useFakeTimers({ shouldAdvanceTime: true });

    // Mock the PUT to succeed
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response(JSON.stringify({ ...mockBriefResponse, title: 'Done' }), {
        status: 200,
        headers: { 'Content-Type': 'application/json' },
      })
    );

    // Type to trigger autosave
    const titleInput = screen.getByLabelText('Title');
    await user.clear(titleInput);
    await user.type(titleInput, 'Done');

    // Advance past debounce delay
    await act(async () => {
      await vi.advanceTimersByTimeAsync(2000);
    });

    // After successful PUT, "Saved" should appear
    await waitFor(() => {
      expect(screen.getByText('Saved')).toBeInTheDocument();
    });

    vi.useRealTimers();
  });

  // --- Task Group 7: Gap-Fill Tests ---

  it('Cancel button calls DELETE and navigates to /dashboard', async () => {
    const user = userEvent.setup();

    const fetchSpy = vi.spyOn(globalThis, 'fetch')
      .mockResolvedValueOnce(
        new Response(JSON.stringify(mockBriefResponse), {
          status: 201,
          headers: { 'Content-Type': 'application/json' },
        })
      )
      .mockResolvedValueOnce(
        new Response(null, { status: 204 })
      );

    renderNewBriefPage();

    // Wait for form to render
    await waitFor(() => {
      expect(screen.getByRole('button', { name: 'Cancel' })).toBeInTheDocument();
    });

    await user.click(screen.getByRole('button', { name: 'Cancel' }));

    await waitFor(() => {
      expect(fetchSpy).toHaveBeenCalledWith(
        `/api/briefs/${mockBriefResponse.id}`,
        expect.objectContaining({
          method: 'DELETE',
          credentials: 'include',
        })
      );
    });

    expect(mockNavigate).toHaveBeenCalledWith('/dashboard');
  });

  it('Cancel button navigates to /dashboard even if DELETE fails', async () => {
    const user = userEvent.setup();

    vi.spyOn(globalThis, 'fetch')
      .mockResolvedValueOnce(
        new Response(JSON.stringify(mockBriefResponse), {
          status: 201,
          headers: { 'Content-Type': 'application/json' },
        })
      )
      .mockRejectedValueOnce(new Error('Network error'));

    renderNewBriefPage();

    await waitFor(() => {
      expect(screen.getByRole('button', { name: 'Cancel' })).toBeInTheDocument();
    });

    await user.click(screen.getByRole('button', { name: 'Cancel' }));

    await waitFor(() => {
      expect(mockNavigate).toHaveBeenCalledWith('/dashboard');
    });
  });

  it('debounce timer resets when user continues typing', async () => {
    vi.useFakeTimers({ shouldAdvanceTime: true });
    const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime });

    const fetchSpy = vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response(JSON.stringify(mockBriefResponse), {
        status: 201,
        headers: { 'Content-Type': 'application/json' },
      })
    );

    renderNewBriefPage();

    await waitFor(() => {
      expect(screen.getByLabelText('Title')).toHaveValue('Untitled Brief');
    });

    // Advance past initial skip trigger
    await act(async () => {
      await vi.advanceTimersByTimeAsync(2000);
    });

    fetchSpy.mockClear();
    fetchSpy.mockResolvedValue(
      new Response(JSON.stringify({ ...mockBriefResponse, title: 'AB' }), {
        status: 200,
        headers: { 'Content-Type': 'application/json' },
      })
    );

    const titleInput = screen.getByLabelText('Title');

    // Type first character
    await user.clear(titleInput);
    await user.type(titleInput, 'A');

    // Advance 1000ms (less than 1500ms debounce)
    await act(async () => {
      await vi.advanceTimersByTimeAsync(1000);
    });

    // No PUT should have been called yet
    expect(fetchSpy).not.toHaveBeenCalled();

    // Type another character, which resets the timer
    await user.type(titleInput, 'B');

    // Advance 1000ms again (still less than 1500ms from the last keystroke)
    await act(async () => {
      await vi.advanceTimersByTimeAsync(1000);
    });

    // Still no PUT -- the timer was reset by the second keystroke
    expect(fetchSpy).not.toHaveBeenCalled();

    // Advance another 600ms (now 1600ms since last keystroke)
    await act(async () => {
      await vi.advanceTimersByTimeAsync(600);
    });

    // NOW the PUT should fire
    expect(fetchSpy).toHaveBeenCalledWith(
      `/api/briefs/${mockBriefResponse.id}`,
      expect.objectContaining({ method: 'PUT' })
    );

    vi.useRealTimers();
  });

  it('shows "Save failed" after autosave error and does not break subsequent saves', async () => {
    vi.useFakeTimers({ shouldAdvanceTime: true });
    const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime });

    vi.spyOn(globalThis, 'fetch').mockResolvedValueOnce(
      new Response(JSON.stringify(mockBriefResponse), {
        status: 201,
        headers: { 'Content-Type': 'application/json' },
      })
    );

    renderNewBriefPage();

    await waitFor(() => {
      expect(screen.getByLabelText('Title')).toHaveValue('Untitled Brief');
    });

    await act(async () => {
      await vi.advanceTimersByTimeAsync(2000);
    });

    // Mock PUT to fail
    vi.spyOn(globalThis, 'fetch').mockResolvedValueOnce(
      new Response(JSON.stringify({ error: 'Server Error' }), {
        status: 500,
        headers: { 'Content-Type': 'application/json' },
      })
    );

    // Type to trigger autosave
    const titleInput = screen.getByLabelText('Title');
    await user.clear(titleInput);
    await user.type(titleInput, 'Fail');

    await act(async () => {
      await vi.advanceTimersByTimeAsync(2000);
    });

    await waitFor(() => {
      expect(screen.getByText('Save failed')).toBeInTheDocument();
    });

    // Now mock a successful PUT for recovery
    vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response(JSON.stringify({ ...mockBriefResponse, title: 'Recovered' }), {
        status: 200,
        headers: { 'Content-Type': 'application/json' },
      })
    );

    // Type again to trigger another autosave
    await user.type(titleInput, 'ed');

    await act(async () => {
      await vi.advanceTimersByTimeAsync(2000);
    });

    await waitFor(() => {
      expect(screen.getByText('Saved')).toBeInTheDocument();
    });

    vi.useRealTimers();
  });

  it('shows error when fetch throws on draft creation (network error)', async () => {
    vi.spyOn(globalThis, 'fetch').mockRejectedValue(new Error('Network error'));

    renderNewBriefPage();

    await waitFor(() => {
      expect(screen.getByRole('alert')).toHaveTextContent(
        'Failed to create brief. Please try again.'
      );
    });
  });

  it('has data-testid page-new-brief on root div', async () => {
    mockPostSuccess();

    renderNewBriefPage();

    expect(screen.getByTestId('page-new-brief')).toBeInTheDocument();
  });

  it('autosave sends all form fields in PUT body', async () => {
    vi.useFakeTimers({ shouldAdvanceTime: true });
    const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime });

    const fullResponse = {
      ...mockBriefResponse,
      title: 'My Project',
      description: 'A description',
      priority: 'HIGH',
      desiredDueDate: '2026-06-01',
      budget: 5000,
      creativeDirection: 'Bold and modern',
    };

    vi.spyOn(globalThis, 'fetch').mockResolvedValueOnce(
      new Response(JSON.stringify(fullResponse), {
        status: 201,
        headers: { 'Content-Type': 'application/json' },
      })
    );

    renderNewBriefPage();

    await waitFor(() => {
      expect(screen.getByLabelText('Title')).toHaveValue('My Project');
    });

    await act(async () => {
      await vi.advanceTimersByTimeAsync(2000);
    });

    const fetchSpy = vi.spyOn(globalThis, 'fetch').mockResolvedValue(
      new Response(JSON.stringify({ ...fullResponse, title: 'Updated Project' }), {
        status: 200,
        headers: { 'Content-Type': 'application/json' },
      })
    );

    // Change the title to trigger autosave
    const titleInput = screen.getByLabelText('Title');
    await user.clear(titleInput);
    await user.type(titleInput, 'Updated Project');

    await act(async () => {
      await vi.advanceTimersByTimeAsync(2000);
    });

    const putCall = fetchSpy.mock.calls.find(
      (call) => typeof call[1] === 'object' && call[1]?.method === 'PUT'
    );
    expect(putCall).toBeDefined();
    const body = JSON.parse(putCall![1]!.body as string);

    // Verify all fields are sent
    expect(body.title).toBe('Updated Project');
    expect(body.description).toBe('A description');
    expect(body.priority).toBe('HIGH');
    expect(body.desiredDueDate).toBe('2026-06-01');
    expect(body.budget).toBe(5000);
    expect(body.creativeDirection).toBe('Bold and modern');

    vi.useRealTimers();
  });
});
