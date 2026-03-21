import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Routes, Route } from 'react-router-dom';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import ScreeningPage from '../pages/ScreeningPage';
import VideoDetailPage from '../pages/VideoDetailPage';

const PROJECT_ID = '70000000-0000-0000-0000-000000000001';

function renderScreeningPage(queryString = `?projectId=${PROJECT_ID}`) {
  return render(
    <MemoryRouter initialEntries={[`/screening${queryString}`]}>
      <Routes>
        <Route path="/screening" element={<ScreeningPage />} />
        <Route path="/screening/:videoId" element={<VideoDetailPage />} />
      </Routes>
    </MemoryRouter>
  );
}

function renderVideoDetailPage(videoId: string) {
  return render(
    <MemoryRouter initialEntries={[`/screening/${videoId}`]}>
      <Routes>
        <Route path="/screening" element={<ScreeningPage />} />
        <Route path="/screening/:videoId" element={<VideoDetailPage />} />
      </Routes>
    </MemoryRouter>
  );
}

function mockPageResponse(content: unknown[], last = true, number = 0, totalPages = 1) {
  return {
    content,
    totalElements: content.length,
    totalPages,
    number,
    last,
    size: 25,
    first: number === 0,
  };
}

const sampleVideos = [
  { id: 'aaa-111', title: 'Brand Launch Teaser', currentVersionNumber: 1, approved: true },
  { id: 'bbb-222', title: 'Product Demo', currentVersionNumber: 2, approved: false },
  { id: 'ccc-333', title: 'Social Cutdown', currentVersionNumber: null, approved: false },
];

describe('ScreeningPage', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  it('renders loading state initially', () => {
    vi.spyOn(globalThis, 'fetch').mockImplementation(
      () => new Promise(() => {/* never resolves */})
    );

    renderScreeningPage();

    expect(screen.getByText('Loading...')).toBeInTheDocument();
  });

  it('renders video list items after successful fetch', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValueOnce({
      ok: true,
      json: async () => mockPageResponse(sampleVideos),
    } as Response);

    renderScreeningPage();

    await waitFor(() => {
      expect(screen.getByText('Brand Launch Teaser')).toBeInTheDocument();
    });

    expect(screen.getByText('Product Demo')).toBeInTheDocument();
    expect(screen.getByText('Social Cutdown')).toBeInTheDocument();
    expect(screen.getByText('Approved')).toBeInTheDocument();
    expect(screen.getByText('V1')).toBeInTheDocument();
    expect(screen.getByText('V2')).toBeInTheDocument();
  });

  it('renders "No videos found for this project" empty state', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValueOnce({
      ok: true,
      json: async () => mockPageResponse([]),
    } as Response);

    renderScreeningPage();

    await waitFor(() => {
      expect(screen.getByText('No videos found for this project')).toBeInTheDocument();
    });
  });

  it('renders error message on fetch failure', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValueOnce({
      ok: false,
      status: 500,
      json: async () => ({}),
    } as Response);

    renderScreeningPage();

    await waitFor(() => {
      expect(screen.getByRole('alert')).toBeInTheDocument();
    });

    expect(screen.getByRole('alert')).toHaveTextContent('Failed to load videos');
  });

  it('clicking a video card navigates to /screening/:videoId', async () => {
    const user = userEvent.setup();

    vi.spyOn(globalThis, 'fetch').mockResolvedValueOnce({
      ok: true,
      json: async () => mockPageResponse(sampleVideos),
    } as Response);

    // Mock the detail page fetch that happens after navigation
    vi.spyOn(globalThis, 'fetch').mockResolvedValueOnce({
      ok: true,
      json: async () => ({
        id: 'aaa-111',
        title: 'Brand Launch Teaser',
        description: 'A teaser video',
        currentVersionNumber: 1,
        approved: true,
        transcodeStatus: 'COMPLETED',
        playbackUrl: 'https://storage.example.com/stub-presigned/aaa-111?token=stub',
        createdAt: '2026-03-14T00:00:00Z',
      }),
    } as Response);

    renderScreeningPage();

    await waitFor(() => {
      expect(screen.getByText('Brand Launch Teaser')).toBeInTheDocument();
    });

    const card = screen.getByText('Brand Launch Teaser').closest('[data-testid="video-card"]');
    expect(card).toBeTruthy();
    await user.click(card!);

    await waitFor(() => {
      expect(screen.getByTestId('page-video-detail')).toBeInTheDocument();
    });
  });

  // ========================================================================
  // Gap-filling tests (Task Group 5)
  // ========================================================================

  // --- Gap Test 1: Verify "No project selected" message when projectId is missing ---
  it('renders "No project selected" when projectId query param is missing', () => {
    vi.spyOn(globalThis, 'fetch').mockImplementation(
      () => new Promise(() => {/* never resolves */})
    );

    renderScreeningPage('');

    expect(screen.getByText('No project selected')).toBeInTheDocument();
  });

  // --- Gap Test 2: Verify video card displays dash when currentVersionNumber is null ---
  it('renders dash character for video with null currentVersionNumber', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValueOnce({
      ok: true,
      json: async () => mockPageResponse([
        { id: 'ddd-444', title: 'Social Cutdown', currentVersionNumber: null, approved: false },
      ]),
    } as Response);

    renderScreeningPage();

    await waitFor(() => {
      expect(screen.getByText('Social Cutdown')).toBeInTheDocument();
    });

    // The em dash character U+2014 is used when currentVersionNumber is null
    expect(screen.getByText('\u2014')).toBeInTheDocument();
  });

  // --- Gap Test 3: Verify infinite scroll sentinel triggers next page fetch ---
  it('fetches next page when infinite scroll sentinel is observed', async () => {
    // Track IntersectionObserver callbacks using a class-based mock
    let observerCallback: IntersectionObserverCallback | null = null;
    const mockObserve = vi.fn();
    const mockDisconnect = vi.fn();

    const MockIntersectionObserver = class {
      constructor(callback: IntersectionObserverCallback) {
        observerCallback = callback;
      }
      observe = mockObserve;
      disconnect = mockDisconnect;
      unobserve = vi.fn();
      root = null;
      rootMargin = '';
      thresholds = [] as number[];
      takeRecords = () => [] as IntersectionObserverEntry[];
    };

    vi.stubGlobal('IntersectionObserver', MockIntersectionObserver);

    const fetchSpy = vi.spyOn(globalThis, 'fetch');

    // First page response - not last, so hasMore=true
    fetchSpy.mockResolvedValueOnce({
      ok: true,
      json: async () => mockPageResponse(
        [{ id: 'aaa-111', title: 'Video A', currentVersionNumber: 1, approved: false }],
        false, // last = false
        0,     // number = 0
        2      // totalPages = 2
      ),
    } as Response);

    renderScreeningPage();

    await waitFor(() => {
      expect(screen.getByText('Video A')).toBeInTheDocument();
    });

    // Prepare page 1 response
    fetchSpy.mockResolvedValueOnce({
      ok: true,
      json: async () => mockPageResponse(
        [{ id: 'bbb-222', title: 'Video B', currentVersionNumber: 2, approved: true }],
        true,  // last = true
        1,     // number = 1
        2      // totalPages = 2
      ),
    } as Response);

    // Simulate the IntersectionObserver firing (sentinel enters viewport)
    expect(observerCallback).not.toBeNull();
    observerCallback!(
      [{ isIntersecting: true } as IntersectionObserverEntry],
      {} as IntersectionObserver
    );

    // Verify page 1 was fetched and Video B appears
    await waitFor(() => {
      expect(screen.getByText('Video B')).toBeInTheDocument();
    });

    // Both videos should be visible (page 0 + page 1 appended)
    expect(screen.getByText('Video A')).toBeInTheDocument();

    // Verify fetch was called with page=1
    expect(fetchSpy).toHaveBeenCalledWith(
      expect.stringContaining('page=1'),
      expect.objectContaining({ credentials: 'include' })
    );
  });
});

describe('VideoDetailPage', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  it('renders video title and <video> player when playbackUrl is present', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValueOnce({
      ok: true,
      json: async () => ({
        id: 'aaa-111',
        title: 'Brand Launch Teaser',
        description: 'A teaser video',
        currentVersionNumber: 1,
        approved: true,
        transcodeStatus: 'COMPLETED',
        playbackUrl: 'https://storage.example.com/stub-presigned/aaa-111?token=stub',
        createdAt: '2026-03-14T00:00:00Z',
      }),
    } as Response);

    renderVideoDetailPage('aaa-111');

    await waitFor(() => {
      expect(screen.getByText('Brand Launch Teaser')).toBeInTheDocument();
    });

    const heading = screen.getByRole('heading', { name: 'Brand Launch Teaser' });
    expect(heading).toBeInTheDocument();

    const videoElement = document.querySelector('video');
    expect(videoElement).toBeTruthy();
    expect(videoElement!.getAttribute('src')).toBe(
      'https://storage.example.com/stub-presigned/aaa-111?token=stub'
    );
    expect(videoElement!.hasAttribute('controls')).toBe(true);
  });

  it('renders "No version available" when currentVersionNumber is null', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValueOnce({
      ok: true,
      json: async () => ({
        id: 'ddd-444',
        title: 'Social Cutdown',
        description: null,
        currentVersionNumber: null,
        approved: false,
        transcodeStatus: null,
        playbackUrl: null,
        createdAt: '2026-03-11T00:00:00Z',
      }),
    } as Response);

    renderVideoDetailPage('ddd-444');

    await waitFor(() => {
      expect(screen.getByText('Social Cutdown')).toBeInTheDocument();
    });

    expect(screen.getByText('No version available')).toBeInTheDocument();

    const videoElement = document.querySelector('video');
    expect(videoElement).toBeNull();
  });

  it('renders "Video is processing..." when transcodeStatus is not COMPLETED', async () => {
    vi.spyOn(globalThis, 'fetch').mockResolvedValueOnce({
      ok: true,
      json: async () => ({
        id: 'ccc-333',
        title: 'Behind the Scenes',
        description: 'BTS footage',
        currentVersionNumber: 1,
        approved: false,
        transcodeStatus: 'PROCESSING',
        playbackUrl: null,
        createdAt: '2026-03-12T00:00:00Z',
      }),
    } as Response);

    renderVideoDetailPage('ccc-333');

    await waitFor(() => {
      expect(screen.getByText('Behind the Scenes')).toBeInTheDocument();
    });

    expect(screen.getByText('Video is processing...')).toBeInTheDocument();

    const videoElement = document.querySelector('video');
    expect(videoElement).toBeNull();
  });

  // ========================================================================
  // Gap-filling tests (Task Group 5)
  // ========================================================================

  // --- Gap Test 4: Verify back navigation from VideoDetailPage to ScreeningPage ---
  it('clicking back button navigates to /screening', async () => {
    const user = userEvent.setup();

    vi.spyOn(globalThis, 'fetch').mockResolvedValueOnce({
      ok: true,
      json: async () => ({
        id: 'aaa-111',
        title: 'Brand Launch Teaser',
        description: 'A teaser video',
        currentVersionNumber: 1,
        approved: true,
        transcodeStatus: 'COMPLETED',
        playbackUrl: 'https://storage.example.com/stub-presigned/aaa-111?token=stub',
        createdAt: '2026-03-14T00:00:00Z',
      }),
    } as Response);

    // Mock fetch for the ScreeningPage that loads after navigating back
    vi.spyOn(globalThis, 'fetch').mockResolvedValueOnce({
      ok: true,
      json: async () => mockPageResponse([]),
    } as Response);

    renderVideoDetailPage('aaa-111');

    await waitFor(() => {
      expect(screen.getByText('Brand Launch Teaser')).toBeInTheDocument();
    });

    const backButton = screen.getByRole('button', { name: /back to screening room/i });
    expect(backButton).toBeInTheDocument();
    await user.click(backButton);

    await waitFor(() => {
      expect(screen.getByTestId('page-screening')).toBeInTheDocument();
    });
  });
});
