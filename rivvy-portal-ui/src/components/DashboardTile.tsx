import { useState } from 'react';
import { Link } from 'react-router-dom';
import { colors, fonts } from '../theme';

interface DashboardTileProps {
  label: string;
  description: string;
  to: string;
}

function DashboardTile({ label, description, to }: DashboardTileProps) {
  const [hovered, setHovered] = useState(false);
  const [focused, setFocused] = useState(false);

  const active = hovered || focused;
  const activeStyle = active
    ? { borderColor: colors.orange, boxShadow: `0 4px 20px rgba(193, 85, 58, 0.2)` }
    : {};

  return (
    <Link
      to={to}
      style={{ ...styles.card, ...activeStyle }}
      onMouseEnter={() => setHovered(true)}
      onMouseLeave={() => setHovered(false)}
      onFocus={() => setFocused(true)}
      onBlur={() => setFocused(false)}
    >
      <h2 style={styles.label}>{label}</h2>
      <p style={styles.description}>{description}</p>
    </Link>
  );
}

const styles: Record<string, React.CSSProperties> = {
  card: {
    display: 'block',
    padding: '2rem',
    border: `1px solid ${colors.borderCream}`,
    borderRadius: '2px',
    backgroundColor: colors.cream,
    boxShadow: '0 2px 8px rgba(0, 0, 0, 0.06)',
    textDecoration: 'none',
    color: 'inherit',
    transition: 'border-color 0.3s, box-shadow 0.3s',
  },
  label: {
    marginTop: 0,
    marginBottom: '0.75rem',
    fontSize: '1.1rem',
    fontFamily: fonts.heading,
    fontWeight: 400,
    letterSpacing: '0.05em',
    color: colors.textOnCream,
  },
  description: {
    margin: 0,
    fontSize: '0.75rem',
    color: colors.textOnCreamMuted,
    lineHeight: 1.6,
  },
};

export default DashboardTile;
