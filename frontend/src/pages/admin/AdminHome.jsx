import { Link } from 'react-router-dom';
import { useAuth } from '../../auth/AuthContext';
import BarChart from '../../components/charts/BarChart';
import Donut from '../../components/charts/Donut';
import StatCard from '../../components/StatCard';
import { useFetch } from '../../hooks/useFetch';
import { attendanceTone } from '../../utils/attendance';
import { LEVEL_BADGE, LEVEL_LABEL } from '../../utils/risk';

const COLORS = ['#4f46e5', '#0ea5e9', '#16a34a', '#d97706', '#db2777', '#7c3aed', '#0d9488', '#ea580c'];

function marksTone(value) {
  if (value < 50) return 'danger';
  if (value < 65) return 'warning';
  return 'success';
}

function gradeTone(grade) {
  if (grade === 'F') return 'danger';
  if (grade === 'C') return 'warning';
  if (grade === 'B' || grade === 'B+') return 'primary';
  return 'success';
}

function Panel({ title, hint, state, children }) {
  return (
    <section className="card">
      <h2>{title}</h2>
      {hint && <p className="muted small">{hint}</p>}
      {state.error && <div className="alert alert-error">{state.error}</div>}
      {state.loading && !state.data && <p className="muted">Loading...</p>}
      {state.data && children(state.data)}
    </section>
  );
}

export default function AdminHome() {
  const { user } = useAuth();
  const overview = useFetch('/api/admin/analytics/overview');
  const attendance = useFetch('/api/admin/analytics/attendance-by-subject');
  const performance = useFetch('/api/admin/analytics/subject-performance');
  const grades = useFetch('/api/admin/analytics/grade-distribution');
  const byCourse = useFetch('/api/admin/analytics/students-by-course');
  const risk = useFetch('/api/admin/analytics/at-risk?minScore=0');

  const o = overview.data;
  const everyone = risk.data ?? [];
  const attention = everyone.filter((r) => r.riskLevel === 'HIGH' || r.riskLevel === 'MEDIUM').slice(0, 5);

  return (
    <>
      <div className="page-header">
        <div>
          <h1>Campus dashboard</h1>
          <p className="muted">Welcome, {user.fullName}. Here is how the campus is doing.</p>
        </div>
        <span className="badge badge-info">ADMIN</span>
      </div>

      {overview.error && <div className="alert alert-error">{overview.error}</div>}

      <div className="stat-grid">
        <StatCard label="Students" value={o ? o.totalStudents : '—'} />
        <StatCard label="Faculty" value={o ? o.totalFaculty : '—'} />
        <StatCard label="Courses" value={o ? o.totalCourses : '—'} hint={o ? `${o.totalSubjects} subjects` : undefined} />
        <StatCard
          label="Overall attendance"
          value={o ? (o.attendanceRecords === 0 ? '—' : `${o.overallAttendancePercentage}%`) : '—'}
          hint={o ? `${o.attendanceRecords} records` : undefined}
          tone={o && o.attendanceRecords > 0 ? attendanceTone(o.overallAttendancePercentage, o.attendanceRecords) : undefined}
        />
        <StatCard
          label="High-risk students"
          value={o ? o.highRiskStudents : '—'}
          tone={o ? (o.highRiskStudents > 0 ? 'danger' : 'success') : undefined}
        />
        <StatCard label="Notices" value={o ? o.totalNotices : '—'} />
      </div>

      <div className="chart-grid">
        <Panel title="Attendance by subject" hint="Average attendance of the students, in %" state={attendance}>
          {(rows) => (
            <BarChart
              max={100}
              unit="%"
              emptyText="No attendance recorded yet"
              data={rows.map((s) => ({
                label: s.subjectCode,
                value: s.averageAttendance,
                tone: attendanceTone(s.averageAttendance, s.records),
              }))}
            />
          )}
        </Panel>

        <Panel title="Average marks by subject" hint="Out of 100" state={performance}>
          {(rows) => (
            <BarChart
              max={100}
              emptyText="No marks entered yet"
              data={rows.map((s) => ({
                label: s.subjectCode,
                value: s.averageMarks,
                tone: marksTone(s.averageMarks),
              }))}
            />
          )}
        </Panel>

        <Panel title="Pass percentage by subject" hint="Students scoring 40 or more" state={performance}>
          {(rows) => (
            <BarChart
              max={100}
              unit="%"
              emptyText="No marks entered yet"
              data={rows.map((s) => ({
                label: s.subjectCode,
                value: s.passPercentage,
                tone: s.passPercentage < 60 ? 'danger' : s.passPercentage < 80 ? 'warning' : 'success',
              }))}
            />
          )}
        </Panel>

        <Panel title="Grade distribution" hint="Number of results in each grade" state={grades}>
          {(map) => (
            <BarChart
              data={Object.entries(map).map(([grade, count]) => ({
                label: grade,
                value: count,
                tone: gradeTone(grade),
              }))}
            />
          )}
        </Panel>

        <Panel title="Students by course" state={byCourse}>
          {(map) => (
            <Donut
              centerLabel="students"
              data={Object.entries(map).map(([course, count], i) => ({
                label: course,
                value: count,
                color: COLORS[i % COLORS.length],
              }))}
            />
          )}
        </Panel>

        <Panel title="Student risk levels" hint="Rule-based score from attendance and marks" state={risk}>
          {(rows) => (
            <Donut
              centerLabel="students"
              data={[
                { label: 'High', value: rows.filter((r) => r.riskLevel === 'HIGH').length, color: '#dc2626' },
                { label: 'Medium', value: rows.filter((r) => r.riskLevel === 'MEDIUM').length, color: '#d97706' },
                { label: 'Low', value: rows.filter((r) => r.riskLevel === 'LOW').length, color: '#16a34a' },
                {
                  label: 'No data yet',
                  value: rows.filter((r) => r.riskLevel === 'INSUFFICIENT_DATA').length,
                  color: '#9ca3af',
                },
              ]}
            />
          )}
        </Panel>
      </div>

      <section className="card">
        <div className="toolbar">
          <h2>Students needing attention</h2>
          <Link to="/admin/at-risk">View all at-risk students</Link>
        </div>

        {risk.error && <div className="alert alert-error">{risk.error}</div>}

        <div className="table-wrap">
          <table className="table">
            <thead>
              <tr>
                <th>Student</th>
                <th>Course</th>
                <th>Score</th>
                <th>Level</th>
                <th>Main reason</th>
              </tr>
            </thead>
            <tbody>
              {attention.map((r) => (
                <tr key={r.studentId}>
                  <td>
                    {r.fullName}
                    <br />
                    <small className="muted">{r.rollNo}</small>
                  </td>
                  <td>
                    {r.course} · Sem {r.semester}
                  </td>
                  <td>{r.riskScore}</td>
                  <td>
                    <span className={`badge ${LEVEL_BADGE[r.riskLevel]}`}>{LEVEL_LABEL[r.riskLevel]}</span>
                  </td>
                  <td className="wrap">{r.reasons[0] ?? '—'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {risk.data && attention.length === 0 && <p className="muted empty">No student needs attention right now.</p>}
      </section>
    </>
  );
}