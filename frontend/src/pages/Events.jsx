import { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { api } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { isAdmin, statusClass, statusLabel } from '../services/format';

const publicStatuses = ['', 'PUBLISHED', 'REGISTRATION_OPEN', 'REGISTRATION_CLOSED', 'ONGOING', 'COMPLETED', 'CANCELLED'];

export default function Events() {
  const { user } = useAuth();
  const [params, setParams] = useSearchParams();
  const [events, setEvents] = useState(null);
  const [categories, setCategories] = useState([]);
  const [venues, setVenues] = useState([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  const [retryKey, setRetryKey] = useState(0);
  const q = params.get('q') || '';
  const categoryId = params.get('categoryId') || '';
  const venueId = params.get('venueId') || '';
  const status = params.get('status') || '';
  const fromDate = params.get('fromDate') || '';
  const sort = params.get('sort') || 'newest';
  const page = Number(params.get('page') || 0);
  const canSeeDrafts = ['ORGANIZER', 'ADMIN', 'SUPER_ADMIN'].includes(user?.role);
  const statuses = canSeeDrafts ? ['', 'DRAFT', ...publicStatuses.slice(1)] : publicStatuses;

  useEffect(() => {
    Promise.all([api.get('/categories'), api.get('/venues')]).then(([c, v]) => {
      setCategories(c.data); setVenues(v.data);
    }).catch(() => setError('Unable to load event filters.'));
  }, []);

  // Re-fetches whenever the filters change or the signed-in user changes (drafts appear for admins/organizers).
  useEffect(() => {
    const query = new URLSearchParams({ page, size: 9, sortBy: sort === 'soonest' ? 'date' : 'createdAt', direction: sort === 'soonest' ? 'asc' : 'desc' });
    if (q) query.set('q', q);
    if (categoryId) query.set('categoryId', categoryId);
    if (venueId) query.set('venueId', venueId);
    if (status) query.set('status', status);
    if (fromDate) query.set('fromDate', fromDate);
    setError('');
    setLoading(true);
    api.get(`/events?${query}`).then(({ data }) => {
      setEvents(data);
    }).catch((err) => {
      setError(err.response?.data?.message || `Unable to load events (HTTP ${err.response?.status || 'network error'}).`);
      setEvents(null);
    }).finally(() => setLoading(false));
  }, [q, categoryId, venueId, status, fromDate, sort, page, user?.id, user?.role, retryKey]);

  const setFilter = (key, value) => {
    const next = new URLSearchParams(params);
    value ? next.set(key, value) : next.delete(key);
    if (key !== 'page') next.delete('page');
    setParams(next);
  };

  return <main className="dashboard">
    <header>
      <div>
        <span className="eyebrow">EVENTHUB</span>
        <h1>Discover events</h1>
        <p className="muted">Search workshops, hackathons, seminars and more.{canSeeDrafts && ' Drafts you can manage are included and marked DRAFT.'}</p>
      </div>
      {canSeeDrafts && <Link className="btn" to="/organizer/events/new">＋ Create event</Link>}
    </header>
    <section className="card filters">
      <input value={q} onChange={e => setFilter('q', e.target.value)} placeholder="Search title or description…" />
      <select value={categoryId} onChange={e => setFilter('categoryId', e.target.value)}><option value="">All categories</option>{categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}</select>
      <select value={venueId} onChange={e => setFilter('venueId', e.target.value)}><option value="">All venues</option>{venues.map(v => <option key={v.id} value={v.id}>{v.name} · {v.city}</option>)}</select>
      <select value={status} onChange={e => setFilter('status', e.target.value)}>{statuses.map(s => <option key={s} value={s}>{s ? statusLabel(s) : 'All statuses'}</option>)}</select>
      <select value={sort} onChange={e => setFilter('sort', e.target.value)}><option value="newest">Newest first</option><option value="soonest">Soonest event date</option></select>
    </section>
    {fromDate && <div className="alert">Showing upcoming events only. <button className="mini secondary" onClick={() => setFilter('fromDate', '')}>Clear</button></div>}
    {error && <div className="error">{error}</div>}
    {loading && !events ? <div className="card empty">Loading events…</div> : !events ? <div className="card empty"><p>Events could not be loaded.</p><button className="secondary" onClick={() => setRetryKey(key => key + 1)}>Retry</button></div> : (events.content || []).length === 0 ? <div className="card empty"><h3>No events found</h3><p>Try changing your search or filters.</p></div> : <section className="event-grid">{events.content.map(e => <article className="card event-card" key={e.id}>
      <div className="event-cover">{e.bannerImage ? <img src={e.bannerImage} alt="" /> : <span>{e.category.name}</span>}</div>
      <div className="event-body">
        <span className={`badge ${statusClass(e.status)}`}>{statusLabel(e.status)}</span>
        <h2>{e.title}</h2>
        <p className="muted">{e.eventDate} · {e.startTime}–{e.endTime}</p>
        <p>{e.venue.name}, {e.venue.city}</p>
        <p className="muted">{e.availableSeats} seats available</p>
        <Link className="button-link" to={`/events/${e.id}`}>View event</Link>
      </div>
    </article>)}</section>}
    {events && events.totalPages > 1 && <div className="pagination"><button className="secondary" disabled={events.first} onClick={() => setFilter('page', page - 1)}>Previous</button><span>Page {events.number + 1} of {events.totalPages}</span><button className="secondary" disabled={events.last} onClick={() => setFilter('page', page + 1)}>Next</button></div>}
  </main>;
}
