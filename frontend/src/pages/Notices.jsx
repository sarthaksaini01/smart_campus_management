import { useState } from 'react';
import { api } from '../api';
import { useAuth } from '../auth/AuthContext';
import ConfirmModal from '../components/ConfirmModal';
import Modal from '../components/Modal';
import Pagination from '../components/Pagination';
import { useToast } from '../components/Toast';
import { usePagedList } from '../hooks/usePagedList';
import NoticeForm from './NoticeForm';

const PAGE_SIZE = 10;
const AUDIENCE_LABEL = { ALL: 'Everyone', STUDENT: 'Students', FACULTY: 'Faculty' };
const AUDIENCE_BADGE = { ALL: 'badge-info', STUDENT: 'badge-success', FACULTY: 'badge-warning' };

export default function Notices() {
  const { user } = useAuth();
  const toast = useToast();
  const [page, setPage] = useState(0);
  const [showForm, setShowForm] = useState(false);
  const [toDelete, setToDelete] = useState(null);
  const [deleting, setDeleting] = useState(false);

  const { data, loading, error, reload } = usePagedList('/api/notices', {
    search: '',
    page,
    size: PAGE_SIZE,
  });

  const canPost = user.role === 'ADMIN' || user.role === 'FACULTY';
  const audiences = user.role === 'ADMIN' ? ['ALL', 'STUDENT', 'FACULTY'] : ['STUDENT'];

  // The backend is the real authority; this only decides which buttons to show.
  function canDelete(notice) {
    if (user.role === 'ADMIN') return true;
    return user.role === 'FACULTY' && notice.postedByRole === 'FACULTY' && notice.postedBy === user.fullName;
  }

  function handleSaved(message) {
    setShowForm(false);
    toast.success(message);
    if (page === 0) {
      reload();
    } else {
      setPage(0);
    }
  }

  async function confirmDelete() {
    setDeleting(true);
    try {
      await api(`/api/notices/${toDelete.id}`, { method: 'DELETE' });
      toast.success('Notice deleted');
      if (data && data.content.length === 1 && page > 0) {
        setPage(page - 1);
      } else {
        reload();
      }
    } catch (err) {
      toast.error(err.message);
    } finally {
      setToDelete(null);
      setDeleting(false);
    }
  }

  const rows = data?.content ?? [];

  return (
    <>
      <div className="page-header">
        <div>
          <h1>Notices</h1>
          <p className="muted">Announcements for you, newest first.</p>
        </div>
        {canPost && (
          <button className="btn btn-primary" onClick={() => setShowForm(true)}>
            + Post notice
          </button>
        )}
      </div>

      <section className="card">
        {error && <div className="alert alert-error">{error}</div>}
        {loading && rows.length === 0 && <p className="muted empty">Loading...</p>}
        {!loading && !error && rows.length === 0 && <p className="muted empty">No notices yet.</p>}

        <div className={loading ? 'table-loading' : ''}>
          {rows.map((n) => (
            <article key={n.id} className="notice-card">
              <div className="notice-head">
                <h3>{n.title}</h3>
                <span className={`badge ${AUDIENCE_BADGE[n.audience]}`}>{AUDIENCE_LABEL[n.audience]}</span>
              </div>
              <p className="notice-body">{n.content}</p>
              <div className="notice-meta">
                <small className="muted">
                  {n.postedBy} ({n.postedByRole}) · {new Date(n.createdAt).toLocaleString()}
                </small>
                {canDelete(n) && (
                  <button className="btn btn-sm btn-danger" onClick={() => setToDelete(n)}>
                    Delete
                  </button>
                )}
              </div>
            </article>
          ))}
        </div>

        {data && (
          <Pagination
            page={data.page}
            totalPages={data.totalPages}
            totalElements={data.totalElements}
            onChange={setPage}
          />
        )}
      </section>

      {showForm && (
        <Modal title="Post notice" onClose={() => setShowForm(false)}>
          <NoticeForm audiences={audiences} onSaved={handleSaved} onCancel={() => setShowForm(false)} />
        </Modal>
      )}

      {toDelete && (
        <ConfirmModal
          title="Delete notice"
          message={`Delete the notice "${toDelete.title}"?`}
          busy={deleting}
          onConfirm={confirmDelete}
          onCancel={() => setToDelete(null)}
        />
      )}
    </>
  );
}