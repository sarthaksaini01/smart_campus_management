import { useState } from 'react';
import { api } from '../../api';
import SubjectPage from '../../components/SubjectPage';
import { useToast } from '../../components/Toast';
import { useFetch } from '../../hooks/useFetch';

function todayString() {
  const d = new Date();
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${d.getFullYear()}-${month}-${day}`;
}

function percentBadge(row) {
  if (row.totalClasses === 0) {
    return <span className="muted">—</span>;
  }
  const cls = row.percentage < 75 ? 'badge-danger' : row.percentage < 85 ? 'badge-warning' : 'badge-success';
  return <span className={`badge ${cls}`}>{row.percentage}%</span>;
}

function AttendanceSheet({ subject }) {
  const toast = useToast();
  const [date, setDate] = useState(todayString);
  const [absent, setAbsent] = useState({}); // studentId -> true when absent, everyone else is present
  const [saving, setSaving] = useState(false);

  const students = useFetch(`/api/faculty/subjects/${subject.id}/students`);
  const summary = useFetch(`/api/faculty/subjects/${subject.id}/attendance/summary`);

  const list = students.data ?? [];
  const summaryRows = summary.data ?? [];
  const absentCount = list.filter((s) => absent[s.id]).length;

  function setStatus(id, isAbsent) {
    setAbsent((current) => ({ ...current, [id]: isAbsent }));
  }

  function markAll(isAbsent) {
    const next = {};
    if (isAbsent) {
      list.forEach((s) => {
        next[s.id] = true;
      });
    }
    setAbsent(next);
  }

  async function save() {
    if (!date) {
      toast.error('Please pick a date');
      return;
    }
    setSaving(true);
    try {
      await api('/api/faculty/attendance', {
        method: 'POST',
        body: {
          subjectId: subject.id,
          date,
          records: list.map((s) => ({
            studentId: s.id,
            status: absent[s.id] ? 'ABSENT' : 'PRESENT',
          })),
        },
      });
      toast.success(`Attendance saved for ${date}`);
      summary.reload();
    } catch (err) {
      toast.error(err.message);
    } finally {
      setSaving(false);
    }
  }

  return (
    <>
      <section className="card">
        <h2>Mark attendance</h2>

        <div className="sheet-bar">
          <input
            className="input"
            type="date"
            value={date}
            max={todayString()}
            onChange={(event) => setDate(event.target.value)}
            aria-label="Date"
          />
          <button className="btn" onClick={() => markAll(false)} disabled={list.length === 0}>
            All present
          </button>
          <button className="btn" onClick={() => markAll(true)} disabled={list.length === 0}>
            All absent
          </button>
          <span className="spacer" />
          <span className="muted">
            Present {list.length - absentCount} · Absent {absentCount}
          </span>
        </div>

        {students.error && <div className="alert alert-error">{students.error}</div>}

        <div className="table-wrap">
          <table className={students.loading ? 'table table-loading' : 'table'}>
            <thead>
              <tr>
                <th>Roll no</th>
                <th>Name</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {list.map((s) => (
                <tr key={s.id}>
                  <td>{s.rollNo}</td>
                  <td>{s.fullName}</td>
                  <td>
                    <div className="seg">
                      <button
                        type="button"
                        className={`seg-btn present${absent[s.id] ? '' : ' active'}`}
                        onClick={() => setStatus(s.id, false)}
                      >
                        Present
                      </button>
                      <button
                        type="button"
                        className={`seg-btn absent${absent[s.id] ? ' active' : ''}`}
                        onClick={() => setStatus(s.id, true)}
                      >
                        Absent
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {students.loading && list.length === 0 && <p className="muted empty">Loading students...</p>}
        {!students.loading && !students.error && list.length === 0 && (
          <p className="muted empty">
            No students found. A student appears here when their course and semester match this subject.
          </p>
        )}

        <div className="modal-actions">
          <button className="btn btn-primary" onClick={save} disabled={saving || list.length === 0}>
            {saving ? 'Saving...' : 'Save attendance'}
          </button>
        </div>
        <p className="muted">Saving the same date again replaces the earlier attendance for that date.</p>
      </section>

      <section className="card">
        <h2>Attendance summary</h2>
        {summary.error && <div className="alert alert-error">{summary.error}</div>}

        <div className="table-wrap">
          <table className={summary.loading ? 'table table-loading' : 'table'}>
            <thead>
              <tr>
                <th>Roll no</th>
                <th>Name</th>
                <th>Classes</th>
                <th>Present</th>
                <th>Attendance</th>
              </tr>
            </thead>
            <tbody>
              {summaryRows.map((r) => (
                <tr key={r.studentId}>
                  <td>{r.rollNo}</td>
                  <td>{r.fullName}</td>
                  <td>{r.totalClasses}</td>
                  <td>{r.present}</td>
                  <td>{percentBadge(r)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        {!summary.loading && !summary.error && summaryRows.length === 0 && (
          <p className="muted empty">No attendance recorded yet.</p>
        )}
      </section>
    </>
  );
}

export default function Attendance() {
  return (
    <SubjectPage title="Attendance" description="Mark attendance for a class and see each student's percentage.">
      {(subject) => <AttendanceSheet key={subject.id} subject={subject} />}
    </SubjectPage>
  );
}