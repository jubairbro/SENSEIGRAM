import React from 'react';
import { Image as ImageIcon, Video, FileText, Trash2, Paperclip, Link as LinkIcon } from 'lucide-react';
import { MediaType, ColorAccent } from '../types';
import { getColorClass } from '../utils/colors';

interface Props {
  mediaType: MediaType;
  setMediaType: (type: MediaType) => void;
  file: File | null;
  setFile: (f: File | null) => void;
  mediaUrl: string;
  setMediaUrl: (url: string) => void;
  useUrl: boolean;
  setUseUrl: (v: boolean) => void;
  hasSpoiler: boolean;
  setHasSpoiler: (v: boolean) => void;
  accent: ColorAccent;
}

export const MediaUploader: React.FC<Props> = ({
  mediaType,
  setMediaType,
  file,
  setFile,
  mediaUrl,
  setMediaUrl,
  useUrl,
  setUseUrl,
  hasSpoiler,
  setHasSpoiler,
  accent
}) => {
  const accentBg = getColorClass(accent, 'bg');
  const accentText = getColorClass(accent, 'text');
  const accentBorder = getColorClass(accent, 'border');
  const accentShadow = getColorClass(accent, 'shadow');

  const toggleMediaType = (type: MediaType) => {
      if (mediaType === type) {
          setMediaType(MediaType.TEXT);
          setFile(null);
          setMediaUrl('');
      } else {
          setMediaType(type);
      }
  };

  return (
    <div className="space-y-3">
        <div className="flex items-center gap-2">
            <span className="text-xs font-bold text-slate-400 uppercase px-1">Attach:</span>
            <button 
                onClick={() => toggleMediaType(MediaType.PHOTO)} 
                className={`p-2 rounded-xl transition-all ${mediaType === MediaType.PHOTO ? `${accentBg} text-white shadow-lg ${accentShadow}` : 'bg-slate-100 dark:bg-white/5 text-slate-500 hover:bg-slate-200 dark:hover:bg-white/10'}`}
                title="Photo"
            >
                <ImageIcon size={18} />
            </button>
            <button 
                onClick={() => toggleMediaType(MediaType.VIDEO)} 
                className={`p-2 rounded-xl transition-all ${mediaType === MediaType.VIDEO ? `${accentBg} text-white shadow-lg ${accentShadow}` : 'bg-slate-100 dark:bg-white/5 text-slate-500 hover:bg-slate-200 dark:hover:bg-white/10'}`}
                title="Video"
            >
                <Video size={18} />
            </button>
            <button 
                onClick={() => toggleMediaType(MediaType.DOCUMENT)} 
                className={`p-2 rounded-xl transition-all ${mediaType === MediaType.DOCUMENT ? `${accentBg} text-white shadow-lg ${accentShadow}` : 'bg-slate-100 dark:bg-white/5 text-slate-500 hover:bg-slate-200 dark:hover:bg-white/10'}`}
                title="File"
            >
                <FileText size={18} />
            </button>
            
            {mediaType !== MediaType.TEXT && (
                <div className="ml-auto text-xs font-medium text-slate-400 bg-slate-100 dark:bg-white/5 px-3 py-1.5 rounded-lg flex items-center gap-2 animate-fade-in">
                    <span>{mediaType === MediaType.PHOTO ? 'Sending Photo' : mediaType === MediaType.VIDEO ? 'Sending Video' : 'Sending File'}</span>
                    <button onClick={() => setMediaType(MediaType.TEXT)}><Trash2 size={14} className="hover:text-red-500" /></button>
                </div>
            )}
        </div>

        {/* Inputs */}
        {mediaType !== MediaType.TEXT && (
            <div className="bg-slate-50 dark:bg-black/20 p-4 rounded-2xl border border-dashed border-slate-300 dark:border-slate-700 animate-fade-in relative transition-all">
                {/* Switcher */}
                <div className="absolute top-3 right-3 flex gap-2">
                    <button onClick={() => setUseUrl(false)} className={`text-[10px] font-bold px-2 py-1 rounded transition-colors ${!useUrl ? `bg-white dark:bg-slate-700 shadow ${accentText}` : 'text-slate-400'}`}>Upload</button>
                    <button onClick={() => setUseUrl(true)} className={`text-[10px] font-bold px-2 py-1 rounded transition-colors ${useUrl ? `bg-white dark:bg-slate-700 shadow ${accentText}` : 'text-slate-400'}`}>Link</button>
                </div>

                {useUrl ? (
                    <div className="mt-2 animate-fade-in">
                        <div className="flex items-center gap-3">
                             <LinkIcon size={18} className="text-slate-400" />
                             <input 
                                type="text" 
                                placeholder={`https://example.com/file...`}
                                value={mediaUrl}
                                onChange={(e) => setMediaUrl(e.target.value)}
                                className={`w-full bg-transparent border-b border-slate-300 dark:border-slate-600 py-2 text-sm outline-none focus:${accentBorder} transition-colors`}
                            />
                        </div>
                    </div>
                ) : (
                    <div className="mt-2 flex items-center gap-3 animate-fade-in">
                        <label className={`flex-1 cursor-pointer flex items-center gap-3 p-2 rounded-xl hover:bg-white dark:hover:bg-white/5 transition-colors group`}>
                            <div className={`p-2 rounded-full bg-opacity-20 ${accentBg} ${accentText} group-hover:scale-110 transition-transform`}>
                                <Paperclip size={20} />
                            </div>
                            <div className="flex flex-col">
                                <span className="text-sm font-medium text-slate-700 dark:text-slate-300 truncate max-w-[200px]">{file ? file.name : 'Choose File...'}</span>
                                <span className="text-[10px] text-slate-400">Max 50MB (Bot API Limit)</span>
                            </div>
                            <input type="file" onChange={(e) => setFile(e.target.files ? e.target.files[0] : null)} className="hidden" />
                        </label>
                    </div>
                )}

                {/* Spoiler Option for Photo/Video */}
                {(mediaType === MediaType.PHOTO || mediaType === MediaType.VIDEO) && (
                    <div className="mt-4 pt-3 border-t border-slate-200 dark:border-white/5">
                        <label className="flex items-center gap-2 cursor-pointer w-fit">
                            <input type="checkbox" checked={hasSpoiler} onChange={() => setHasSpoiler(!hasSpoiler)} className={`w-3.5 h-3.5 accent-${accent}-500`} />
                            <span className="text-xs font-bold text-slate-500 uppercase flex items-center gap-1">Spoiler</span>
                        </label>
                    </div>
                )}
            </div>
        )}
    </div>
  );
};