import React from 'react';
import { Moon, Sun, Smartphone, Palette } from 'lucide-react';
import { AppTheme, ColorAccent } from '../types';

interface Props {
  currentTheme: AppTheme;
  setTheme: (theme: AppTheme) => void;
  accent: ColorAccent;
  setAccent: (accent: ColorAccent) => void;
}

export const ThemeSwitcher: React.FC<Props> = ({ currentTheme, setTheme, accent, setAccent }) => {
  return (
    <div className="space-y-4">
        {/* Mode */}
        <div className="grid grid-cols-3 gap-2">
            {[
                { id: AppTheme.LIGHT, icon: Sun, label: 'Light' },
                { id: AppTheme.DARK, icon: Moon, label: 'Dark' },
                { id: AppTheme.AMOLED, icon: Smartphone, label: 'Amoled' },
            ].map((t) => (
                <button 
                    key={t.id}
                    onClick={() => setTheme(t.id)}
                    className={`
                        flex flex-col items-center gap-1.5 p-2 rounded-xl border transition-all 
                        ${currentTheme === t.id 
                            ? `border-${accent}-500 bg-${accent}-500/10 shadow-lg shadow-${accent}-500/10` 
                            : 'border-transparent hover:bg-slate-100 dark:hover:bg-slate-800'}
                    `}
                >
                    <t.icon size={20} className={currentTheme === t.id ? `text-${accent}-500` : 'text-slate-400'} />
                    <span className={`text-[10px] font-bold ${currentTheme === t.id ? `text-${accent}-600 dark:text-${accent}-400` : 'text-slate-500'}`}>{t.label}</span>
                </button>
            ))}
        </div>

        {/* Accents */}
        <div className="flex gap-3 justify-center bg-slate-100 dark:bg-black/20 p-2 rounded-xl w-fit mx-auto">
            {[ColorAccent.EMERALD, ColorAccent.BLUE, ColorAccent.VIOLET].map((c) => (
                <button
                    key={c}
                    onClick={() => setAccent(c)}
                    className={`w-8 h-8 rounded-full flex items-center justify-center transition-transform active:scale-90 ring-offset-2 dark:ring-offset-slate-900 ${accent === c ? 'ring-2' : ''} ring-${c}-500`}
                    style={{ backgroundColor: `var(--color-${c})` }}
                >
                    <div className={`w-full h-full rounded-full bg-${c}-500 ${accent === c ? '' : 'opacity-50 hover:opacity-100'}`}></div>
                </button>
            ))}
        </div>
    </div>
  );
};