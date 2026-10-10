import { createContext, useContext, useEffect, useState } from 'react';
import { api, clearAccessToken, refreshSession, setAccessToken } from '../services/api';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    refreshSession()
      .then((data) => { setAccessToken(data.accessToken); setUser(data.user); })
      .catch(() => {})
      .finally(() => setLoading(false));
    const handler = () => { clearAccessToken(); setUser(null); };
    window.addEventListener('eventhub:logout', handler);
    return () => window.removeEventListener('eventhub:logout', handler);
  }, []);

  const login = async (email, password) => {
    const { data } = await api.post('/auth/login', { email, password });
    setAccessToken(data.accessToken); setUser(data.user); return data.user;
  };

  const logout = async () => {
    try { await api.post('/auth/logout'); } finally { clearAccessToken(); setUser(null); }
  };

  return <AuthContext.Provider value={{ user, loading, login, logout }}>{children}</AuthContext.Provider>;
}

export const useAuth = () => useContext(AuthContext);
