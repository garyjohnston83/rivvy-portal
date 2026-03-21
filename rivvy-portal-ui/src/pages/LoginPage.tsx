import { useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import RivvyLogo from '../components/RivvyLogo';
import { colors, fonts } from '../theme';

function LoginPage() {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [rememberMe, setRememberMe] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const response = await fetch('/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({ email, password, rememberMe }),
      });

      if (response.ok) {
        const data = await response.json();
        navigate(data.redirectUrl);
      } else if (response.status === 401) {
        const data = await response.json();
        setError(data.error || 'Invalid email or password');
      } else {
        setError('An unexpected error occurred. Please try again.');
      }
    } catch {
      setError('Unable to connect. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div data-testid="page-login" style={styles.container}>
      <div style={styles.inner}>
        <div style={styles.logoSection}>
          <RivvyLogo size="large" />
        </div>
        <div style={styles.card}>
          <h1 style={styles.title}>Sign in</h1>
          <form onSubmit={handleSubmit}>
            <div style={styles.field}>
              <label htmlFor="email" style={styles.label}>Email</label>
              <input
                id="email"
                type="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                disabled={loading}
                style={styles.input}
              />
            </div>
            <div style={styles.field}>
              <label htmlFor="password" style={styles.label}>Password</label>
              <input
                id="password"
                type="password"
                required
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                disabled={loading}
                style={styles.input}
              />
            </div>
            <div style={styles.checkboxField}>
              <input
                id="rememberMe"
                type="checkbox"
                checked={rememberMe}
                onChange={(e) => setRememberMe(e.target.checked)}
                disabled={loading}
              />
              <label htmlFor="rememberMe" style={styles.checkboxLabel}>Remember me</label>
            </div>
            {error && (
              <div role="alert" style={styles.error}>{error}</div>
            )}
            <button
              type="submit"
              disabled={loading}
              style={{
                ...styles.button,
                ...(loading ? styles.buttonDisabled : {}),
              }}
            >
              {loading ? 'Signing in...' : 'Sign in'}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}

const styles: Record<string, React.CSSProperties> = {
  container: {
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    minHeight: '100vh',
    padding: '2rem',
  },
  inner: {
    width: '100%',
    maxWidth: '400px',
  },
  logoSection: {
    marginBottom: '3rem',
  },
  card: {
    width: '100%',
    padding: '2.5rem',
    backgroundColor: colors.cream,
    borderRadius: '2px',
    border: `1px solid ${colors.borderCream}`,
  },
  title: {
    marginTop: 0,
    marginBottom: '2rem',
    fontSize: '1.5rem',
    fontFamily: fonts.heading,
    fontWeight: 400,
    textAlign: 'center' as const,
    color: colors.textOnCream,
    letterSpacing: '0.05em',
  },
  field: {
    marginBottom: '1.25rem',
  },
  label: {
    display: 'block',
    marginBottom: '0.4rem',
    fontWeight: 700,
    fontSize: '0.7rem',
    letterSpacing: '0.15em',
    textTransform: 'uppercase' as const,
    color: colors.textOnCreamMuted,
  },
  input: {
    width: '100%',
    padding: '0.65rem 0.75rem',
    fontSize: '0.85rem',
    fontFamily: fonts.body,
    border: `1px solid rgba(10, 10, 10, 0.15)`,
    borderRadius: '2px',
    boxSizing: 'border-box' as const,
    backgroundColor: 'rgba(255, 255, 255, 0.6)',
    color: colors.textOnCream,
    outline: 'none',
  },
  checkboxField: {
    display: 'flex',
    alignItems: 'center',
    gap: '0.5rem',
    marginBottom: '1.25rem',
  },
  checkboxLabel: {
    fontSize: '0.75rem',
    color: colors.textOnCreamMuted,
    letterSpacing: '0.05em',
  },
  error: {
    color: colors.error,
    fontSize: '0.75rem',
    marginBottom: '1rem',
    padding: '0.6rem 0.75rem',
    backgroundColor: colors.errorBg,
    borderRadius: '2px',
    letterSpacing: '0.02em',
  },
  button: {
    width: '100%',
    padding: '0.75rem',
    fontSize: '0.75rem',
    fontWeight: 700,
    fontFamily: fonts.body,
    letterSpacing: '0.15em',
    textTransform: 'uppercase' as const,
    color: colors.cream,
    backgroundColor: colors.orange,
    border: 'none',
    borderRadius: '2px',
    cursor: 'pointer',
    transition: 'opacity 0.2s',
  },
  buttonDisabled: {
    opacity: 0.5,
    cursor: 'not-allowed',
  },
};

export default LoginPage;
