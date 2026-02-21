import React, { useState, useRef, useEffect } from 'react';
import { ChevronDown, Check } from 'lucide-react';

interface Option {
  id: string | number;
  label: string;
  subLabel?: string;
}

interface Props {
  options: Option[];
  value: string | number;
  onChange: (val: string) => void;
  placeholder?: string;
  color: string;
}

export const CustomSelect: React.FC<Props> = ({ options, value, onChange, placeholder, color }) => {
  const [isOpen, setIsOpen] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);

  const selected = options.find(o => String(o.id) === String(value));

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target as Node)) {
        setIsOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  return (
    <div className="relative w-full" ref={dropdownRef}>
      <div 
        onClick={() => setIsOpen(!isOpen)}
        className={`
            w-full px-4 py-3.5 rounded-2xl cursor-pointer flex items-center justify-between transition-all
            ${isOpen ? `ring-2 ring-${color}-500 bg-white dark:bg-slate-900` : 'bg-slate-50 dark:bg-black/40 border border-slate-200 dark:border-white/10 hover:bg-slate-100 dark:hover:bg-white/5'}
        `}
      >
        <div className="flex flex-col items-start truncate">
            {selected ? (
                <>
                    <span className="text-sm font-bold text-slate-800 dark:text-slate-200">{selected.label}</span>
                    {selected.subLabel && <span className="text-[10px] text-slate-400 font-mono">{selected.subLabel}</span>}
                </>
            ) : (
                <span className="text-sm text-slate-400 font-medium">{placeholder || 'Select...'}</span>
            )}
        </div>
        <ChevronDown size={18} className={`text-slate-400 transition-transform ${isOpen ? 'rotate-180' : ''}`} />
      </div>

      {isOpen && (
        <div className="absolute top-full left-0 w-full mt-2 max-h-60 overflow-y-auto rounded-2xl bg-white/90 dark:bg-slate-900/95 backdrop-blur-xl border border-slate-200 dark:border-slate-700 shadow-xl z-50 animate-fade-in custom-scrollbar">
            {options.map((opt) => (
                <div 
                    key={opt.id} 
                    onClick={() => { onChange(String(opt.id)); setIsOpen(false); }}
                    className={`
                        p-3 flex items-center justify-between cursor-pointer border-b border-slate-100 dark:border-white/5 last:border-0
                        hover:bg-slate-50 dark:hover:bg-white/5 transition-colors
                    `}
                >
                    <div className="flex flex-col">
                        <span className={`text-sm font-bold ${String(value) === String(opt.id) ? `text-${color}-500` : 'text-slate-700 dark:text-slate-200'}`}>
                            {opt.label}
                        </span>
                        {opt.subLabel && <span className="text-[10px] text-slate-400 font-mono">{opt.subLabel}</span>}
                    </div>
                    {String(value) === String(opt.id) && <Check size={16} className={`text-${color}-500`} />}
                </div>
            ))}
            {options.length === 0 && (
                <div className="p-4 text-center text-xs text-slate-400">No options available</div>
            )}
        </div>
      )}
    </div>
  );
};