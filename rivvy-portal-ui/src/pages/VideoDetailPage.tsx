import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { colors, fonts } from '../theme';

interface VideoDetail {
  id: string;
  title: string;
  description: string | null;
  currentVersionNumber: number | null;
  approved: boolean;
  transcodeStatus: string | null;
  playbackUrl: string | null;
  createdAt: string;
}

function VideoDetailPage() {
  const { videoId } = useParams<{ videoId: string }>();
  const navigate = useNavigate();

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [video, setVideo] = useState<VideoDetail | null>(null);

  useEffect(() => {
    if (!videoId) {
      setLoading(false);
      return;
    }

    let cancelled = false;

    async function fetchVideo() {
      try {
        const response = await fetch(`/api/videos/${videoId}`, {
          credentials: 'include',
        });

        if (!response.ok) {
          throw new Error('Failed to load video details');
        }

        const data: VideoDetail = await response.json();

        if (!cancelled) {
          setVideo(data);
        }
      } catch (err) {
        if (!cancelled) {
          setError(err instanceof Error ? err.message : 'An unexpected error occurred');
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    }

    fetchVideo();

    return () => {
      cancelled = true;
    };
  }, [videoId]);

  if (loading) {
    return (
      <div data-testid="page-video-detail">
        <p style={styles.loadingText}>Loading...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div data-testid="page-video-detail">
        <button onClick={() => navigate('/screening')} style={styles.backButton}>
          &larr; Back to Screening Room
        </button>
        <div role="alert" style={styles.error}>{error}</div>
      </div>
    );
  }

  if (!video) {
    return (
      <div data-testid="page-video-detail">
        <button onClick={() => navigate('/screening')} style={styles.backButton}>
          &larr; Back to Screening Room
        </button>
        <p style={styles.emptyText}>Video not found</p>
      </div>
    );
  }

  const hasVersion = video.currentVersionNumber !== null;
  const isCompleted = video.transcodeStatus === 'COMPLETED';
  const showPlayer = hasVersion && isCompleted && video.playbackUrl !== null;
  const showProcessing = hasVersion && !isCompleted;

  return (
    <div data-testid="page-video-detail">
      <button onClick={() => navigate('/screening')} style={styles.backButton}>
        &larr; Back to Screening Room
      </button>

      <div style={styles.header}>
        <h1 style={styles.pageTitle}>{video.title}</h1>
        <div style={styles.badges}>
          {hasVersion && (
            <span style={styles.versionBadge}>V{video.currentVersionNumber}</span>
          )}
          {video.approved && (
            <span style={styles.approvedBadge}>Approved</span>
          )}
        </div>
      </div>

      {video.description && (
        <p style={styles.description}>{video.description}</p>
      )}

      {showPlayer && (
        <video
          controls
          src={video.playbackUrl!}
          width="100%"
          style={{ maxWidth: '800px' }}
          preload="metadata"
        />
      )}

      {showProcessing && (
        <div style={styles.processingMessage}>
          <p style={styles.processingText}>Video is processing...</p>
        </div>
      )}

      {!hasVersion && (
        <div style={styles.noVersionMessage}>
          <p style={styles.noVersionText}>No version available</p>
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
    margin: 0,
  },
  loadingText: {
    color: colors.textMuted,
    fontSize: '0.8rem',
    letterSpacing: '0.1em',
    fontFamily: fonts.body,
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
  backButton: {
    background: 'none',
    border: `1px solid ${colors.border}`,
    color: colors.textMuted,
    fontFamily: fonts.body,
    fontSize: '0.7rem',
    letterSpacing: '0.1em',
    padding: '0.5rem 1rem',
    cursor: 'pointer',
    marginBottom: '1.5rem',
    transition: 'color 0.2s, border-color 0.2s',
  },
  header: {
    display: 'flex',
    alignItems: 'center',
    gap: '1rem',
    marginBottom: '1.5rem',
    flexWrap: 'wrap' as const,
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
  description: {
    color: colors.textMuted,
    fontFamily: fonts.body,
    fontSize: '0.8rem',
    lineHeight: 1.6,
    marginBottom: '1.5rem',
    maxWidth: '800px',
  },
  processingMessage: {
    padding: '2rem',
    border: `1px solid ${colors.border}`,
    borderRadius: '2px',
    maxWidth: '800px',
    textAlign: 'center' as const,
  },
  processingText: {
    color: colors.textMuted,
    fontFamily: fonts.body,
    fontSize: '0.85rem',
    letterSpacing: '0.05em',
    margin: 0,
  },
  noVersionMessage: {
    padding: '2rem',
    border: `1px solid ${colors.border}`,
    borderRadius: '2px',
    maxWidth: '800px',
    textAlign: 'center' as const,
  },
  noVersionText: {
    color: colors.textMuted,
    fontFamily: fonts.body,
    fontSize: '0.85rem',
    letterSpacing: '0.05em',
    margin: 0,
  },
};

export default VideoDetailPage;
