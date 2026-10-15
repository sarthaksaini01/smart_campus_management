import { BrowserRouter, Route, Routes } from 'react-router-dom';
import { AuthProvider } from './auth/AuthContext';
import ProtectedRoute from './auth/ProtectedRoute';
import Layout from './components/Layout';
import { ToastProvider } from './components/Toast';
import Courses from './pages/admin/Courses';
import Faculty from './pages/admin/Faculty';
import Students from './pages/admin/Students';
import Subjects from './pages/admin/Subjects';
import Home from './pages/Home';
import Login from './pages/Login';
import NotFound from './pages/NotFound';
import Notices from './pages/Notices';

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <ToastProvider>
          <Routes>
            <Route path="/login" element={<Login />} />

            <Route element={<ProtectedRoute />}>
              <Route element={<Layout />}>
                <Route index element={<Home />} />
                <Route path="notices" element={<Notices />} />

                <Route element={<ProtectedRoute roles={['ADMIN']} />}>
                  <Route path="admin/students" element={<Students />} />
                  <Route path="admin/faculty" element={<Faculty />} />
                  <Route path="admin/courses" element={<Courses />} />
                  <Route path="admin/subjects" element={<Subjects />} />
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