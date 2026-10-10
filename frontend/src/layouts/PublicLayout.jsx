import { Link, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import AppShell from './AppShell';

// Pages such as /events and /notices are public. Signed-in users get the full app shell
// (sidebar, topbar); visitors get a simple top navigation bar.
export default function PublicLayout() {
  const { user, loading } = useAuth();
  if (loading) return <div className="page-center">Loading EventHub…</div>;
  if (user) return <AppShell />;
  return <div className="public-shell">
    <nav className="public-nav">
      <Link to="/events" className="brand"><div className="brand-mark">E</div><div><strong>EventHub</strong><span>Campus events</span></div></Link>
      <Link className="plain" to="/events">Events</Link>
      <Link className="plain" to="/notices">Notices</Link>
      <Link className="btn secondary" to="/login">Sign in</Link>
      <Link className="btn" to="/register">Create account</Link>
    </nav>
    <div className="content"><Outlet /></div>
  </div>;
}
