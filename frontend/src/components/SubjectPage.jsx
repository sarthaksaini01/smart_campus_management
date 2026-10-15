import { useEffect, useMemo } from 'react';
import { useSearchParams } from 'react-router-dom';
import { useFetch } from '../hooks/useFetch';

function useSubjectSelection() {
  const { data, loading, error } = useFetch('/api/faculty/subjects');
  const [params, setParams] = useSearchParams();
  const subjects = useMemo(() => data ?? [], [data]);
  const wanted = params.get('subject');
  const selected = subjects.find((s) => String(s.id) === wanted) ?? null;

  useEffect(() => {
    if (!loading && !selected && subjects.length > 0) {
      setParams({ subject: String(subjects[0].id) }, { replace: true });
    }
  }, [loading, selected, subjects, setParams]);

  function select(id) {
    setParams({ subject: String(id) });
  }

  return { subjects, selected, select, loading, error };
}

export default function SubjectPage({ title, description, children }) {
  const { subjects, selected, select, loading, error } = useSubjectSelection();

  return (
    <>
      <div className="page-header">
        <div>
          <h1>{title}</h1>
          <p className="muted">{description}</p>
        </div>
      </div>

      {error && <div className="alert alert-error">{error}</div>}
      {loading && <p className="muted">Loading your subjects...</p>}

      {!loading && !error && subjects.length === 0 && (
        <div className="card">
          <p>No subjects are assigned to you yet.</p>
          <p className="muted">Ask the administrator to assign you to a subject.</p>
        </div>
      )}

      {subjects.length > 0 && (
        <>
          <section className="card">
            <div className="picker">
              <label htmlFor="subject-picker">Subject</label>
              <select
                id="subject-picker"
                className="input"
                value={selected ? String(selected.id) : ''}
                onChange={(event) => select(event.target.value)}
              >
                {subjects.map((s) => (
                  <option key={s.id} value={String(s.id)}>
                    {s.code} · {s.name}
                  </option>
                ))}
              </select>
              {selected && (
                <span className="muted">
                  {selected.courseName} · Semester {selected.semester}
                </span>
              )}
            </div>
          </section>

          {selected && children(selected)}
        </>
      )}
    </>
  );
}