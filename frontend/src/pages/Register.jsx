import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { api } from '../services/api';
import { errorMessage } from '../services/errors';
import { useToast } from '../context/ToastContext';

export default function Register() {
  const navigate = useNavigate();
  const toast = useToast();
  const [form, setForm] = useState({ firstName:'', lastName:'', email:'', phone:'', password:'' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const submit = async (e) => {
    e.preventDefault();
    if (loading) return;
    setError('');
    setLoading(true);
    try {
      await api.post('/auth/register', form);
      toast.success('Registration successful! Please sign in with your new account.');
      navigate('/login', { state: { email: form.email } });
    } catch (err) {
      const message = err.response?.status === 401 ? 'Registration failed. Please try again.' : errorMessage(err, 'Registration failed');
      setError(message);
      toast.error(message);
    } finally {
      setLoading(false);
    }
  };
  const update = e => setForm({...form, [e.target.name]: e.target.value});

  return <main className="auth-shell"><form className="card auth-card" onSubmit={submit}>
    <span className="eyebrow">EVENTHUB</span><h1>Create account</h1><p>Start with a Student account. Role elevation will be controlled by the backend.</p>
    {error && <div className="error" role="alert">{error}</div>}
    <div className="grid2"><label>First name<input name="firstName" required value={form.firstName} onChange={update}/></label><label>Last name<input name="lastName" required value={form.lastName} onChange={update}/></label></div>
    <label>Email<input name="email" type="email" required value={form.email} onChange={update}/></label>
    <label>Phone<input name="phone" value={form.phone} onChange={update}/></label>
    <label>Password<input name="password" type="password" minLength="8" maxLength="72" pattern="(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[^A-Za-z0-9]).{8,72}" title="8-72 characters with an uppercase letter, a lowercase letter, a number and a special character (e.g. Ashok@1234)" required value={form.password} onChange={update}/></label>
    <small className="field-hint">8+ characters with an uppercase letter, a lowercase letter, a number and a special character.</small>
    <button disabled={loading}>{loading ? <><span className="spinner" aria-hidden="true"/> Creating account…</> : 'Create account'}</button>
    <p className="muted">Already registered? <Link to="/login">Sign in</Link></p>
  </form></main>;
}
