import { useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../services/api';
import { errorMessage } from '../services/errors';

export default function ForgotPassword() {
  const [email, setEmail] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);
  const [loading, setLoading] = useState(false);

  const submit = async (e) => {
    e.preventDefault();
    if (loading) return;
    setError('');
    setLoading(true);
    try {
      await api.post('/auth/forgot-password', { email });
      setSuccess(true);
    } catch (err) {
      setError(errorMessage(err, 'Unable to process the request. Please try again.'));
    } finally {
      setLoading(false);
    }
  };

  return <main className="auth-shell"><form className="card auth-card" onSubmit={submit}>
    <span className="eyebrow">EVENTHUB</span>
    <h1>Forgot password?</h1>
    <p>Enter the email address linked to your account and we’ll send a password reset link.</p>
    {error && <div className="error" role="alert">{error}</div>}
    {success ? <div className="success" role="status">If an account exists for that email, password reset instructions will be sent shortly. Check your inbox and spam folder.</div> : <>
      <label>Email<input type="email" autoComplete="email" required maxLength="190" value={email} onChange={e => setEmail(e.target.value)} /></label>
      <button disabled={loading}>{loading ? <><span className="spinner" aria-hidden="true"/> Sending…</> : 'Send reset link'}</button>
    </>}
    <p className="muted"><Link to="/login">Back to sign in</Link></p>
  </form></main>;
}
