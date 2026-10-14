import { Link } from 'react-router-dom';

export default function NotFound() {
  return (
    <div className="card">
      <h1>Page not found</h1>
      <p className="muted">The page you are looking for does not exist.</p>
      <Link to="/" className="btn btn-primary">
        Go to dashboard
      </Link>
    </div>
  );
}