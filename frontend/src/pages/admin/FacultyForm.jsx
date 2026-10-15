import { useState } from 'react';
import { api } from '../../api';

const EMPTY = { fullName: '', email: '', password: '', employeeId: '', department: '' };

export default function FacultyForm({ faculty, onSaved, onCancel }) {
  const isEdit = Boolean(faculty);
  const [form, setForm] = useState(() =>
    isEdit
      ? {
          ...EMPTY,
          fullName: faculty.fullName,
          email: faculty.email,
          employeeId: faculty.employeeId,
          department: faculty.department,
        }
      : EMPTY,
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
      if (isEdit) {
        await api(`/api/admin/faculty/${faculty.id}`, {
          method: 'PUT',
          body: { fullName: form.fullName.trim(), department: form.department.trim() },
        });
      } else {
        await api('/api/admin/faculty', {
          method: 'POST',
          body: {
            fullName: form.fullName.trim(),
            email: form.email.trim(),
            password: form.password,
            employeeId: form.employeeId.trim(),
            department: form.department.trim(),
          },
        });
      }
      onSaved(isEdit ? 'Faculty updated' : 'Faculty created');
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
          <label htmlFor="f-name">Full name</label>
          <input id="f-name" className="input" value={form.fullName} onChange={update('fullName')} required />
        </div>

        {isEdit ? (
          <>
            <div className="field">
              <label>Email</label>
              <div className="readonly">{faculty.email}</div>
            </div>
            <div className="field">
              <label>Employee ID</label>
              <div className="readonly">{faculty.employeeId}</div>
            </div>
          </>
        ) : (
          <>
            <div className="field">
              <label htmlFor="f-email">Email</label>
              <input id="f-email" className="input" type="email" value={form.email} onChange={update('email')} required />
            </div>
            <div className="field">
              <label htmlFor="f-emp">Employee ID</label>
              <input id="f-emp" className="input" value={form.employeeId} onChange={update('employeeId')} required />
            </div>
            <div className="field full">
              <label htmlFor="f-pass">Temporary password</label>
              <input
                id="f-pass"
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

        <div className="field full">
          <label htmlFor="f-dept">Department</label>
          <input
            id="f-dept"
            className="input"
            value={form.department}
            onChange={update('department')}
            placeholder="e.g. Computer Science"
            required
          />
        </div>
      </div>

      <div className="modal-actions">
        <button type="button" className="btn" onClick={onCancel} disabled={saving}>
          Cancel
        </button>
        <button type="submit" className="btn btn-primary" disabled={saving}>
          {saving ? 'Saving...' : isEdit ? 'Save changes' : 'Create faculty'}
        </button>
      </div>
    </form>
  );
}