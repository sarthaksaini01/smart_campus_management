import { useState } from 'react';
import { api } from '../../api';
import ConfirmModal from '../../components/ConfirmModal';
import Modal from '../../components/Modal';
import Pagination from '../../components/Pagination';
import { useToast } from '../../components/Toast';
import { useDebounce } from '../../hooks/useDebounce';
import { usePagedList } from '../../hooks/usePagedList';
import FacultyForm from './FacultyForm';

const PAGE_SIZE = 10;

export default function Faculty() {
  const toast = useToast();
  const [searchInput, setSearchInput] = useState('');
  const [page, setPage] = useState(0);
  const [formFor, setFormFor] = useState(null); // null = closed, 'new' or a faculty member
  const [toDelete, setToDelete] = useState(null);
  const [deleting, setDeleting] = useState(false);

  const search = useDebounce(searchInput.trim());
  const { data, loading, error, reload } = usePagedList('/api/admin/faculty', {
    search,
    page,
    size: PAGE_SIZE,
  });

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
      await api(`/api/admin/faculty/${toDelete.id}`, { method: 'DELETE' });
      toast.success('Faculty deleted');
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
          <h1>Faculty</h1>
          <p className="muted">Create accounts and manage faculty profiles.</p>
        </div>
        <button className="btn btn-primary" onClick={() => setFormFor('new')}>
          + Add faculty
        </button>
      </div>

      <section className="card">
        <div className="toolbar">
          <input
            className="input"
            type="search"
            placeholder="Search by name or employee ID"
            value={searchInput}
            onChange={handleSearch}
          />
        </div>

        {error && <div className="alert alert-error">{error}</div>}

        <div className="table-wrap">
          <table className={loading ? 'table table-loading' : 'table'}>
            <thead>
              <tr>
                <th>Employee ID</th>
                <th>Name</th>
                <th>Email</th>
                <th>Department</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {rows.map((f) => (
                <tr key={f.id}>
                  <td>{f.employeeId}</td>
                  <td>{f.fullName}</td>
                  <td>{f.email}</td>
                  <td>{f.department}</td>
                  <td>
                    <div className="row-actions">
                      <button className="btn btn-sm" onClick={() => setFormFor(f)}>
                        Edit
                      </button>
                      <button className="btn btn-sm btn-danger" onClick={() => setToDelete(f)}>
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
            {search ? 'No faculty match your search.' : 'No faculty yet. Add the first one.'}
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
        <Modal title={formFor === 'new' ? 'Add faculty' : 'Edit faculty'} onClose={() => setFormFor(null)}>
          <FacultyForm
            faculty={formFor === 'new' ? null : formFor}
            onSaved={handleSaved}
            onCancel={() => setFormFor(null)}
          />
        </Modal>
      )}

      {toDelete && (
        <ConfirmModal
          title="Delete faculty"
          message={`Delete ${toDelete.fullName} (${toDelete.employeeId})? Their login account will be removed too.`}
          busy={deleting}
          onConfirm={confirmDelete}
          onCancel={() => setToDelete(null)}
        />
      )}
    </>
  );
}