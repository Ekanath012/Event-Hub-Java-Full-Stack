import { createContext, useCallback, useContext, useMemo, useRef, useState } from 'react';

const ToastContext = createContext({ success() {}, error() {}, info() {}, dismiss() {} });
const ICONS = { success: '✓', error: '!', info: 'i' };

export function ToastProvider({ children }) {
  const [toasts, setToasts] = useState([]);
  const nextId = useRef(1);

  const dismiss = useCallback((id) => setToasts((list) => list.filter((t) => t.id !== id)), []);

  const notify = useCallback((type, message, duration = 4500) => {
    const id = nextId.current++;
    setToasts((list) => [...list.slice(-3), { id, type, message }]);
    if (duration > 0) setTimeout(() => dismiss(id), duration);
    return id;
  }, [dismiss]);

  const api = useMemo(() => ({
    success: (message, duration) => notify('success', message, duration),
    error: (message, duration) => notify('error', message, duration ?? 6500),
    info: (message, duration) => notify('info', message, duration),
    dismiss
  }), [notify, dismiss]);

  return (
    <ToastContext.Provider value={api}>
      {children}
      <div className="toast-region" aria-live="polite">
        {toasts.map((t) => (
          <div key={t.id} className={`toast toast-${t.type}`} role={t.type === 'error' ? 'alert' : 'status'} onClick={() => dismiss(t.id)}>
            <span className="toast-icon" aria-hidden="true">{ICONS[t.type]}</span>
            <span className="toast-msg">{t.message}</span>
            <button type="button" className="toast-close" aria-label="Dismiss notification" onClick={(e) => { e.stopPropagation(); dismiss(t.id); }}>×</button>
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  );
}

export const useToast = () => useContext(ToastContext);
