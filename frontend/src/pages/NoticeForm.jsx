import { useState } from 'react';
import { api } from '../api';

const LABEL = { ALL: 'Everyone', STUDENT: 'Students', FACULTY: 'Faculty' };

export default function NoticeForm({ audiences, onSaved, onCancel }) {
  const [form, setForm] = useState({ title: '', content: '', audience: audiences[0] });
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
      await api('/api/notices', {
        method: 'POST',
        body: { title: form.title.trim(), content: form.content.trim(), audience: form.audience },
      });
      onSaved('Notice posted');
    } catch (err) {
      setError(err.message);
    } finally {
      setSaving(false);
    }
  }

  return (
    <form onSubmit={handleSubmit}>
      {error && <div className="alert alert-error">{error}</div>}

      <div className="field">
        <label htmlFor="n-title">Title</label>
        <input id="n-title" className="input" maxLength={150} value={form.title} onChange={update('title')} required />
      </div>

      <div className="field">
        <label htmlFor="n-aud">Who can see this</label>
        <select id="n-aud" className="input" value={form.audience} onChange={update('audience')}>
          {audiences.map((a) => (
            <option key={a} value={a}>
              {LABEL[a]}
            </option>
          ))}
        </select>
      </div>

      <div className="field">
        <label htmlFor="n-content">Message</label>
        <textarea
          id="n-content"
          className="input"
          rows={5}
          maxLength={2000}
          value={form.content}
          onChange={update('content')}
          required
        />
        <small className="muted">{form.content.length} / 2000</small>
      </div>

      <div className="modal-actions">
        <button type="button" className="btn" onClick={onCancel} disabled={saving}>
          Cancel
        </button>
        <button type="submit" className="btn btn-primary" disabled={saving}>
          {saving ? 'Posting...' : 'Post notice'}
        </button>
      </div>
    </form>
  );
}