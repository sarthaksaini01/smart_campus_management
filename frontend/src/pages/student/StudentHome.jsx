import { Link } from 'react-router-dom';
import { useAuth } from '../../auth/AuthContext';
import StatCard from '../../components/StatCard';
import { useFetch } from '../../hooks/useFetch';
import { MIN_ATTENDANCE, attendanceTone } from '../../utils/attendance';
import { LEVEL_LABEL, LEVEL_TONE } from '../../utils/risk';

export default function StudentHome() {
  const { user } = useAuth();
  const me = useFetch('/api/student/me');
  const attendance = useFetch('/api/student/attendance');
  const results = useFetch('/api/student/results');
  const risk = useFetch('/api/student/risk');
  const notices = useFetch('/api/notices?page=0&size=3');

  if (me.error) {
    return (
      <>
        <div className="page-header">
          <div>
            <h1>Welcome, {user.fullName}</h1>
          </div>
        </div>
        <div className="card">
          <h2>Your student profile is not ready</h2>
          <p className="muted">{me.error}. Please ask the administrator to create your student profile.</p>
        </div>
      </>
    );
  }

  const student = me.data;
  const subjects = attendance.data ?? [];
  const classes = subjects.reduce((sum, s) => sum + s.totalClasses, 0);
  const present = subjects.reduce((sum, s) => sum + s.present, 0);
  const overall = classes === 0 ? null : Math.round((present * 10000) / classes) / 100;
  const low = subjects.filter((s) => s.totalClasses > 0 && (s.present * 100) / s.totalClasses < MIN_ATTENDANCE);
  const hasResults = Boolean(results.data) && results.data.semesters.length > 0;
  const report = risk.data;
  const noticeList = notices.data?.content ?? [];

  return (
    <>
      <div className="page-header">
        <div>
          <h1>Welcome, {user.fullName}</h1>
          {student && (
            <div className="profile-line">
              <span>Roll no: {student.rollNo}</span>
              <span>{student.course}</span>
              <span>Semester {student.semester}</span>
            </div>
          )}
        </div>
        <span className="badge badge-info">STUDENT</span>
      </div>

      {low.length > 0 && (
        <div className="alert alert-warn">
          Attendance is below {MIN_ATTENDANCE}% in: {low.map((s) => s.subjectName).join(', ')}.{' '}
          <Link to="/student/attendance">See what you need to do</Link>
        </div>
      )}

      <div className="stat-grid">
        <StatCard
          label="Overall attendance"
          value={overall === null ? '—' : `${overall}%`}
          hint={classes === 0 ? 'No classes recorded yet' : `${present} of ${classes} classes`}
          tone={overall === null ? undefined : attendanceTone(overall, classes)}
        />
        <StatCard
          label="CGPA"
          value={hasResults ? results.data.cgpa.toFixed(2) : '—'}
          hint={hasResults ? 'Average grade point of all subjects' : 'No results published yet'}
        />
        <StatCard
          label="Academic risk"
          value={report ? LEVEL_LABEL[report.riskLevel] : '—'}
          hint={
            report && report.riskLevel !== 'INSUFFICIENT_DATA'
              ? `Score ${report.riskScore} / 100`
              : 'Needs attendance or marks'
          }
          tone={report ? LEVEL_TONE[report.riskLevel] : undefined}
        />
        <StatCard
          label={`Subjects below ${MIN_ATTENDANCE}%`}
          value={low.length}
          tone={low.length > 0 ? 'danger' : 'success'}
        />
      </div>

      <section className="card">
        <h2>Quick links</h2>
        <div className="inline-row">
          <Link className="btn" to="/student/attendance">
            Attendance
          </Link>
          <Link className="btn" to="/student/results">
            Results
          </Link>
          <Link className="btn btn-primary" to="/student/insights">
            Risk and AI advice
          </Link>
        </div>
      </section>

      <section className="card">
        <div className="toolbar">
          <h2>Latest notices</h2>
          <Link to="/notices">View all</Link>
        </div>
        {notices.loading && noticeList.length === 0 && <p className="muted">Loading...</p>}
        {notices.error && <div className="alert alert-error">{notices.error}</div>}
        {!notices.loading && !notices.error && noticeList.length === 0 && <p className="muted">No notices yet.</p>}
        <ul className="notice-list">
          {noticeList.map((n) => (
            <li key={n.id}>
              <div className="notice-title">{n.title}</div>
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