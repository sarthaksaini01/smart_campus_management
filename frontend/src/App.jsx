import { BrowserRouter, Route, Routes } from 'react-router-dom';
import { AuthProvider } from './auth/AuthContext';
import ProtectedRoute from './auth/ProtectedRoute';
import Layout from './components/Layout';
import { ToastProvider } from './components/Toast';
import Courses from './pages/admin/Courses';
import Faculty from './pages/admin/Faculty';
import Students from './pages/admin/Students';
import Subjects from './pages/admin/Subjects';
import Dashboard from './pages/Dashboard';
import Attendance from './pages/faculty/Attendance';
import AtRisk from './pages/faculty/AtRisk';
import Marks from './pages/faculty/Marks';
import MySubjects from './pages/faculty/MySubjects';
import Login from './pages/Login';
import NotFound from './pages/NotFound';
import Notices from './pages/Notices';
import StudentAttendance from './pages/student/Attendance';
import Insights from './pages/student/Insights';
import Results from './pages/student/Results';

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <ToastProvider>
          <Routes>
            <Route path="/login" element={<Login />} />

            <Route element={<ProtectedRoute />}>
              <Route element={<Layout />}>
                <Route index element={<Dashboard />} />
                <Route path="notices" element={<Notices />} />

                <Route element={<ProtectedRoute roles={['ADMIN']} />}>
                  <Route path="admin/students" element={<Students />} />
                  <Route path="admin/faculty" element={<Faculty />} />
                  <Route path="admin/courses" element={<Courses />} />
                  <Route path="admin/subjects" element={<Subjects />} />
                </Route>

                <Route element={<ProtectedRoute roles={['FACULTY']} />}>
                  <Route path="faculty/subjects" element={<MySubjects />} />
                  <Route path="faculty/attendance" element={<Attendance />} />
                  <Route path="faculty/marks" element={<Marks />} />
                  <Route path="faculty/at-risk" element={<AtRisk />} />
                </Route>

                <Route element={<ProtectedRoute roles={['STUDENT']} />}>
                  <Route path="student/attendance" element={<StudentAttendance />} />
                  <Route path="student/results" element={<Results />} />
                  <Route path="student/insights" element={<Insights />} />
                </Route>

                <Route path="*" element={<NotFound />} />
              </Route>
            </Route>
          </Routes>
        </ToastProvider>
      </AuthProvider>
    </BrowserRouter>
  );
}