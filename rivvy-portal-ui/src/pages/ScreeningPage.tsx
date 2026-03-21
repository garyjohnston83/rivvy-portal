import { useState, useEffect, useRef, useCallback } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { colors, fonts } from '../theme';

interface VideoListItem {
  id: string;
  title: string;
  currentVersionNumber: number | null;
  approved: boolean;
}

interface PageMeta {
  totalPages: number;
  number: number;
  last: boolean;
}

function VideoCard({ video, onClick }: { video: VideoListItem; onClick: () => void }) {
  const [hovered, setHovered] = useState(false);
  const [focused, setFocused] = useState(false);

  const active = hovered || focused;
  const activeStyle: React.CSSProperties = active
    ? { borderColor: colors.orange, boxShadow: '0 4px 20px rgba(193, 85, 58, 0.2)' }
    : {};

  return (
    <div
      data-testid="video-card"
      role="button"
      tabIndex={0}
      style={{ ...styles.card, ...activeStyle }}
      onClick={onClick}
      onKeyDown={(e) => {
        if (e.key === 'Enter' || e.key === ' ') {
          e.preventDefault();
          onClick();
        }
      }}
      onMouseEnter={() => setHovered(true)}
      onMouseLeave={() => setHovered(false)}
      onFocus={() => setFocused(true)}
      onBlur={() => setFocused(false)}
    >
      <div style={styles.cardContent}>
        <span style={styles.cardTitle}>{video.title}</span>
        <div style={styles.badges}>
          <span style={styles.versionBadge}>
            {video.currentVersionNumber !== null ? `V${video.currentVersionNumber}` : '\u2014'}
          </span>
          {video.approved && (
            <span style={styles.approvedBadge}>Approved</span>
          )}
        </div>
      </div>
    </div>
  );
}

function ScreeningPage() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const projectId = searchParams.get('projectId');

  const [items, setItems] = useState<VideoListItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const [error, setError] = useState('');
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(false);

  const sentinelRef = useRef<HTMLDivElement | null>(null);

  const fetchPage = useCallback(async (pageNum: number, signal?: AbortSignal) => {
    const isFirstPage = pageNum === 0;
    if (isFirstPage) {
      setLoading(true);
    } else {
      setLoadingMore(true);
    }

    try {
      const response = await fetch(
        `/api/videos?projectId=${projectId}&page=${pageNum}`,
        { credentials: 'include', signal }
      );

      if (!response.ok) {
        throw new Error('Failed to load videos');
      }

      const data = await response.json();
      const pageMeta: PageMeta = {
        totalPages: data.totalPages,
        number: data.number,
        last: data.last,
      };

      if (isFirstPage) {
        setItems(data.content);
      } else {
        setItems((prev) => [...prev, ...data.content]);
      }

      setHasMore(!pageMeta.last);
      setPage(pageMeta.number);
    } catch (err) {
      if (err instanceof DOMException && err.name === 'AbortError') {
        return;
      }
      setError(err instanceof Error ? err.message : 'An unexpected error occurred');
    } finally {
      if (isFirstPage) {
        setLoading(false);
      } else {
        setLoadingMore(false);
      }
    }
  }, [projectId]);

  useEffect(() => {
    if (!projectId) {
      setLoading(false);
      return;
    }

    let cancelled = false;
    const controller = new AbortController();

    setItems([]);
    setError('');
    setPage(0);
    setHasMore(false);

    async function load() {
      try {
        const response = await fetch(
          `/api/videos?projectId=${projectId}&page=0`,
          { credentials: 'include', signal: controller.signal }
        );

        if (!response.ok) {
          throw new Error('Failed to load videos');
        }

        const data = await response.json();

        if (!cancelled) {
          setItems(data.content);
          setHasMore(!data.last);
          setPage(data.number);
        }
      } catch (err) {
        if (err instanceof DOMException && err.name === 'AbortError') {
          return;
        }
        if (!cancelled) {
          setError(err instanceof Error ? err.message : 'An unexpected error occurred');
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    }

    load();

    return () => {
      cancelled = true;
      controller.abort();
    };
  }, [projectId]);

  // Infinite scroll observer
  useEffect(() => {
    if (!hasMore || loadingMore) return;

    const sentinel = sentinelRef.current;
    if (!sentinel) return;

    const observer = new IntersectionObserver(
      (entries) => {
        if (entries[0].isIntersecting && hasMore && !loadingMore) {
          fetchPage(page + 1);
        }
      },
      { rootMargin: '0px 0px 200px 0px' }
    );

    observer.observe(sentinel);

    return () => {
      observer.disconnect();
    };
  }, [hasMore, loadingMore, page, fetchPage]);

  if (!projectId) {
    return (
      <div data-testid="page-screening">
        <h1 style={styles.pageTitle}>Screening Room</h1>
        <p style={styles.emptyText}>No project selected</p>
      </div>
    );
  }

  if (loading) {
    return (
      <div data-testid="page-screening">
        <h1 style={styles.pageTitle}>Screening Room</h1>
        <p style={styles.loadingText}>Loading...</p>
      </div>
    );
  }

  return (
    <div data-testid="page-screening">
      <h1 style={styles.pageTitle}>Screening Room</h1>

      {error && (
        <div role="alert" style={styles.error}>{error}</div>
      )}

      {items.length === 0 && !error ? (
        <p style={styles.emptyText}>No videos found for this project</p>
      ) : (
        <div style={styles.list}>
          {items.map((video) => (
            <VideoCard
              key={video.id}
              video={video}
              onClick={() => navigate(`/screening/${video.id}`)}
            />
          ))}

          {hasMore && <div ref={sentinelRef} style={styles.sentinel} />}

          {loadingMore && (
            <p style={styles.loadingMoreText}>Loading more...</p>
          )}
        </div>
      )}
    </div>
  );
}

const styles: Record<string, React.CSSProperties> = {
  pageTitle: {
    fontFamily: fonts.heading,
    fontSize: '1.75rem',
    fontWeight: 400,
    letterSpacing: '0.05em',
    color: colors.text,
    marginBottom: '2rem',
  },
  loadingText: {
    color: colors.textMuted,
    fontSize: '0.8rem',
    letterSpacing: '0.1em',
    fontFamily: fonts.body,
  },
  loadingMoreText: {
    color: colors.textMuted,
    fontSize: '0.75rem',
    letterSpacing: '0.1em',
    fontFamily: fonts.body,
    textAlign: 'center' as const,
    padding: '1rem 0',
  },
  error: {
    color: colors.error,
    fontSize: '0.75rem',
    marginBottom: '1.5rem',
    padding: '0.75rem 1rem',
    backgroundColor: colors.errorBg,
    borderRadius: '2px',
    letterSpacing: '0.02em',
    fontFamily: fonts.body,
  },
  emptyText: {
    color: colors.textMuted,
    fontSize: '0.85rem',
    letterSpacing: '0.05em',
    fontFamily: fonts.body,
  },
  list: {
    display: 'flex',
    flexDirection: 'column' as const,
    gap: '0.75rem',
  },
  card: {
    display: 'block',
    padding: '1.25rem 1.5rem',
    border: `1px solid ${colors.border}`,
    borderRadius: '2px',
    backgroundColor: '#111111',
    cursor: 'pointer',
    transition: 'border-color 0.3s, box-shadow 0.3s',
    outline: 'none',
  },
  cardContent: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  cardTitle: {
    fontFamily: fonts.heading,
    fontSize: '1rem',
    fontWeight: 400,
    color: colors.text,
    letterSpacing: '0.03em',
  },
  badges: {
    display: 'flex',
    gap: '0.5rem',
    alignItems: 'center',
  },
  versionBadge: {
    fontFamily: fonts.body,
    fontSize: '0.7rem',
    color: colors.textMuted,
    letterSpacing: '0.1em',
    padding: '0.2rem 0.5rem',
    border: `1px solid ${colors.border}`,
    borderRadius: '2px',
  },
  approvedBadge: {
    fontFamily: fonts.body,
    fontSize: '0.65rem',
    letterSpacing: '0.1em',
    textTransform: 'uppercase' as const,
    color: '#ffffff',
    backgroundColor: colors.sage,
    padding: '0.2rem 0.6rem',
    borderRadius: '2px',
  },
  sentinel: {
    height: '1px',
  },
};

export default ScreeningPage;
