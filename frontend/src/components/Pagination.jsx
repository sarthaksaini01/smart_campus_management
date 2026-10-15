export default function Pagination({ page, totalPages, totalElements, onChange }) {
  if (!totalElements) {
    return null;
  }

  return (
    <div className="pagination">
      <span className="muted">
        {totalElements} total · page {page + 1} of {Math.max(totalPages, 1)}
      </span>
      <div>
        <button className="btn" disabled={page <= 0} onClick={() => onChange(page - 1)}>
          Previous
        </button>
        <button className="btn" disabled={page + 1 >= totalPages} onClick={() => onChange(page + 1)}>
          Next
        </button>
      </div>
    </div>
  );
}