export const statusClass = (s) => `status-${String(s || '').toLowerCase()}`;
export const statusLabel = (s) => String(s || '').replace(/_/g, ' ');
export const isAdmin = (role) => role === 'ADMIN' || role === 'SUPER_ADMIN';
export const today = () => new Date().toISOString().slice(0, 10);
