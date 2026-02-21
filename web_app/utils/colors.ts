import { ColorAccent } from '../types';

type ColorType = 'bg' | 'text' | 'border' | 'ring' | 'from' | 'to' | 'shadow' | 'iconBg';

export const getColorClass = (accent: ColorAccent, type: ColorType): string => {
    const maps: Record<ColorAccent, Record<string, string>> = {
        [ColorAccent.EMERALD]: { 
            bg: 'bg-emerald-500', 
            text: 'text-emerald-600 dark:text-emerald-400', 
            border: 'border-emerald-500', 
            ring: 'ring-emerald-500',
            from: 'from-emerald-600',
            to: 'to-teal-500',
            shadow: 'shadow-emerald-500/30',
            iconBg: 'bg-emerald-500/10'
        },
        [ColorAccent.BLUE]: { 
            bg: 'bg-blue-500', 
            text: 'text-blue-600 dark:text-blue-400', 
            border: 'border-blue-500', 
            ring: 'ring-blue-500',
            from: 'from-blue-600',
            to: 'to-indigo-500',
            shadow: 'shadow-blue-500/30',
            iconBg: 'bg-blue-500/10'
        },
        [ColorAccent.VIOLET]: { 
            bg: 'bg-violet-500', 
            text: 'text-violet-600 dark:text-violet-400', 
            border: 'border-violet-500', 
            ring: 'ring-violet-500',
            from: 'from-violet-600',
            to: 'to-purple-500',
            shadow: 'shadow-violet-500/30',
            iconBg: 'bg-violet-500/10'
        },
        [ColorAccent.ROSE]: { 
            bg: 'bg-rose-500', 
            text: 'text-rose-600 dark:text-rose-400', 
            border: 'border-rose-500', 
            ring: 'ring-rose-500',
            from: 'from-rose-600',
            to: 'to-pink-500',
            shadow: 'shadow-rose-500/30',
            iconBg: 'bg-rose-500/10'
        },
        [ColorAccent.AMBER]: { 
            bg: 'bg-amber-500', 
            text: 'text-amber-600 dark:text-amber-400', 
            border: 'border-amber-500', 
            ring: 'ring-amber-500',
            from: 'from-amber-600',
            to: 'to-orange-500',
            shadow: 'shadow-amber-500/30',
            iconBg: 'bg-amber-500/10'
        },
    };
    return maps[accent][type];
};

export const getHexColor = (accent: ColorAccent): string => {
    const maps: Record<ColorAccent, string> = {
        [ColorAccent.EMERALD]: '#10b981',
        [ColorAccent.BLUE]: '#3b82f6',
        [ColorAccent.VIOLET]: '#8b5cf6',
        [ColorAccent.ROSE]: '#f43f5e',
        [ColorAccent.AMBER]: '#f59e0b',
    };
    return maps[accent];
};