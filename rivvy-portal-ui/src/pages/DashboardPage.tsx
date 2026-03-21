import DashboardTile from '../components/DashboardTile';
import RivvyLogo from '../components/RivvyLogo';
import { fonts, colors } from '../theme';

function DashboardPage() {
  return (
    <div data-testid="page-dashboard">
      <style>{`
        .dashboard-grid {
          display: grid;
          grid-template-columns: repeat(3, 1fr);
          gap: 1.5rem;
        }
        @media (max-width: 768px) {
          .dashboard-grid {
            grid-template-columns: 1fr;
          }
        }
      `}</style>
      <div style={styles.heroSection}>
        <RivvyLogo size="large" />
        <p style={styles.subtitle}>Client Portal</p>
      </div>
      <div className="dashboard-grid">
        <DashboardTile
          label="New Brief"
          description="Submit a new creative project request"
          to="/new-brief"
        />
        <DashboardTile
          label="Screening Room"
          description="Review and comment on video deliverables"
          to="/screening"
        />
        <DashboardTile
          label="Brand Vault"
          description="Manage your brand assets and guidelines"
          to="/vault"
        />
      </div>
    </div>
  );
}

const styles: Record<string, React.CSSProperties> = {
  heroSection: {
    textAlign: 'center' as const,
    padding: '3rem 0 3.5rem',
  },
  subtitle: {
    fontFamily: fonts.body,
    fontSize: '0.7rem',
    letterSpacing: '0.4em',
    textTransform: 'uppercase',
    color: colors.textMuted,
    marginTop: '1.5rem',
  },
};

export default DashboardPage;
