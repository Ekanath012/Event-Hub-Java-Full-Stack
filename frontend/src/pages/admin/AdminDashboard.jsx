import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../../services/api';
import { today } from '../../services/format';

export default function AdminDashboard() {
  const [summary, setSummary] = useState(null);
  const [recent, setRecent] = useState(null);
  const [summaryError, setSummaryError] = useState('');
  const [recentError, setRecentError] = useState('');
  const [retryKey, setRetryKey] = useState(0);

  useEffect(() => {
    let active = true;
    setSummaryError('');
    setRecentError('');
    setSummary(null);
    setRecent(null);

    api.get('/analytics/summary')
      .then((response) => { if (active) setSummary(response.data); })
      .catch(() => { if (active) setSummaryError('Dashboard statistics could not be loaded. Check that the backend is running and try again.'); });

    api.get('/events?size=5&sortBy=createdAt&direction=desc')
      .then((response) => {
        if (active) {
          const data = response.data;
          setRecent({ ...data, content: Array.isArray(data?.content) ? data.content : [] });
        }
      })
      .catch(() => { if (active) setRecentError('Recent events could not be loaded. Check the event API and your permissions, then retry.'); });

    return () => { active = false; };
  }, [retryKey]);

  const metrics = [
    ['Events', summary?.totalEvents, '/events', 'All events'],
    ['Upcoming', summary?.upcomingEvents, `/events?fromDate=${today()}&sort=soonest`, 'Upcoming events'],
    ['Completed', summary?.completedEvents, '/events?status=COMPLETED', 'Completed events'],
    ['Registrations', summary?.totalRegistrations, '/organizer/events', 'Manage events'],
    ['Attendance', summary?.totalAttendance, '/organizer/events', 'Manage events'],
    ['Waitlist', summary?.waitlistCount, '/organizer/events', 'Manage events'],
    ['Cancellations', summary?.cancellationCount, '/events?status=CANCELLED', 'Cancelled events'],
  ];

  const tiles = [
    ['📅', 'Manage events', 'Publish, open registration, cancel – all events including drafts', '/organizer/events'],
    ['➕', 'Create event', 'Add a new workshop, seminar or hackathon', '/organizer/events/new'],
    ['🔎', 'Event discovery', 'See the events page exactly as students do', '/events'],
    ['📝', 'Notices', 'Publish and manage announcements', '/admin/notices'],
    ['🏛️', 'Catalog', 'Categories and venues', '/admin/catalog'],
    ['🛡️', 'Audit logs', 'Review security and business activity', '/admin/audit'],
  ];

  return (
    <div>
      <div className="page-head">
        <div>
          <span className="eyebrow">ADMINISTRATION</span>
          <h1>Control center</h1>
          <p className="muted">Monitor the platform and jump straight to the page you need.</p>
        </div>
        <Link className="btn" to="/organizer/events/new">＋ Create event</Link>
      </div>

      {summaryError && <div className="error" role="alert">{summaryError} <button className="btn" type="button" onClick={() => setRetryKey((key) => key + 1)}>Retry</button></div>}
      <section className="metric-grid">
        {metrics.map(([label, value, to, hint]) => (
          <Link className="metric" to={to} key={label} title={`Open: ${hint}`}>
            <span>{label}</span><strong>{value ?? '—'}</strong>
          </Link>
        ))}
      </section>

      <section className="quick-grid">
        {tiles.map(([icon, title, description, to]) => (
          <Link className="action-card" to={to} key={title}>
            <span className="tile-ico">{icon}</span><strong>{title}</strong><span>{description}</span>
          </Link>
        ))}
      </section>

      <section className="panel" style={{ marginTop: 22 }}>
        <div className="section-head">
          <div><span className="eyebrow">LATEST</span><h2>Recently created events</h2></div>
          <Link to="/organizer/events">Manage all</Link>
        </div>
        {recentError ? (
          <div className="error" role="alert">{recentError} <button className="btn" type="button" onClick={() => setRetryKey((key) => key + 1)}>Retry</button></div>
        ) : !recent ? (
          <div className="empty">Loading…</div>
        ) : !recent.content.length ? (
          <div className="empty">No events found. <Link to="/organizer/events/new">Create the first one</Link>.</div>
        ) : recent.content.map((event) => (
          <Link className="list-row" key={event.id} to={`/events/${event.id}`} style={{ color: 'inherit', textDecoration: 'none' }}>
            <div><strong>{event.title}</strong><span>{event.eventDate} · {event.venue?.name}</span></div>
            <span className={`status-pill status-${String(event.status || '').toLowerCase()}`}>{String(event.status || 'UNKNOWN').replace(/_/g, ' ')}</span>
          </Link>
        ))}
      </section>
    </div>
  );
}
