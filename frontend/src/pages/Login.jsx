import { useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { errorMessage } from '../services/errors';

export default function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const toast = useToast();
  const [form, setForm] = useState({ email: location.state?.email || '', password: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const submit = async (e) => {
    e.preventDefault();
    if (loading) return;
    setError('');
    setLoading(true);
    try {
      const user = await login(form.email, form.password);
      toast.success(`Login successful. Welcome back, ${user?.firstName || 'there'}!`);
      navigate('/');
    } catch (err) {
      const message = errorMessage(err, 'Unable to sign in');
      setError(message);
      toast.error(message);
      setLoading(false);
    }
  };

  return <main className="auth-shell"><form className="card auth-card" onSubmit={submit}>
    <span className="eyebrow">EVENTHUB</span><h1>Welcome back</h1><p>Sign in to your event workspace.</p>
    {error && <div className="error" role="alert">{error}</div>}
    <label>Email<input type="email" required value={form.email} onChange={e=>setForm({...form,email:e.target.value})}/></label>
    <label>Password<input type="password" required value={form.password} onChange={e=>setForm({...form,password:e.target.value})}/></label>
    <p className="auth-form-actions"><Link to="/forgot-password">Forgot password?</Link></p>
    <button disabled={loading}>{loading ? <><span className="spinner" aria-hidden="true"/> Signing in…</> : 'Sign in'}</button>
    <p className="muted">New here? <Link to="/register">Create an account</Link></p>
  </form></main>;
}
