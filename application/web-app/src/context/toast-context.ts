import { createContext } from 'react';

export type ToastLevel = 'info' | 'success' | 'error';

export type ToastItem = {
  id: string;
  level: ToastLevel;
  title: string;
  description?: string;
};

export type ToastContextType = {
  toast: (input: Omit<ToastItem, 'id'> & { id?: string }) => string;
  dismiss: (id: string) => void;
  dismissAll: () => void;
};

export const ToastContext = createContext<ToastContextType | null>(null);

