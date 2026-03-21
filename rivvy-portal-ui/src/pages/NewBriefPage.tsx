import { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { colors, fonts } from '../theme';

interface FormData {
  title: string;
  description: string;
  priority: string;
  desiredDueDate: string;
  budget: string;
  creativeDirection: string;
}

function NewBriefPage() {
  const navigate = useNavigate();
  const [briefId, setBriefId] = useState<string | null>(null);
  const [formData, setFormData] = useState<FormData>({
    title: '',
    description: '',
    priority: 'NORMAL',
    desiredDueDate: '',
    budget: '',
    creativeDirection: '',
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [saveStatus, setSaveStatus] = useState<'' | 'saving' | 'saved' | 'error'>('');

  // Refs for debounced autosave
  const timerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const isInitialPopulateRef = useRef(true);

  // Auto-create draft on mount
  useEffect(() => {
    let cancelled = false;

    async function createDraft() {
      try {
        const response = await fetch('/api/briefs', {
          method: 'POST',
          credentials: 'include',
        });

        if (!response.ok) {
          if (!cancelled) {
            setError('Failed to create brief. Please try again.');
            setLoading(false);
          }
          return;
        }

        const data = await response.json();

        if (!cancelled) {
          setBriefId(data.id);
          setFormData({
            title: data.title || '',
            description: data.description || '',
            priority: data.priority || 'NORMAL',
            desiredDueDate: data.desiredDueDate || '',
            budget: data.budget != null ? String(data.budget) : '',
            creativeDirection: data.creativeDirection || '',
          });
          setLoading(false);
        }
      } catch {
        if (!cancelled) {
          setError('Failed to create brief. Please try again.');
          setLoading(false);
        }
      }
    }

    createDraft();

    return () => {
      cancelled = true;
    };
  }, []);

  // Debounced autosave
  useEffect(() => {
    if (briefId === null) {
      return;
    }

    // Skip the initial trigger when form is first populated from the POST response
    if (isInitialPopulateRef.current) {
      isInitialPopulateRef.current = false;
      return;
    }

    if (timerRef.current !== null) {
      clearTimeout(timerRef.current);
    }

    timerRef.current = setTimeout(() => {
      performAutosave();
    }, 1500);

    return () => {
      if (timerRef.current !== null) {
        clearTimeout(timerRef.current);
      }
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [briefId, formData.title, formData.description, formData.priority, formData.desiredDueDate, formData.budget, formData.creativeDirection]);

  async function performAutosave() {
    if (briefId === null) {
      return;
    }

    setSaveStatus('saving');

    const requestBody: Record<string, unknown> = {
      title: formData.title,
      description: formData.description,
      priority: formData.priority,
      desiredDueDate: formData.desiredDueDate || null,
      budget: formData.budget ? Number(formData.budget) : null,
      creativeDirection: formData.creativeDirection,
    };

    try {
      const response = await fetch(`/api/briefs/${briefId}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify(requestBody),
      });

      if (response.ok) {
        setSaveStatus('saved');
      } else {
        setSaveStatus('error');
      }
    } catch {
      setSaveStatus('error');
    }
  }

  function handleFieldChange(field: keyof FormData, value: string) {
    setFormData((prev) => ({ ...prev, [field]: value }));
  }

  async function handleCancel() {
    if (briefId === null) {
      return;
    }

    try {
      await fetch(`/api/briefs/${briefId}`, {
        method: 'DELETE',
        credentials: 'include',
      });
    } catch {
      // Draft may be orphaned -- acceptable per spec
    }

    navigate('/dashboard');
  }

  if (loading) {
    return (
      <div data-testid="page-new-brief" style={styles.container}>
        <div style={styles.card}>
          <p style={styles.loadingText}>Creating brief...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div data-testid="page-new-brief" style={styles.container}>
        <div style={styles.card}>
          <div role="alert" style={styles.error}>{error}</div>
        </div>
      </div>
    );
  }

  return (
    <div data-testid="page-new-brief" style={styles.container}>
      <div style={styles.card}>
        <h1 style={styles.title}>New Brief</h1>

        {saveStatus === 'saving' && <p style={styles.saveIndicator}>Saving...</p>}
        {saveStatus === 'saved' && <p style={styles.saveIndicator}>Saved</p>}
        {saveStatus === 'error' && <p style={styles.saveIndicatorError}>Save failed</p>}

        <div style={styles.field}>
          <label htmlFor="title" style={styles.label}>Title</label>
          <input
            id="title"
            type="text"
            placeholder="Untitled Brief"
            value={formData.title}
            onChange={(e) => handleFieldChange('title', e.target.value)}
            style={styles.input}
          />
        </div>

        <div style={styles.field}>
          <label htmlFor="description" style={styles.label}>Description</label>
          <textarea
            id="description"
            placeholder="Describe your project..."
            value={formData.description}
            onChange={(e) => handleFieldChange('description', e.target.value)}
            style={styles.textarea}
          />
        </div>

        <div style={styles.field}>
          <label htmlFor="priority" style={styles.label}>Priority</label>
          <select
            id="priority"
            value={formData.priority}
            onChange={(e) => handleFieldChange('priority', e.target.value)}
            style={styles.select}
          >
            <option value="NORMAL">Normal</option>
            <option value="HIGH">High</option>
            <option value="URGENT">Urgent</option>
          </select>
        </div>

        <div style={styles.field}>
          <label htmlFor="desiredDueDate" style={styles.label}>Desired Due Date</label>
          <input
            id="desiredDueDate"
            type="date"
            value={formData.desiredDueDate}
            onChange={(e) => handleFieldChange('desiredDueDate', e.target.value)}
            style={styles.input}
          />
        </div>

        <div style={styles.field}>
          <label htmlFor="budget" style={styles.label}>Budget</label>
          <input
            id="budget"
            type="number"
            step="0.01"
            min="0"
            value={formData.budget}
            onChange={(e) => handleFieldChange('budget', e.target.value)}
            style={styles.input}
          />
        </div>

        <div style={styles.field}>
          <label htmlFor="creativeDirection" style={styles.label}>Creative Direction</label>
          <textarea
            id="creativeDirection"
            placeholder="Describe the creative vision..."
            value={formData.creativeDirection}
            onChange={(e) => handleFieldChange('creativeDirection', e.target.value)}
            style={styles.textarea}
          />
        </div>

        <button
          type="button"
          onClick={handleCancel}
          style={styles.cancelButton}
        >
          Cancel
        </button>
      </div>
    </div>
  );
}

const styles: Record<string, React.CSSProperties> = {
  container: {
    display: 'flex',
    justifyContent: 'center',
    paddingTop: '1rem',
    paddingBottom: '2rem',
  },
  card: {
    width: '100%',
    maxWidth: '640px',
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
    border: '1px solid rgba(10, 10, 10, 0.15)',
    borderRadius: '2px',
    boxSizing: 'border-box' as const,
    backgroundColor: 'rgba(255, 255, 255, 0.6)',
    color: colors.textOnCream,
    outline: 'none',
  },
  textarea: {
    width: '100%',
    padding: '0.65rem 0.75rem',
    fontSize: '0.85rem',
    fontFamily: fonts.body,
    border: '1px solid rgba(10, 10, 10, 0.15)',
    borderRadius: '2px',
    boxSizing: 'border-box' as const,
    backgroundColor: 'rgba(255, 255, 255, 0.6)',
    color: colors.textOnCream,
    outline: 'none',
    minHeight: '80px',
    resize: 'vertical' as const,
  },
  select: {
    width: '100%',
    padding: '0.65rem 0.75rem',
    fontSize: '0.85rem',
    fontFamily: fonts.body,
    border: '1px solid rgba(10, 10, 10, 0.15)',
    borderRadius: '2px',
    boxSizing: 'border-box' as const,
    backgroundColor: 'rgba(255, 255, 255, 0.6)',
    color: colors.textOnCream,
    outline: 'none',
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
  loadingText: {
    textAlign: 'center' as const,
    color: colors.textOnCreamMuted,
    fontSize: '0.8rem',
    letterSpacing: '0.1em',
  },
  cancelButton: {
    width: '100%',
    padding: '0.75rem',
    fontSize: '0.75rem',
    fontWeight: 700,
    fontFamily: fonts.body,
    letterSpacing: '0.15em',
    textTransform: 'uppercase' as const,
    color: colors.textOnCreamMuted,
    backgroundColor: 'transparent',
    border: `1px solid rgba(10, 10, 10, 0.2)`,
    borderRadius: '2px',
    cursor: 'pointer',
    marginTop: '0.5rem',
    transition: 'border-color 0.2s',
  },
  saveIndicator: {
    fontSize: '0.7rem',
    color: colors.textOnCreamMuted,
    textAlign: 'center' as const,
    marginBottom: '1rem',
    marginTop: 0,
    letterSpacing: '0.1em',
  },
  saveIndicatorError: {
    fontSize: '0.7rem',
    color: colors.error,
    textAlign: 'center' as const,
    marginBottom: '1rem',
    marginTop: 0,
    letterSpacing: '0.1em',
  },
};

export default NewBriefPage;
