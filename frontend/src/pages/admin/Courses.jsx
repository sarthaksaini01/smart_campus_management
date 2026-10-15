import { useState } from 'react';
import { api } from '../../api';
import ConfirmModal from '../../components/ConfirmModal';
import Modal from '../../components/Modal';
import { useToast } from '../../components/Toast';
import { useFetch } from '../../hooks/useFetch';
import CourseForm from './CourseForm';

export default function Courses() {
  const toast = useToast();
  const { data, loading, error, reload } = useFetch('/api/courses');
  const [formFor, setFormFor] = useState(null); // null = closed, 'new' or a course
  const [toDelete, setToDelete] = useState(null);
  const [deleting, setDeleting] = useState(false);

  function handleSaved(message) {
    setFormFor(null);
    toast.success(message);
    reload();
  }

  async function confirmDelete() {
    setDeleting(true);
    try {
      await api(`/api/admin/courses/${toDelete.id}`, { method: 'DELETE' });
      toast.success('Course deleted');
      reload();
    } catch (err) {
      toast.error(err.message);
    } finally {
      setToDelete(null);
      setDeleting(false);
    }
  }

  const rows = data ?? [];

  return (
    <>
      <div className="page-header">
        <div>
          <h1>Courses</h1>
          <p className="muted">Programmes offered by the college. Subjects belong to a course.</p>
        </div>
        <button className="btn btn-primary" onClick={() => setFormFor('new')}>
          + Add course
        </button>
      </div>

      <section className="card">
        {error && <div className="alert alert-error">{error}</div>}

        <div className="table-wrap">
          <table className={loading ? 'table table-loading' : 'table'}>
            <thead>
              <tr>
                <th>Code</th>
                <th>Name</th>
                <th>Semesters</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {rows.map((c) => (
                <tr key={c.id}>
                  <td>{c.code}</td>
                  <td>{c.name}</td>
                  <td>{c.totalSemesters}</td>
                  <td>
                    <div className="row-actions">
                      <button className="btn btn-sm" onClick={() => setFormFor(c)}>
                        Edit
                      </button>
                      <button className="btn btn-sm btn-danger" onClick={() => setToDelete(c)}>
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
          <p className="muted empty">No courses yet. Add the first one.</p>
        )}
      </section>

      {formFor && (
        <Modal title={formFor === 'new' ? 'Add course' : 'Edit course'} onClose={() => setFormFor(null)}>
          <CourseForm
            course={formFor === 'new' ? null : formFor}
            onSaved={handleSaved}
            onCancel={() => setFormFor(null)}
          />
        </Modal>
      )}

      {toDelete && (
        <ConfirmModal
          title="Delete course"
          message={`Delete ${toDelete.name} (${toDelete.code})? A course that still has subjects cannot be deleted.`}
          busy={deleting}
          onConfirm={confirmDelete}
          onCancel={() => setToDelete(null)}
        />
      )}
    </>
  );
}