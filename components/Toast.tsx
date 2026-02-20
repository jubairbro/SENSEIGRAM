import React, { useEffect } from 'react';
import { X, CheckCircle, AlertCircle, Info } from 'lucide-react';

export interface ToastMessage {
  id: string;
  type: 'success' | 'error' | 'info';
  message: string;
}

interface ToastProps {
  toasts: ToastMessage[];
  removeToast: (id: string) => void;
}

export const ToastContainer: React.FC<ToastProps> = ({ toasts, removeToast }) => {
  return (
    <div className="fixed bottom-20 left-4 right-4 z-50 flex flex-col gap-2 pointer-events-none sm:bottom-4 sm:right-4 sm:left-auto sm:w-96">
      {toasts.map((toast) => (
        <ToastItem key={toast.id} toast={toast} removeToast={removeToast} />
      ))}
    </div>
  );
};

const ToastItem: React.FC<{ toast: ToastMessage; removeToast: (id: string) => void }> = ({ toast, removeToast }) => {
  useEffect(() => {
    const timer = setTimeout(() => {
      removeToast(toast.id);
    }, 4000);
    return () => clearTimeout(timer);
  }, [toast.id, removeToast]);

  const bgColors = {
    success: 'bg-green-500/90 border-green-400',
    error: 'bg-red-500/90 border-red-400',
    info: 'bg-blue-500/90 border-blue-400',
  };

  const Icon = {
    success: CheckCircle,
    error: AlertCircle,
    info: Info,
  }[toast.type];

  return (
    <div
      className={`
        pointer-events-auto backdrop-blur-md text-white px-4 py-3 rounded-xl shadow-lg border 
        flex items-center justify-between gap-3 animate-slide-up ${bgColors[toast.type]}
      `}
    >
      <div className="flex items-center gap-3">
        <Icon size={20} />
        <span className="text-sm font-medium">{toast.message}</span>
      </div>
      <button onClick={() => removeToast(toast.id)} className="hover:bg-white/20 rounded-full p-1 transition-colors">
        <X size={16} />
      </button>
    </div>
  );
};