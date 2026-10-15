import { useState } from 'react';
import { api } from '../../api';

export default function SubjectForm({ subject, courses, onSaved, onCancel }) {
  const isEdit = Boolean(subject);
  const [form, setForm] = useState(() =>
    isEdit
      ? {
          code: subject.code,
          name: subject.name,
          courseId: String(subject.courseId),
          semester: String(subject.semester),
        }
      : {
          code: '',
          name: '',
          courseId: courses.length === 1 ? String(courses[0].id) : '',
          semester: '',
        },
  );
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const selected = courses.find((c) => String(c.id) === form.courseId);
  const maxSemester = selected ? selected.totalSemesters : 8;

  function update(field) {
    return (event) => setForm((f) => ({ ...f, [field]: event.target.value }));
  }

  function changeCourse(event) {
    const courseId = event.target.value;
    setForm((f) => ({ ...f, courseId, semester: '' }));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setError('');
    setSaving(true);
    try {
      await api(isEdit ? `/api/admin/subjects/${subject.id}` : '/api/admin/subjects', {
        method: isEdit ? 'PUT' : 'POST',
        body: {
          code: form.code.trim(),
          name: form.name.trim(),
          semester: Number(form.semester),
          courseId: Number(form.courseId),
        },
      });
      onSaved(isEdit ? 'Subject updated' : 'Subject created');
    } catch (err) {
      setError(err.message);
    } finally {
      setSaving(false);
    }
  }

  return (
    <form onSubmit={handleSubmit}>
      {error && <div className="alert alert-error">{error}</div>}
      {courses.length === 0 && (
        <div className="alert alert-error">Create a course first. A subject must belong to a course.</div>
      )}

      <div className="form-grid">
        <div className="field">
          <label htmlFor="sb-code">Subject code</label>
          <input id="sb-code" className="input" value={form.code} onChange={update('code')} placeholder="e.g. CS503" required />
        </div>

        <div className="field">
          <label htmlFor="sb-course">Course</label>
          <select id="sb-course" className="input" value={form.courseId} onChange={changeCourse} required>
            <option value="">Select course</option>
            {courses.map((c) => (
              <option key={c.id} value={String(c.id)}>
                {c.name}
              </option>
            ))}
          </select>
        </div>

        <div className="field full">
          <label htmlFor="sb-name">Subject name</label>
          <input id="sb-name" className="input" value={form.name} onChange={update('name')} required />
        </div>

        <div className="field">
          <label htmlFor="sb-sem">Semester</label>
          <select id="sb-sem" className="input" value={form.semester} onChange={update('semester')} required>
            <option value="">Select</option>
            {Array.from({ length: maxSemester }, (_, i) => i + 1).map((n) => (
              <option key={n} value={String(n)}>
                {n}
              </option>
            ))}
          </select>
        </div>
      </div>

      <div className="modal-actions">
        <button type="button" className="btn" onClick={onCancel} disabled={saving}>
          Cancel
        </button>
        <button type="submit" className="btn btn-primary" disabled={saving || courses.length === 0}>
          {saving ? 'Saving...' : isEdit ? 'Save changes' : 'Create subject'}
        </button>
      </div>
    </form>
  );
}