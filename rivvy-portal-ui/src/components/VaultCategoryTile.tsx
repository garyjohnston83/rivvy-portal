import { colors, fonts } from '../theme';

interface VaultCategoryTileProps {
  icon: React.ReactNode;
  label: string;
  orgCount: number;
  projectCount?: number;
}

function VaultCategoryTile({ icon, label, orgCount, projectCount }: VaultCategoryTileProps) {
  const ariaLabel = projectCount !== undefined
    ? `${label}: ${orgCount} org assets, ${projectCount} project assets`
    : `${label}: ${orgCount} org assets`;

  return (
    <div style={styles.card} aria-label={ariaLabel}>
      <div style={styles.iconWrapper} aria-hidden="true">
        {icon}
      </div>
      <h3 style={styles.label}>{label}</h3>
      <p style={styles.orgCount}>{orgCount} org assets</p>
      {projectCount !== undefined && (
        <p style={styles.projectCount}>{projectCount} project assets</p>
      )}
    </div>
  );
}

const styles: Record<string, React.CSSProperties> = {
  card: {
    padding: '2rem',
    border: `1px solid ${colors.borderCream}`,
    borderRadius: '2px',
    backgroundColor: colors.cream,
    boxShadow: '0 2px 8px rgba(0, 0, 0, 0.06)',
  },
  iconWrapper: {
    fontSize: '1.75rem',
    marginBottom: '1rem',
    color: colors.orange,
  },
  label: {
    marginTop: 0,
    marginBottom: '0.5rem',
    fontSize: '1rem',
    fontFamily: fonts.heading,
    fontWeight: 400,
    letterSpacing: '0.05em',
    color: colors.textOnCream,
  },
  orgCount: {
    margin: 0,
    fontSize: '1.25rem',
    fontWeight: 700,
    color: colors.textOnCream,
  },
  projectCount: {
    margin: 0,
    marginTop: '0.25rem',
    fontSize: '0.75rem',
    color: colors.textOnCreamMuted,
  },
};

export default VaultCategoryTile;
