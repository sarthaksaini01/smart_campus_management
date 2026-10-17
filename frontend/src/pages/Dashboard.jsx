import { useAuth } from '../auth/AuthContext';
import AdminHome from './admin/AdminHome';
import Home from './Home';
import StudentHome from './student/StudentHome';

export default function Dashboard() {
  const { user } = useAuth();
  if (user.role === 'ADMIN') return <AdminHome />;
  if (user.role === 'STUDENT') return <StudentHome />;
  return <Home />;
}