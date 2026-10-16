import { useAuth } from '../auth/AuthContext';
import Home from './Home';
import StudentHome from './student/StudentHome';

export default function Dashboard() {
  const { user } = useAuth();
  return user.role === 'STUDENT' ? <StudentHome /> : <Home />;
}