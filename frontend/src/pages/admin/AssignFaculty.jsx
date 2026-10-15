import { useState } from 'react';
import { api } from '../../api';
import { useFetch } from '../../hooks/useFetch';

export default function AssignFaculty({ subject, onSaved, onCancel }) {
  const { data, loading, error } = useFetch('/api/admin/faculty?page=0&size=50');
  const [facultyId, setFacultyId] = useState(subject.facultyId ? String(subject.facultyId) : '');
  const [saving, setSaving] = useState(false);
  const [saveError, setSaveError] = useState('');

  const list = data?.content ?? [];

  async function handleSubmit(event) {
    event.preventDefault();
    setSaveError('');
    setSaving(true);
    try {
      await api(`/api/admin/subjects/${subject.id}/faculty/${facultyId}`, { method: 'PUT' });
      onSaved('Faculty assigned');
    } catch (err) {
      setSaveError(err.message);
    } finally {
      setSaving(false);
    }
  }

  return (
    <form onSubmit={handleSubmit}>
      <p className="muted">
        {subject.code} · {subject.name}
      </p>

      {(error || saveError) && <div className="alert alert-error">{error || saveError}</div>}
      {loading && <p className="muted">Loading faculty...</p>}
      {!loading && !error && list.length === 0 && (
        <div className="alert alert-error">No faculty found. Add faculty first.</div>
      )}

      <div className="field">
        <label htmlFor="as-faculty">Faculty member</label>
        <select
          id="as-faculty"
          className="input"
          value={facultyId}
          onChange={(event) => setFacultyId(event.target.value)}
          required
        >
          <option value="">Select faculty</option>
          {list.map((f) => (
            <option key={f.id} value={String(f.id)}>
              {f.fullName} ({f.employeeId}, {f.department})
            </option>
          ))}
        </select>
      </div>

      <div className="modal-actions">
        <button type="button" className="btn" onClick={onCancel} disabled={saving}>
          Cancel
        </button>
        <button type="submit" className="btn btn-primary" disabled={saving || !facultyId}>
          {saving ? 'Saving...' : 'Assign'}
        </button>
      </div>
    </form>
  );
}