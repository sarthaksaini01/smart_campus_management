import { useState } from 'react';
import SubjectPage from '../../components/SubjectPage';
import { useFetch } from '../../hooks/useFetch';

const LEVEL_BADGE = {
  HIGH: 'badge-danger',
  MEDIUM: 'badge-warning',
  LOW: 'badge-success',
  INSUFFICIENT_DATA: '',
};

const LEVEL_LABEL = {
  HIGH: 'High',
  MEDIUM: 'Medium',
  LOW: 'Low',
  INSUFFICIENT_DATA: 'No data',
};

function RiskTable({ subject }) {
  const [minScore, setMinScore] = useState('25');
  const { data, loading, error } = useFetch(
    `/api/faculty/subjects/${subject.id}/at-risk?minScore=${minScore}`,
  );
  const rows = data ?? [];

  return (
    <section className="card">
      <div className="toolbar">
        <h2>Students at risk</h2>
        <select className="input" value={minScore} onChange={(event) => setMinScore(event.target.value)}>
          <option value="0">All students</option>
          <option value="25">Medium risk and above</option>
          <option value="50">High risk only</option>
        </select>
      </div>

      <p className="muted">
        The score is calculated from attendance, average marks and failed subjects. It is a rule-based guide, not a
        prediction.
      </p>

      {error && <div className="alert alert-error">{error}</div>}

      <div className="table-wrap">
        <table className={loading ? 'table table-loading' : 'table'}>
          <thead>
            <tr>
              <th>Student</th>
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
      {!loading && !error && rows.length === 0 && (
        <p className="muted empty">No students at this risk level.</p>
      )}
    </section>
  );
}

export default function AtRisk() {
  return (
    <SubjectPage title="At-risk students" description="Students who may need help, with the reasons.">
      {(subject) => <RiskTable key={subject.id} subject={subject} />}
    </SubjectPage>
  );
}