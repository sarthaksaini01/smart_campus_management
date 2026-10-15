import { BrowserRouter, Route, Routes } from 'react-router-dom';
import { AuthProvider } from './auth/AuthContext';
import ProtectedRoute from './auth/ProtectedRoute';
import Layout from './components/Layout';
import { ToastProvider } from './components/Toast';
import Faculty from './pages/admin/Faculty';
import Students from './pages/admin/Students';
import Home from './pages/Home';
import Login from './pages/Login';
import NotFound from './pages/NotFound';

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

                <Route element={<ProtectedRoute roles={['ADMIN']} />}>
                  <Route path="admin/students" element={<Students />} />
                  <Route path="admin/faculty" element={<Faculty />} />
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