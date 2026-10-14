import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { api, clearAuth, getStoredAuth, setUnauthorizedHandler, storeAuth } from '../api';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [auth, setAuth] = useState(() => getStoredAuth());

  const logout = useCallback(() => {
    clearAuth();
    setAuth(null);
  }, []);

  useEffect(() => {
    setUnauthorizedHandler(logout);
  }, [logout]);

  const login = useCallback(async (email, password) => {
    const data = await api('/api/auth/login', {
      method: 'POST',
      body: { email, password },
      auth: false,
    });
    storeAuth(data);
    setAuth(data);
    return data;
  }, []);

  const value = useMemo(() => ({ user: auth, login, logout }), [auth, login, logout]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  return useContext(AuthContext);
}