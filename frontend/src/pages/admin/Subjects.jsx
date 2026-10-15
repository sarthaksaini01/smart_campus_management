import { useState } from 'react';
import { api } from '../../api';
import ConfirmModal from '../../components/ConfirmModal';
import Modal from '../../components/Modal';
import { useToast } from '../../components/Toast';
import { useFetch } from '../../hooks/useFetch';
import AssignFaculty from './AssignFaculty';
import SubjectForm from './SubjectForm';

export default function Subjects() {
  const toast = useToast();
  const [courseFilter, setCourseFilter] = useState('');
  const [textFilter, setTextFilter] = useState('');
  const [formFor, setFormFor] = useState(null); // null = closed, 'new' or a subject
  const [assignFor, setAssignFor] = useState(null);
  const [toDelete, setToDelete] = useState(null);
  const [deleting, setDeleting] = useState(false);

  const coursesState = useFetch('/api/courses');
  const subjectsState = useFetch(courseFilter ? `/api/subjects?courseId=${courseFilter}` : '/api/subjects');

  const courses = coursesState.data ?? [];
  const needle = textFilter.trim().toLowerCase();
  const rows = (subjectsState.data ?? [])
    .filter(
      (s) =>
        !needle || [s.code, s.name, s.facultyName ?? ''].some((value) => value.toLowerCase().includes(needle)),
    )
    .sort(
      (a, b) =>
        a.courseName.localeCompare(b.courseName) || a.semester - b.semester || a.code.localeCompare(b.code),
    );

  function handleSaved(message) {
    setFormFor(null);
    setAssignFor(null);
    toast.success(message);
    subjectsState.reload();
  }

  async function confirmDelete() {
    setDeleting(true);
    try {
      await api(`/api/admin/subjects/${toDelete.id}`, { method: 'DELETE' });
      toast.success('Subject deleted');
      subjectsState.reload();
    } catch (err) {
      toast.error(err.message);
    } finally {
      setToDelete(null);
      setDeleting(false);
    }
  }

  const { loading, error } = subjectsState;

  return (
    <>
      <div className="page-header">
        <div>
          <h1>Subjects</h1>
          <p className="muted">Subjects of each course and semester, and the faculty teaching them.</p>
        </div>
        <button className="btn btn-primary" onClick={() => setFormFor('new')}>
          + Add subject
        </button>
      </div>

      <section className="card">
        <div className="toolbar">
          <input
            className="input"
            type="search"
            placeholder="Search by code, name or faculty"
            value={textFilter}
            onChange={(event) => setTextFilter(event.target.value)}
          />
          <select
            className="input"
            value={courseFilter}
            onChange={(event) => setCourseFilter(event.target.value)}
          >
            <option value="">All courses</option>
            {courses.map((c) => (
              <option key={c.id} value={String(c.id)}>
                {c.name}
              </option>
            ))}
          </select>
        </div>

        {error && <div className="alert alert-error">{error}</div>}

        <div className="table-wrap">
          <table className={loading ? 'table table-loading' : 'table'}>
            <thead>
              <tr>
                <th>Code</th>
                <th>Name</th>
                <th>Course</th>
                <th>Sem</th>
                <th>Faculty</th>
                <th>Actions</th>
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
                    {s.facultyName ? s.facultyName : <span className="badge badge-warning">Not assigned</span>}
                  </td>
                  <td>
                    <div className="row-actions">
                      <button className="btn btn-sm" onClick={() => setAssignFor(s)}>
                        {s.facultyName ? 'Change faculty' : 'Assign faculty'}
                      </button>
                      <button className="btn btn-sm" onClick={() => setFormFor(s)}>
                        Edit
                      </button>
                      <button className="btn btn-sm btn-danger" onClick={() => setToDelete(s)}>
                        Delete
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {loading && rows.length === 0 && <p className="muted empty">Loading...</p>}
        {!loading && !error && rows.length === 0 && (
          <p className="muted empty">
            {needle || courseFilter ? 'No subjects match your filters.' : 'No subjects yet. Add the first one.'}
          </p>
        )}
      </section>

      {formFor && (
        <Modal title={formFor === 'new' ? 'Add subject' : 'Edit subject'} onClose={() => setFormFor(null)}>
          <SubjectForm
            subject={formFor === 'new' ? null : formFor}
            courses={courses}
            onSaved={handleSaved}
            onCancel={() => setFormFor(null)}
          />
        </Modal>
      )}

      {assignFor && (
        <Modal title="Assign faculty" onClose={() => setAssignFor(null)}>
          <AssignFaculty subject={assignFor} onSaved={handleSaved} onCancel={() => setAssignFor(null)} />
        </Modal>
      )}

      {toDelete && (
        <ConfirmModal
          title="Delete subject"
          message={`Delete ${toDelete.name} (${toDelete.code})? A subject that already has attendance or marks cannot be deleted.`}
          busy={deleting}
          onConfirm={confirmDelete}
          onCancel={() => setToDelete(null)}
        />
      )}
    </>
  );
}