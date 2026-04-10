import React, { createContext, useContext, useState, useCallback } from 'react';
import { X, CheckCircle, AlertCircle, AlertTriangle, Info } from 'lucide-react';

type ToastType = 'success' | 'error' | 'warning' | 'info';

interface Toast {
  id: string;
  type: ToastType;
  title: string;
  message?: string;
  duration?: number;
}

interface ToastContextType {
  toasts: Toast[];
  addToast: (type: ToastType, title: string, message?: string, duration?: number) => void;
  removeToast: (id: string) => void;
  success: (title: string, message?: string) => void;
  error: (title: string, message?: string) => void;
  warning: (title: string, message?: string) => void;
  info: (title: string, message?: string) => void;
}

const ToastContext = createContext<ToastContextType | null>(null);

export const useToast = () => {
  const context = useContext(ToastContext);
  if (!context) {
    throw new Error('useToast must be used within ToastProvider');
  }
  return context;
};

const icons = {
  success: CheckCircle,
  error: AlertCircle,
  warning: AlertTriangle,
  info: Info,
};

const colors = {
  success: 'bg-green-50 border-green-500 text-green-800',
  error: 'bg-red-50 border-red-500 text-red-800',
  warning: 'bg-yellow-50 border-yellow-500 text-yellow-800',
  info: 'bg-blue-50 border-blue-500 text-blue-800',
};

const iconColors = {
  success: 'text-green-500',
  error: 'text-red-500',
  warning: 'text-yellow-500',
  info: 'text-blue-500',
};

export const ToastProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [toasts, setToasts] = useState<Toast[]>([]);

  const addToast = useCallback((type: ToastType, title: string, message?: string, duration = 5000) => {
    const id = Math.random().toString(36).substring(2, 9);
    const toast: Toast = { id, type, title, message, duration };
    
    setToasts((prev) => [...prev, toast]);

    if (duration > 0) {
      setTimeout(() => {
        setToasts((prev) => prev.filter((t) => t.id !== id));
      }, duration);
    }
  }, []);

  const removeToast = useCallback((id: string) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  }, []);

  const success = useCallback((title: string, message?: string) => addToast('success', title, message), [addToast]);
  const error = useCallback((title: string, message?: string) => addToast('error', title, message, 8000), [addToast]);
  const warning = useCallback((title: string, message?: string) => addToast('warning', title, message), [addToast]);
  const info = useCallback((title: string, message?: string) => addToast('info', title, message), [addToast]);

  return (
    <ToastContext.Provider value={{ toasts, addToast, removeToast, success, error, warning, info }}>
      {children}
      <ToastContainer toasts={toasts} onRemove={removeToast} />
    </ToastContext.Provider>
  );
};

const ToastContainer: React.FC<{ toasts: Toast[]; onRemove: (id: string) => void }> = ({ toasts, onRemove }) => {
  if (toasts.length === 0) return null;

  return (
    <div className="fixed top-4 right-4 z-50 flex flex-col gap-2 max-w-sm w-full">
      {toasts.map((toast) => {
        const Icon = icons[toast.type];
        
        return (
          <div
            key={toast.id}
            className={`flex items-start gap-3 p-4 rounded-lg border-l-4 shadow-lg ${colors[toast.type]} animate-slide-in`}
            role="alert"
          >
            <Icon className={`w-5 h-5 flex-shrink-0 ${iconColors[toast.type]}`} />
            
            <div className="flex-1 min-w-0">
              <p className="font-medium">{toast.title}</p>
              {toast.message && (
                <p className="text-sm opacity-90 mt-1">{toast.message}</p>
              )}
            </div>
            
            <button
              onClick={() => onRemove(toast.id)}
              className="flex-shrink-0 p-1 hover:bg-black/5 rounded transition-colors"
              aria-label="Fechar"
            >
              <X className="w-4 h-4" />
            </button>
          </div>
        );
      })}
    </div>
  );
};

// Add animation styles
const style = document.createElement('style');
style.textContent = `
  @keyframes slide-in {
    from {
      transform: translateX(100%);
      opacity: 0;
    }
    to {
      transform: translateX(0);
      opacity: 1;
    }
  }
  .animate-slide-in {
    animation: slide-in 0.3s ease-out;
  }
`;
document.head.appendChild(style);