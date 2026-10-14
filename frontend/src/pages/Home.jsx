import { useEffect, useState } from 'react';
import { api } from '../api';
import { useAuth } from '../auth/AuthContext';

const ROLE_HINT = {
  ADMIN: 'Manage students, faculty, courses and notices.',
  FACULTY: 'Mark attendance, upload marks and track students at risk.',
  STUDENT: 'Check your attendance, results and ask the AI assistant.',
};

export default function Home() {
  const { user } = useAuth();
  const [notices, setNotices] = useState([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;
    api('/api/notices?size=5')
      .then((page) => {
        if (!cancelled) setNotices(page.content);
      })
      .catch((err) => {
        if (!cancelled) setError(err.message);
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });
    return () => {
      cancelled = true;
    };
  }, []);

  return (
    <>
      <div className="page-header">
        <div>
          <h1>Welcome, {user.fullName}</h1>
          <p className="muted">{ROLE_HINT[user.role]}</p>
        </div>
        <span className="badge badge-info">{user.role}</span>
      </div>

      <section className="card">
        <h2>Latest notices</h2>
        {loading && <p className="muted">Loading...</p>}
        {error && <div className="alert alert-error">{error}</div>}
        {!loading && !error && notices.length === 0 && <p className="muted">No notices yet.</p>}

        <ul className="notice-list">
          {notices.map((n) => (
            <li key={n.id}>
              <div className="notice-title">
                {n.title} <span className="badge">{n.audience}</span>
              </div>
              <p>{n.content}</p>
              <small className="muted">
                {n.postedBy} · {new Date(n.createdAt).toLocaleString()}
              </small>
            </li>
          ))}
        </ul>
      </section>
    </>
  );
}