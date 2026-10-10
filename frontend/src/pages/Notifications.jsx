import { useCallback, useEffect, useState } from 'react';
import { api } from '../services/api';

export default function Notifications() {
  const [data, setData] = useState(null);
  const [unreadCount, setUnreadCount] = useState(0);
  const [loading, setLoading] = useState(true);
  const [busyId, setBusyId] = useState(null);
  const [error, setError] = useState('');

  const load = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const [notifications, unread] = await Promise.all([
        api.get('/notifications?size=50'),
        api.get('/notifications/unread-count')
      ]);
      setData(notifications.data);
      setUnreadCount(Number(unread.data) || 0);
    } catch (e) {
      setError(e.response?.data?.message || 'Unable to load notifications. Please try again.');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { load(); }, [load]);

  const read = async (id) => {
    setBusyId(id);
    try {
      await api.put(`/notifications/${id}/read`);
      await load();
    } catch (e) {
      setError(e.response?.data?.message || 'Unable to mark this notification as read.');
    } finally {
      setBusyId(null);
    }
  };

  const all = async () => {
    setBusyId('all');
    try {
      await api.put('/notifications/read-all');
      await load();
    } catch (e) {
      setError(e.response?.data?.message || 'Unable to mark notifications as read.');
    } finally {
      setBusyId(null);
    }
  };

  return <main className="page">
    <div className="page-head">
      <div><span className="eyebrow">ACCOUNT</span><h1>Notifications</h1><p className="muted">{unreadCount} unread notification{unreadCount === 1 ? '' : 's'}</p></div>
      <button className="btn secondary" onClick={all} disabled={loading || busyId !== null || unreadCount === 0}>Mark all read</button>
    </div>
    {error && <div className="alert error" role="alert">{error} <button className="btn secondary" onClick={load}>Retry</button></div>}
    {loading && <div className="card empty">Loading notifications…</div>}
    {!loading && data?.content?.length === 0 && <div className="card empty"><h3>You're all caught up</h3><p>No notifications yet.</p></div>}
    {!loading && data?.content?.map(n => <article className={`notice-card ${n.read ? 'read' : ''}`} key={n.id}>
      <div><strong>{n.title}</strong><p>{n.message}</p><small>{new Date(n.createdAt).toLocaleString()} · {n.type}</small></div>
      {!n.read && <button className="btn secondary" disabled={busyId !== null} onClick={() => read(n.id)}>{busyId === n.id ? 'Saving…' : 'Mark read'}</button>}
    </article>)}
  </main>;
}
