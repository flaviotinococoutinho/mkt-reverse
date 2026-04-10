import React, { useCallback, useMemo, useRef, useState } from 'react';
import { X, CircleCheck, CircleX, Info } from 'lucide-react';

import {
  ToastContext,
  type ToastContextType,
  type ToastItem,
  type ToastLevel,
} from './toast-context';
import { cn } from '../lib/cn';

function levelIcon(level: ToastLevel) {
  switch (level) {
    case 'success':
      return <CircleCheck className="h-4 w-4 text-mint" aria-hidden="true" />;
    case 'error':
      return <CircleX className="h-4 w-4 text-danger" aria-hidden="true" />;
    default:
      return <Info className="h-4 w-4 text-citrus" aria-hidden="true" />;
  }
}

function levelBorder(level: ToastLevel) {
  switch (level) {
    case 'success':
      return 'border-mint/30 bg-mint/5';
    case 'error':
      return 'border-danger/40 bg-danger/10';
    default:
      return 'border-stroke bg-paper';
  }
}

export function ToastProvider({ children }: { children: React.ReactNode }) {
  const [items, setItems] = useState<ToastItem[]>([]);
  const timeouts = useRef(new Map<string, number>());

  const dismiss = useCallback((id: string) => {
    const timeout = timeouts.current.get(id);
    if (timeout) {
      window.clearTimeout(timeout);
      timeouts.current.delete(id);
    }
    setItems((prev) => prev.filter((t) => t.id !== id));
  }, []);

  const dismissAll = useCallback(() => {
    timeouts.current.forEach((timeout) => window.clearTimeout(timeout));
    timeouts.current.clear();
    setItems([]);
  }, []);

  const toast = useCallback<ToastContextType['toast']>(
    ({ id, level, title, description }) => {
      const toastId = id || crypto.randomUUID();

      setItems((prev) => {
        const next = [{ id: toastId, level, title, description }, ...prev];
        return next.slice(0, 4);
      });

      const timeout = window.setTimeout(() => dismiss(toastId), 4500);
      timeouts.current.set(toastId, timeout);
      return toastId;
    },
    [dismiss]
  );

  const value = useMemo<ToastContextType>(() => ({ toast, dismiss, dismissAll }), [toast, dismiss, dismissAll]);

  return (
    <ToastContext.Provider value={value}>
      {children}

      <div
        className="fixed bottom-4 right-4 z-50 flex w-[min(92vw,380px)] flex-col gap-2"
        aria-live="polite"
        aria-relevant="additions"
      >
        {items.map((t) => (
          <div
            key={t.id}
            className={cn(
              'rounded-xl border p-3 shadow-xl backdrop-blur-sm',
              'transition ease-out duration-200',
              levelBorder(t.level)
            )}
          >
            <div className="flex items-start gap-2">
              <div className="mt-0.5">{levelIcon(t.level)}</div>
              <div className="min-w-0 flex-1">
                <div className="text-sm font-medium text-zinc-100">{t.title}</div>
                {t.description ? (
                  <div className="mt-1 text-xs text-zinc-400 leading-relaxed">{t.description}</div>
                ) : null}
              </div>
              <button
                type="button"
                onClick={() => dismiss(t.id)}
                className="rounded-md p-1 text-zinc-400 hover:text-zinc-200 hover:bg-white/5"
                aria-label="Fechar"
              >
                <X className="h-4 w-4" aria-hidden="true" />
              </button>
            </div>
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  );
}
