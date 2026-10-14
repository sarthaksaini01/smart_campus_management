import { NavLink, Outlet } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { NAV } from '../nav';

export default function Layout() {
  const { user, logout } = useAuth();
  const items = NAV[user.role] || [];

  return (
    <div className="app">
      <aside className="sidebar">
        <div className="brand">Smart Campus</div>

        <nav className="nav">
          {items.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.end}
              className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}
            >
              {item.label}
            </NavLink>
          ))}
        </nav>

        <div className="sidebar-footer">
          <div className="user-name">{user.fullName}</div>
          <div className="user-role">{user.role}</div>
          <button className="btn btn-ghost" onClick={logout}>
            Logout
          </button>
        </div>
      </aside>

      <main className="content">
        <Outlet />
      </main>
    </div>
  );
}