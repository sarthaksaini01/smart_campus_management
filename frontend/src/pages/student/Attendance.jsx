import { useState } from 'react';
import Modal from '../../components/Modal';
import ProgressBar from '../../components/ProgressBar';
import StatCard from '../../components/StatCard';
import { useFetch } from '../../hooks/useFetch';
import { MIN_ATTENDANCE, adviceText, attendanceTone } from '../../utils/attendance';

function formatDate(iso) {
  return new Date(`${iso}T00:00:00`).toLocaleDateString(undefined, {
    weekday: 'short',
    day: 'numeric',
    month: 'short',
    year: 'numeric',
  });
}

function Records({ subject }) {
  const { data, loading, error } = useFetch(`/api/student/attendance/${subject.subjectId}`);
  const rows = [...(data ?? [])].reverse();

  return (
    <>
      <p className="muted">
        {subject.present} present out of {subject.totalClasses} classes
      </p>
      {error && <div className="alert alert-error">{error}</div>}
      {loading && rows.length === 0 && <p className="muted">Loading...</p>}
      {!loading && !error && rows.length === 0 && <p className="muted">No classes recorded yet.</p>}
      <ul className="record-list">
        {rows.map((r) => (
          <li key={r.date}>
            <span>{formatDate(r.date)}</span>
            <span className={r.status === 'PRESENT' ? 'badge badge-success' : 'badge badge-danger'}>
              {r.status === 'PRESENT' ? 'Present' : 'Absent'}
            </span>
          </li>
        ))}
      </ul>
    </>
  );
}

export default function StudentAttendance() {
  const { data, loading, error } = useFetch('/api/student/attendance');
  const [open, setOpen] = useState(null);

  const rows = data ?? [];
  const classes = rows.reduce((sum, s) => sum + s.totalClasses, 0);
  const present = rows.reduce((sum, s) => sum + s.present, 0);
  const overall = classes === 0 ? null : Math.round((present * 10000) / classes) / 100;
  const lowCount = rows.filter((s) => s.totalClasses > 0 && (s.present * 100) / s.totalClasses < MIN_ATTENDANCE).length;

  return (
    <>
      <div className="page-header">
        <div>
          <h1>My attendance</h1>
          <p className="muted">Minimum required attendance is {MIN_ATTENDANCE}%.</p>
        </div>
      </div>

      {error && <div className="alert alert-error">{error}</div>}

      <div className="stat-grid">
        <StatCard
          label="Overall attendance"
          value={overall === null ? '—' : `${overall}%`}
          hint={classes === 0 ? 'No classes recorded yet' : `${present} of ${classes} classes`}
          tone={overall === null ? undefined : attendanceTone(overall, classes)}
        />
        <StatCard label="Subjects" value={rows.length} />
        <StatCard
          label={`Below ${MIN_ATTENDANCE}%`}
          value={lowCount}
          tone={lowCount > 0 ? 'danger' : 'success'}
        />
      </div>

      <section className="card">
        <div className="table-wrap">
          <table className={loading ? 'table table-loading' : 'table'}>
            <thead>
              <tr>
                <th>Subject</th>
                <th>Classes</th>
                <th>Present</th>
                <th>Attendance</th>
                <th>What to do</th>
                <th>Details</th>
              </tr>
            </thead>
            <tbody>
              {rows.map((s) => (
                <tr key={s.subjectId}>
                  <td>
                    {s.subjectCode} · {s.subjectName}
                  </td>
                  <td>{s.totalClasses}</td>
                  <td>{s.present}</td>
                  <td>
                    <div className="cell-bar">
                      <ProgressBar value={s.percentage} tone={attendanceTone(s.percentage, s.totalClasses)} />
                      <span>{s.totalClasses === 0 ? '—' : `${s.percentage}%`}</span>
                    </div>
                  </td>
                  <td className="wrap">{adviceText(s.present, s.totalClasses)}</td>
                  <td>
                    <button className="btn btn-sm" onClick={() => setOpen(s)}>
                      Day-wise
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {loading && rows.length === 0 && <p className="muted empty">Loading...</p>}
        {!loading && !error && rows.length === 0 && (
          <p className="muted empty">No subjects found for your course and semester yet.</p>
        )}
      </section>

      {open && (
        <Modal title={`${open.subjectCode} · ${open.subjectName}`} onClose={() => setOpen(null)}>
          <Records subject={open} />
        </Modal>
      )}
    </>
  );
}