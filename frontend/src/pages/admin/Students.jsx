import { useEffect, useState } from 'react';
import { api } from '../../api';
import ConfirmModal from '../../components/ConfirmModal';
import Modal from '../../components/Modal';
import Pagination from '../../components/Pagination';
import { useToast } from '../../components/Toast';
import { useDebounce } from '../../hooks/useDebounce';
import { usePagedList } from '../../hooks/usePagedList';
import StudentForm from './StudentForm';

const PAGE_SIZE = 10;

export default function Students() {
  const toast = useToast();
  const [searchInput, setSearchInput] = useState('');
  const [page, setPage] = useState(0);
  const [courses, setCourses] = useState([]);
  const [formFor, setFormFor] = useState(null); // null = closed, 'new' or a student
  const [toDelete, setToDelete] = useState(null);
  const [deleting, setDeleting] = useState(false);

  const search = useDebounce(searchInput.trim());
  const { data, loading, error, reload } = usePagedList('/api/admin/students', {
    search,
    page,
    size: PAGE_SIZE,
  });

  useEffect(() => {
    api('/api/courses')
      .then(setCourses)
      .catch(() => setCourses([]));
  }, []);

  function handleSearch(event) {
    setSearchInput(event.target.value);
    setPage(0);
  }

  function handleSaved(message) {
    setFormFor(null);
    toast.success(message);
    reload();
  }

  async function confirmDelete() {
    setDeleting(true);
    try {
      await api(`/api/admin/students/${toDelete.id}`, { method: 'DELETE' });
      toast.success('Student deleted');
      setToDelete(null);
      if (data && data.content.length === 1 && page > 0) {
        setPage(page - 1);
      } else {
        reload();
      }
    } catch (err) {
      toast.error(err.message);
      setToDelete(null);
    } finally {
      setDeleting(false);
    }
  }

  const rows = data?.content ?? [];

  return (
    <>
      <div className="page-header">
        <div>
          <h1>Students</h1>
          <p className="muted">Create accounts and manage student profiles.</p>
        </div>
        <button className="btn btn-primary" onClick={() => setFormFor('new')}>
          + Add student
        </button>
      </div>

      <section className="card">
        <div className="toolbar">
          <input
            className="input"
            type="search"
            placeholder="Search by name or roll number"
            value={searchInput}
            onChange={handleSearch}
          />
        </div>

        {error && <div className="alert alert-error">{error}</div>}

        <div className="table-wrap">
          <table className={loading ? 'table table-loading' : 'table'}>
            <thead>
              <tr>
                <th>Roll no</th>
                <th>Name</th>
                <th>Email</th>
                <th>Course</th>
                <th>Sem</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {rows.map((s) => (
                <tr key={s.id}>
                  <td>{s.rollNo}</td>
                  <td>{s.fullName}</td>
                  <td>{s.email}</td>
                  <td>{s.course}</td>
                  <td>{s.semester}</td>
                  <td>
                    <div className="row-actions">
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
            {search ? 'No students match your search.' : 'No students yet. Add the first one.'}
          </p>
        )}

        {data && (
          <Pagination
            page={data.page}
            totalPages={data.totalPages}
            totalElements={data.totalElements}
            onChange={setPage}
          />
        )}
      </section>

      {formFor && (
        <Modal title={formFor === 'new' ? 'Add student' : 'Edit student'} onClose={() => setFormFor(null)}>
          <StudentForm
            student={formFor === 'new' ? null : formFor}
            courses={courses}
            onSaved={handleSaved}
            onCancel={() => setFormFor(null)}
          />
        </Modal>
      )}

      {toDelete && (
        <ConfirmModal
          title="Delete student"
          message={`Delete ${toDelete.fullName} (${toDelete.rollNo})? Their login account will be removed too.`}
          busy={deleting}
          onConfirm={confirmDelete}
          onCancel={() => setToDelete(null)}
        />
      )}
    </>
  );
}