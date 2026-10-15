import { useState } from 'react';
import { api } from '../../api';

const EMPTY = { fullName: '', email: '', password: '', rollNo: '', course: '', semester: '' };

export default function StudentForm({ student, courses, onSaved, onCancel }) {
  const isEdit = Boolean(student);
  const [form, setForm] = useState(() =>
    isEdit
      ? {
          ...EMPTY,
          fullName: student.fullName,
          email: student.email,
          rollNo: student.rollNo,
          course: student.course,
          semester: String(student.semester),
        }
      : EMPTY,
  );
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  const courseNames = courses.map((c) => c.name);
  if (form.course && !courseNames.includes(form.course)) {
    courseNames.push(form.course);
  }
  const selectedCourse = courses.find((c) => c.name === form.course);
  const maxSemester = selectedCourse ? selectedCourse.totalSemesters : 8;

  function update(field) {
    return (event) => setForm((f) => ({ ...f, [field]: event.target.value }));
  }

  async function handleSubmit(event) {
    event.preventDefault();
    setError('');
    setSaving(true);
    try {
      if (isEdit) {
        await api(`/api/admin/students/${student.id}`, {
          method: 'PUT',
          body: {
            fullName: form.fullName.trim(),
            course: form.course,
            semester: Number(form.semester),
          },
        });
      } else {
        await api('/api/admin/students', {
          method: 'POST',
          body: {
            fullName: form.fullName.trim(),
            email: form.email.trim(),
            password: form.password,
            rollNo: form.rollNo.trim(),
            course: form.course,
            semester: Number(form.semester),
          },
        });
      }
      onSaved(isEdit ? 'Student updated' : 'Student created');
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
        <div className="field full">
          <label htmlFor="s-name">Full name</label>
          <input id="s-name" className="input" value={form.fullName} onChange={update('fullName')} required />
        </div>

        {isEdit ? (
          <>
            <div className="field">
              <label>Email</label>
              <div className="readonly">{student.email}</div>
            </div>
            <div className="field">
              <label>Roll number</label>
              <div className="readonly">{student.rollNo}</div>
            </div>
          </>
        ) : (
          <>
            <div className="field">
              <label htmlFor="s-email">Email</label>
              <input id="s-email" className="input" type="email" value={form.email} onChange={update('email')} required />
            </div>
            <div className="field">
              <label htmlFor="s-roll">Roll number</label>
              <input id="s-roll" className="input" value={form.rollNo} onChange={update('rollNo')} required />
            </div>
            <div className="field full">
              <label htmlFor="s-pass">Temporary password</label>
              <input
                id="s-pass"
                className="input"
                type="password"
                minLength={6}
                value={form.password}
                onChange={update('password')}
                required
              />
            </div>
          </>
        )}

        <div className="field">
          <label htmlFor="s-course">Course</label>
          {courseNames.length > 0 ? (
            <select id="s-course" className="input" value={form.course} onChange={update('course')} required>
              <option value="">Select course</option>
              {courseNames.map((name) => (
                <option key={name} value={name}>
                  {name}
                </option>
              ))}
            </select>
          ) : (
            <>
              <input
                id="s-course"
                className="input"
                value={form.course}
                onChange={update('course')}
                placeholder="e.g. B.Tech CSE"
                required
              />
              <small className="muted">No courses found. The name must match a course created later.</small>
            </>
          )}
        </div>

        <div className="field">
          <label htmlFor="s-sem">Semester</label>
          <select id="s-sem" className="input" value={form.semester} onChange={update('semester')} required>
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
        <button type="submit" className="btn btn-primary" disabled={saving}>
          {saving ? 'Saving...' : isEdit ? 'Save changes' : 'Create student'}
        </button>
      </div>
    </form>
  );
}