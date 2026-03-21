import { useState, useEffect } from 'react';
import { FiImage, FiType, FiBookOpen, FiCamera } from 'react-icons/fi';
import VaultCategoryTile from '../components/VaultCategoryTile';
import { colors, fonts } from '../theme';

interface CountsData {
  orgCounts: Record<string, number>;
  projectCounts: Record<string, number> | null;
}

const categories = [
  { key: 'logos', label: 'Logos', icon: <FiImage /> },
  { key: 'fonts', label: 'Fonts', icon: <FiType /> },
  { key: 'guidelines', label: 'Guidelines', icon: <FiBookOpen /> },
  { key: 'visuals', label: 'Visuals', icon: <FiCamera /> },
] as const;

function VaultPage() {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [counts, setCounts] = useState<CountsData | null>(null);

  useEffect(() => {
    let cancelled = false;

    async function fetchCounts() {
      try {
        const response = await fetch('/api/brand-assets/counts', {
          credentials: 'include',
        });

        if (!response.ok) {
          throw new Error('Failed to load brand asset counts');
        }

        const data: CountsData = await response.json();

        if (!cancelled) {
          setCounts(data);
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

    fetchCounts();

    return () => {
      cancelled = true;
    };
  }, []);

  if (loading) {
    return (
      <div data-testid="page-vault">
        <style>{`
          .vault-grid {
            display: grid;
            grid-template-columns: repeat(4, 1fr);
            gap: 1.5rem;
          }
          @media (max-width: 768px) {
            .vault-grid {
              grid-template-columns: 1fr;
            }
          }
        `}</style>
        <h1 style={styles.pageTitle}>Brand Vault</h1>
        <p style={styles.loadingText}>Loading...</p>
      </div>
    );
  }

  const orgCounts = counts?.orgCounts ?? {};
  const projectCounts = counts?.projectCounts ?? undefined;

  return (
    <div data-testid="page-vault">
      <style>{`
        .vault-grid {
          display: grid;
          grid-template-columns: repeat(4, 1fr);
          gap: 1.5rem;
        }
        @media (max-width: 768px) {
          .vault-grid {
            grid-template-columns: 1fr;
          }
        }
      `}</style>
      <h1 style={styles.pageTitle}>Brand Vault</h1>
      {error && (
        <div role="alert" style={styles.error}>{error}</div>
      )}
      <div className="vault-grid">
        {categories.map((cat) => (
          <VaultCategoryTile
            key={cat.key}
            icon={cat.icon}
            label={cat.label}
            orgCount={orgCounts[cat.key] ?? 0}
            projectCount={projectCounts ? projectCounts[cat.key] : undefined}
          />
        ))}
      </div>
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
  },
  error: {
    color: colors.error,
    fontSize: '0.75rem',
    marginBottom: '1.5rem',
    padding: '0.75rem 1rem',
    backgroundColor: colors.errorBg,
    borderRadius: '2px',
    letterSpacing: '0.02em',
  },
};

export default VaultPage;
