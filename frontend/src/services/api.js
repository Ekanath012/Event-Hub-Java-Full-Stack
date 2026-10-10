import axios from 'axios';

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  withCredentials: true,
  headers: { 'Content-Type': 'application/json' }
});

let accessToken = null;
let refreshPromise = null;

export function setAccessToken(token) { accessToken = token; }
export function clearAccessToken() { accessToken = null; }

// Share one refresh request across React StrictMode effects and simultaneous requests in this tab.
export function refreshSession() {
  if (!refreshPromise) {
    const performRefresh = () => api.post('/auth/refresh').then(({ data }) => {
      setAccessToken(data.accessToken);
      return data;
    });
    // Web Locks also serialize refreshes across tabs in modern browsers. The browser sends
    // the latest shared HttpOnly cookie after a waiting tab acquires the lock.
    const lockManager = typeof navigator !== 'undefined' ? navigator.locks : undefined;
    const refreshOperation = lockManager?.request
      ? lockManager.request('eventhub-auth-refresh', performRefresh)
      : performRefresh();
    refreshPromise = refreshOperation.finally(() => { refreshPromise = null; });
  }
  return refreshPromise;
}

api.interceptors.request.use((config) => {
  if (accessToken) config.headers.Authorization = `Bearer ${accessToken}`;
  return config;
});

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const original = error.config;
    const url = original?.url || '';
    const authEndpointThatMustNotRefresh = /\/auth\/(login|register|refresh|logout|forgot-password|reset-password)(?:[/?]|$)/.test(url);
    if (error.response?.status !== 401 || !original || original._retry || authEndpointThatMustNotRefresh) {
      return Promise.reject(error);
    }
    original._retry = true;
    try {
      const data = await refreshSession();
      original.headers = original.headers || {};
      original.headers.Authorization = `Bearer ${data.accessToken}`;
      return api(original);
    } catch (refreshError) {
      clearAccessToken();
      window.dispatchEvent(new Event('eventhub:logout'));
      return Promise.reject(refreshError);
    }
  }
);
