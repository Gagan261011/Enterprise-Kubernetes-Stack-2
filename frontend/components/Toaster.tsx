'use client';

import { useEffect, useState } from 'react';
import { CheckCircle, XCircle, Info, X } from 'lucide-react';

interface Toast {
  id: string;
  type: 'success' | 'error' | 'info';
  message: string;
}

let toastId = 0;
const listeners: Set<(toast: Toast) => void> = new Set();

export const toast = {
  success: (message: string) => {
    const id = String(++toastId);
    listeners.forEach(listener => listener({ id, type: 'success', message }));
  },
  error: (message: string) => {
    const id = String(++toastId);
    listeners.forEach(listener => listener({ id, type: 'error', message }));
  },
  info: (message: string) => {
    const id = String(++toastId);
    listeners.forEach(listener => listener({ id, type: 'info', message }));
  },
};

export function Toaster() {
  const [toasts, setToasts] = useState<Toast[]>([]);

  useEffect(() => {
    const listener = (toast: Toast) => {
      setToasts(prev => [...prev, toast]);
    };
    listeners.add(listener);
    return () => {
      listeners.delete(listener);
    };
  }, []);

  const removeToast = (id: string) => {
    setToasts(prev => prev.filter(t => t.id !== id));
  };

  useEffect(() => {
    if (toasts.length > 0) {
      const timer = setTimeout(() => {
        setToasts(prev => prev.slice(1));
      }, 4000);
      return () => clearTimeout(timer);
    }
  }, [toasts]);

  const icons = {
    success: <CheckCircle className="w-5 h-5 text-green-500" />,
    error: <XCircle className="w-5 h-5 text-red-500" />,
    info: <Info className="w-5 h-5 text-blue-500" />,
  };

  const backgrounds = {
    success: 'bg-green-50 border-green-200',
    error: 'bg-red-50 border-red-200',
    info: 'bg-blue-50 border-blue-200',
  };

  return (
    <div className="fixed top-20 right-4 z-50 flex flex-col gap-2">
      {toasts.map((t) => (
        <div
          key={t.id}
          className={`flex items-center gap-3 px-4 py-3 rounded-lg border shadow-lg animate-slide-in ${backgrounds[t.type]}`}
        >
          {icons[t.type]}
          <span className="text-gray-700">{t.message}</span>
          <button
            onClick={() => removeToast(t.id)}
            className="ml-2 p-1 hover:bg-white/50 rounded"
          >
            <X className="w-4 h-4 text-gray-400" />
          </button>
        </div>
      ))}
      <style jsx global>{`
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
      `}</style>
    </div>
  );
}
