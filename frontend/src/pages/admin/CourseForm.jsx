import { useState } from 'react';
import { api } from '../../api';

export default function CourseForm({ course, onSaved, onCancel }) {
  const isEdit = Boolean(course);
  const [form, setForm] = useState(() =>
    isEdit
      ? { code: course.code, name: course.name, totalSemesters: String(course.totalSemesters) }
      : { code: '', name: '', totalSemesters: '8' },
  );
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  function update(field) {
    return (event) => setForm((f) => ({ ...f, [field]: event.target.value }));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setError('');
    setSaving(true);
    try {
      await api(isEdit ? `/api/admin/courses/${course.id}` : '/api/admin/courses', {
        method: isEdit ? 'PUT' : 'POST',
        body: {
          code: form.code.trim(),
          name: form.name.trim(),
          totalSemesters: Number(form.totalSemesters),
        },
      });
      onSaved(isEdit ? 'Course updated' : 'Course created');
    } catch (err) {
      setError(err.message);
    } finally {
      setSaving(false);
    }
  }

  return (
    <form onSubmit={handleSubmit}>
      {error && <div className="alert alert-error">{error}</div>}

      <div className="form-grid">
        <div className="field">
          <label htmlFor="c-code">Course code</label>
          <input id="c-code" className="input" value={form.code} onChange={update('code')} placeholder="e.g. BTCSE" required />
        </div>

        <div className="field">
          <label htmlFor="c-sem">Total semesters</label>
          <select id="c-sem" className="input" value={form.totalSemesters} onChange={update('totalSemesters')}>
            {[1, 2, 3, 4, 5, 6, 7, 8].map((n) => (
              <option key={n} value={String(n)}>
                {n}
              </option>
            ))}
          </select>
        </div>

        <div className="field full">
          <label htmlFor="c-name">Course name</label>
          {isEdit ? (
            <>
              <div className="readonly">{course.name}</div>
              <small className="muted">
                The name cannot be changed because student records are matched to a course by its name.
              </small>
            </>
          ) : (
            <input id="c-name" className="input" value={form.name} onChange={update('name')} placeholder="e.g. B.Tech CSE" required />
          )}
        </div>
      </div>

      <div className="modal-actions">
        <button type="button" className="btn" onClick={onCancel} disabled={saving}>
          Cancel
        </button>
        <button type="submit" className="btn btn-primary" disabled={saving}>
          {saving ? 'Saving...' : isEdit ? 'Save changes' : 'Create course'}
        </button>
      </div>
    </form>
  );
}