import { useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { api } from '../services/api';
import { errorMessage } from '../services/errors';
import { useToast } from '../context/ToastContext';

export default function ResetPassword() {
  const [params] = useSearchParams();
  const navigate = useNavigate();
  const toast = useToast();
  const token = params.get('token') || '';
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const submit = async (e) => {
    e.preventDefault();
    if (loading) return;
    setError('');
    if (!token) {
      setError('This password reset link is missing or invalid. Request a new link.');
      return;
    }
    if (password !== confirmPassword) {
      setError('The passwords do not match.');
      return;
    }
    setLoading(true);
    try {
      const { data } = await api.post('/auth/reset-password', { token, password });
      toast.success(data.message || 'Password updated. Please sign in.');
      navigate('/login', { replace: true });
    } catch (err) {
      setError(errorMessage(err, 'Unable to reset password. The link may have expired.'));
    } finally {
      setLoading(false);
    }
  };

  return <main className="auth-shell"><form className="card auth-card" onSubmit={submit}>
    <span className="eyebrow">EVENTHUB</span>
    <h1>Set a new password</h1>
    <p>Choose a new password for your account.</p>
    {error && <div className="error" role="alert">{error}</div>}
    {!token && <div className="alert" role="status">This link has no reset token. Request a new password reset link.</div>}
    <label>New password<input type="password" autoComplete="new-password" required minLength="8" maxLength="72" pattern="(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[^A-Za-z0-9]).{8,72}" title="8–72 characters with uppercase, lowercase, number, and special character" value={password} onChange={e => setPassword(e.target.value)} /></label>
    <label>Confirm new password<input type="password" autoComplete="new-password" required minLength="8" maxLength="72" value={confirmPassword} onChange={e => setConfirmPassword(e.target.value)} /></label>
    <small className="field-hint">Use 8–72 characters with an uppercase letter, lowercase letter, number, and special character.</small>
    <button disabled={loading || !token}>{loading ? <><span className="spinner" aria-hidden="true"/> Updating…</> : 'Reset password'}</button>
    <p className="muted"><Link to="/forgot-password">Request a new reset link</Link> · <Link to="/login">Sign in</Link></p>
  </form></main>;
}
