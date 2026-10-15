import { useState } from 'react';
import { api } from '../../api';
import SubjectPage from '../../components/SubjectPage';
import { useToast } from '../../components/Toast';
import { useFetch } from '../../hooks/useFetch';
import { PASS_MARK, gradeFor } from '../../utils/grades';

function parseMark(text, max) {
  const trimmed = text.trim();
  if (trimmed === '') {
    return { empty: true };
  }
  if (!/^\d+$/.test(trimmed) || Number(trimmed) > max) {
    return { error: true };
  }
  return { value: Number(trimmed) };
}

function MarksSheet({ subject }) {
  const toast = useToast();
  const { data, loading, error, reload } = useFetch(`/api/faculty/subjects/${subject.id}/marks`);
  const [edits, setEdits] = useState({});
  const [saving, setSaving] = useState(false);

  const rows = data ?? [];
  const dirty = Object.keys(edits).length > 0;

  function valueOf(row, field) {
    const edited = edits[row.studentId]?.[field];
    if (edited !== undefined) {
      return edited;
    }
    const saved = field === 'internal' ? row.internalMarks : row.externalMarks;
    return saved === null || saved === undefined ? '' : String(saved);
  }

  function change(row, field, value) {
    setEdits((current) => ({
      ...current,
      [row.studentId]: { ...current[row.studentId], [field]: value },
    }));
  }

  async function save() {
    const records = [];
    for (const row of rows) {
      const internal = parseMark(valueOf(row, 'internal'), 40);
      const external = parseMark(valueOf(row, 'external'), 60);
      if (internal.empty && external.empty) {
        continue;
      }
      if (internal.empty || external.empty || internal.error || external.error) {
        toast.error(`Check the marks of ${row.fullName}: internal 0-40 and external 0-60, both are required`);
        return;
      }
      records.push({
        studentId: row.studentId,
        internalMarks: internal.value,
        externalMarks: external.value,
      });
    }
    if (records.length === 0) {
      toast.error('Enter marks for at least one student');
      return;
    }

    setSaving(true);
    try {
      await api('/api/faculty/marks', {
        method: 'POST',
        body: { subjectId: subject.id, records },
      });
      toast.success('Marks saved');
      setEdits({});
      reload();
    } catch (err) {
      toast.error(err.message);
    } finally {
      setSaving(false);
    }
  }

  return (
    <section className="card">
      <h2>Marks</h2>
      <p className="muted">Internal is out of 40 and external is out of 60. Pass mark is {PASS_MARK} out of 100.</p>

      {error && <div className="alert alert-error">{error}</div>}

      <div className="table-wrap">
        <table className={loading ? 'table table-loading' : 'table'}>
          <thead>
            <tr>
              <th>Roll no</th>
              <th>Name</th>
              <th>Internal /40</th>
              <th>External /60</th>
              <th>Total</th>
              <th>Grade</th>
              <th>Result</th>
            </tr>
          </thead>
          <tbody>
            {rows.map((row) => {
              const internalText = valueOf(row, 'internal');
              const externalText = valueOf(row, 'external');
              const internal = parseMark(internalText, 40);
              const external = parseMark(externalText, 60);
              const total =
                internal.value !== undefined && external.value !== undefined
                  ? internal.value + external.value
                  : null;

              return (
                <tr key={row.studentId}>
                  <td>{row.rollNo}</td>
                  <td>{row.fullName}</td>
                  <td>
                    <input
                      className={internal.error ? 'input input-num input-error' : 'input input-num'}
                      type="number"
                      min="0"
                      max="40"
                      value={internalText}
                      onChange={(event) => change(row, 'internal', event.target.value)}
                    />
                  </td>
                  <td>
                    <input
                      className={external.error ? 'input input-num input-error' : 'input input-num'}
                      type="number"
                      min="0"
                      max="60"
                      value={externalText}
                      onChange={(event) => change(row, 'external', event.target.value)}
                    />
                  </td>
                  <td>{total === null ? '—' : total}</td>
                  <td>{total === null ? '—' : gradeFor(total)}</td>
                  <td>
                    {total === null ? (
                      <span className="muted">—</span>
                    ) : total >= PASS_MARK ? (
                      <span className="badge badge-success">Pass</span>
                    ) : (
                      <span className="badge badge-danger">Fail</span>
                    )}
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>

      {loading && rows.length === 0 && <p className="muted empty">Loading students...</p>}
      {!loading && !error && rows.length === 0 && (
        <p className="muted empty">
          No students found. A student appears here when their course and semester match this subject.
        </p>
      )}

      <div className="modal-actions">
        {dirty && <span className="muted">You have unsaved changes</span>}
        <button className="btn btn-primary" onClick={save} disabled={saving || rows.length === 0}>
          {saving ? 'Saving...' : 'Save marks'}
        </button>
      </div>
    </section>
  );
}

export default function Marks() {
  return (
    <SubjectPage title="Marks" description="Enter internal and external marks for the students of a subject.">
      {(subject) => <MarksSheet key={subject.id} subject={subject} />}
    </SubjectPage>
  );
}