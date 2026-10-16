import { useFetch } from '../../hooks/useFetch';

const GRADE_BADGE = {
  O: 'badge-success',
  'A+': 'badge-success',
  A: 'badge-success',
  'B+': 'badge-info',
  B: 'badge-info',
  C: 'badge-warning',
  F: 'badge-danger',
};

export default function Results() {
  const { data, loading, error } = useFetch('/api/student/results');
  const semesters = data?.semesters ?? [];

  return (
    <>
      <div className="page-header">
        <div>
          <h1>Results</h1>
          <p className="muted">Your marks, grades, SGPA and CGPA.</p>
        </div>
        <button className="btn no-print" onClick={() => window.print()} disabled={semesters.length === 0}>
          Print result card
        </button>
      </div>

      {error && <div className="alert alert-error">{error}</div>}
      {loading && !data && <p className="muted">Loading...</p>}

      {data && (
        <section className="card">
          <div className="result-head">
            <div>
              <div className="stat-label">Student</div>
              <strong>{data.fullName}</strong>
            </div>
            <div>
              <div className="stat-label">Roll no</div>
              <strong>{data.rollNo}</strong>
            </div>
            <div>
              <div className="stat-label">Course</div>
              <strong>{data.course}</strong>
            </div>
            <div>
              <div className="stat-label">CGPA</div>
              <strong className="big-number">{semesters.length === 0 ? '—' : data.cgpa.toFixed(2)}</strong>
            </div>
          </div>
        </section>
      )}

      {data && semesters.length === 0 && (
        <div className="card">
          <p className="muted">No results have been published for you yet.</p>
        </div>
      )}

      {semesters.map((sem) => (
        <section className="card" key={sem.semester}>
          <div className="toolbar">
            <h2>Semester {sem.semester}</h2>
            <span className="badge badge-info">SGPA {sem.sgpa.toFixed(2)}</span>
          </div>

          <div className="table-wrap">
            <table className="table">
              <thead>
                <tr>
                  <th>Code</th>
                  <th>Subject</th>
                  <th>Internal</th>
                  <th>External</th>
                  <th>Total</th>
                  <th>Grade</th>
                  <th>Result</th>
                </tr>
              </thead>
              <tbody>
                {sem.subjects.map((s) => (
                  <tr key={s.subjectId}>
                    <td>{s.subjectCode}</td>
                    <td>{s.subjectName}</td>
                    <td>{s.internalMarks} / 40</td>
                    <td>{s.externalMarks} / 60</td>
                    <td>{s.total} / 100</td>
                    <td>
                      <span className={`badge ${GRADE_BADGE[s.grade] ?? ''}`}>{s.grade}</span>
                    </td>
                    <td>
                      {s.passed ? (
                        <span className="badge badge-success">Pass</span>
                      ) : (
                        <span className="badge badge-danger">Fail</span>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>
      ))}

      {semesters.length > 0 && (
        <p className="muted">
          SGPA and CGPA are the average grade point of the subjects. Every subject has the same weight.
        </p>
      )}
    </>
  );
}