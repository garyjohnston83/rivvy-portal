import { Link, Outlet, useNavigate } from 'react-router-dom';
import { colors } from '../theme';

function RootLayout() {
  const navigate = useNavigate();

  const handleSignOut = async () => {
    try {
      await fetch('/api/auth/logout', { method: 'POST', credentials: 'include' });
    } catch {
      // proceed regardless
    }
    navigate('/login');
  };

  return (
    <div style={styles.wrapper}>
      <header style={styles.header}>
        <Link to="/dashboard" style={styles.brandLink}>
          <img src="/rivvy_studios_logo.png" alt="Rivvy Studios" style={styles.brandLogo} />
        </Link>
        <button onClick={handleSignOut} style={styles.signOut}>Sign out</button>
      </header>
      <main style={styles.main}>
        <Outlet />
      </main>
      <footer style={styles.footer}>
        <span style={styles.footerText}>RIVVY STUDIOS</span>
      </footer>
    </div>
  );
}

const styles: Record<string, React.CSSProperties> = {
  wrapper: {
    maxWidth: '1100px',
    margin: '0 auto',
    padding: '0 2rem',
    minHeight: '100vh',
    display: 'flex',
    flexDirection: 'column',
  },
  header: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    borderBottom: `1px solid ${colors.border}`,
    padding: '1.5rem 0',
  },
  brandLink: {
    textDecoration: 'none',
    display: 'flex',
    alignItems: 'center',
  },
  brandLogo: {
    width: '80px',
    height: 'auto',
  },
  signOut: {
    background: 'none',
    border: `1px solid ${colors.border}`,
    color: colors.textMuted,
    fontFamily: "'Space Mono', monospace",
    fontSize: '0.7rem',
    letterSpacing: '0.1em',
    textTransform: 'uppercase',
    padding: '0.5rem 1rem',
    cursor: 'pointer',
    transition: 'color 0.2s, border-color 0.2s',
  },
  main: {
    flex: 1,
    paddingTop: '2rem',
    paddingBottom: '2rem',
  },
  footer: {
    borderTop: `1px solid ${colors.border}`,
    padding: '1.5rem 0',
    textAlign: 'center' as const,
  },
  footerText: {
    fontFamily: "'Space Mono', monospace",
    fontSize: '0.6rem',
    letterSpacing: '0.4em',
    color: colors.textMuted,
  },
};

export default RootLayout;
