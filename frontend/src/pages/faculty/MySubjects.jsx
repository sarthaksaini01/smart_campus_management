import { Link } from 'react-router-dom';
import { useFetch } from '../../hooks/useFetch';

export default function MySubjects() {
  const { data, loading, error } = useFetch('/api/faculty/subjects');
  const rows = data ?? [];

  return (
    <>
      <div className="page-header">
        <div>
          <h1>My subjects</h1>
          <p className="muted">Subjects assigned to you by the administrator.</p>
        </div>
      </div>

      <section className="card">
        {error && <div className="alert alert-error">{error}</div>}

        <div className="table-wrap">
          <table className={loading ? 'table table-loading' : 'table'}>
            <thead>
              <tr>
                <th>Code</th>
                <th>Name</th>
                <th>Course</th>
                <th>Sem</th>
                <th>Open</th>
              </tr>
            </thead>
            <tbody>
              {rows.map((s) => (
                <tr key={s.id}>
                  <td>{s.code}</td>
                  <td>{s.name}</td>
                  <td>{s.courseName}</td>
                  <td>{s.semester}</td>
                  <td>
                    <div className="row-actions">
                      <Link className="btn btn-sm" to={`/faculty/attendance?subject=${s.id}`}>
                        Attendance
                      </Link>
                      <Link className="btn btn-sm" to={`/faculty/marks?subject=${s.id}`}>
                        Marks
                      </Link>
                      <Link className="btn btn-sm" to={`/faculty/at-risk?subject=${s.id}`}>
                        At-risk
                      </Link>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {loading && rows.length === 0 && <p className="muted empty">Loading...</p>}
        {!loading && !error && rows.length === 0 && (
          <p className="muted empty">No subjects are assigned to you yet.</p>
        )}
      </section>
    </>
  );
}