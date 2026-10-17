import { useState } from 'react';
import { useFetch } from '../../hooks/useFetch';
import { LEVEL_BADGE, LEVEL_LABEL } from '../../utils/risk';

export default function AtRiskAll() {
  const [minScore, setMinScore] = useState('25');
  const [text, setText] = useState('');
  const { data, loading, error } = useFetch(`/api/admin/analytics/at-risk?minScore=${minScore}`);

  const needle = text.trim().toLowerCase();
  const rows = (data ?? []).filter(
    (r) => !needle || r.fullName.toLowerCase().includes(needle) || r.rollNo.toLowerCase().includes(needle),
  );

  return (
    <>
      <div className="page-header">
        <div>
          <h1>At-risk students</h1>
          <p className="muted">
            Rule-based score from attendance, average marks and failed subjects. It is a guide, not a prediction.
          </p>
        </div>
      </div>

      <section className="card">
        <div className="toolbar">
          <input
            className="input"
            type="search"
            placeholder="Search by name or roll number"
            value={text}
            onChange={(event) => setText(event.target.value)}
          />
          <select className="input" value={minScore} onChange={(event) => setMinScore(event.target.value)}>
            <option value="0">All students</option>
            <option value="25">Medium risk and above</option>
            <option value="50">High risk only</option>
          </select>
        </div>

        {error && <div className="alert alert-error">{error}</div>}

        <div className="table-wrap">
          <table className={loading ? 'table table-loading' : 'table'}>
            <thead>
              <tr>
                <th>Student</th>
                <th>Course</th>
                <th>Attendance</th>
                <th>Avg marks</th>
                <th>Failed</th>
                <th>Score</th>
                <th>Level</th>
                <th>Why</th>
              </tr>
            </thead>
            <tbody>
              {rows.map((r) => (
                <tr key={r.studentId}>
                  <td>
                    {r.fullName}
                    <br />
                    <small className="muted">{r.rollNo}</small>
                  </td>
                  <td>
                    {r.course} · Sem {r.semester}
                  </td>
                  <td>{r.attendancePercentage === null ? '—' : `${r.attendancePercentage}%`}</td>
                  <td>{r.averageMarks === null ? '—' : r.averageMarks}</td>
                  <td>{r.failedSubjects}</td>
                  <td>{r.riskScore}</td>
                  <td>
                    <span className={`badge ${LEVEL_BADGE[r.riskLevel]}`}>{LEVEL_LABEL[r.riskLevel]}</span>
                  </td>
                  <td className="wrap">
                    {r.reasons.length === 0 ? (
                      <span className="muted">No concerns</span>
                    ) : (
                      <ul className="reasons">
                        {r.reasons.map((reason) => (
                          <li key={reason}>{reason}</li>
                        ))}
                      </ul>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {loading && rows.length === 0 && <p className="muted empty">Loading...</p>}
        {!loading && !error && rows.length === 0 && <p className="muted empty">No students found.</p>}
      </section>
    </>
  );
}