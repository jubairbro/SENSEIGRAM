import React, { useState, useRef, useEffect } from 'react';
import { 
  Send, Bold, Italic, Code, EyeOff, X, GripHorizontal, 
  Link as LinkIcon, Save, Calendar, Eye, CheckCircle2
} from 'lucide-react';
import { TelegramService } from '../services/telegram';
import { InlineButton, SavedChat, MessageDraft, MediaType, ColorAccent } from '../types';
import { CustomSelect } from './CustomSelect';
import { MediaUploader } from './MediaUploader';
import { getColorClass } from '../utils/colors';

interface Props {
  botToken: string;
  savedChats: SavedChat[];
  initialChatId: string;
  addToast: (type: 'success' | 'error' | 'info', msg: string) => void;
  onSaveDraft: (draft: MessageDraft) => void;
  accent: ColorAccent;
}

export const Composer: React.FC<Props> = ({ botToken, savedChats, initialChatId, addToast, onSaveDraft, accent }) => {
  const [chatId, setChatId] = useState(initialChatId);
  const [postLink, setPostLink] = useState('');
  
  // Media State
  const [mediaType, setMediaType] = useState<MediaType>(MediaType.TEXT);
  const [file, setFile] = useState<File | null>(null);
  const [mediaUrl, setMediaUrl] = useState('');
  const [useUrl, setUseUrl] = useState(false);
  const [hasSpoiler, setHasSpoiler] = useState(false);
  
  const [isSending, setIsSending] = useState(false);
  const [buttons, setButtons] = useState<InlineButton[][]>([]);
  
  // Settings
  const [silent, setSilent] = useState(false);
  const [protect, setProtect] = useState(false);
  const [disableWebPreview, setDisableWebPreview] = useState(false);
  
  // Edit Mode
  const [isEditMode, setIsEditMode] = useState(false);
  const [editMsgId, setEditMsgId] = useState('');
  const [editOptions, setEditOptions] = useState({ text: true, buttons: false });

  // Keyboard Builder State
  const [newBtnText, setNewBtnText] = useState('');
  const [newBtnUrl, setNewBtnUrl] = useState('');

  const editorRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (initialChatId) setChatId(String(initialChatId));
  }, [initialChatId]);

  const execCmd = (command: string, value: string | undefined = undefined) => {
    document.execCommand(command, false, value);
    editorRef.current?.focus();
  };

  const handleLink = () => {
      const url = prompt("Enter URL:", "https://");
      if(url) execCmd('createLink', url);
  };

  const insertSpoiler = () => {
    const selection = window.getSelection();
    if (!selection || selection.rangeCount === 0) return;
    const span = document.createElement('span');
    span.style.backgroundColor = '#333';
    span.style.color = 'transparent';
    span.setAttribute('data-tg-spoiler', 'true');
    const range = selection.getRangeAt(0);
    const content = range.extractContents();
    span.appendChild(content);
    range.insertNode(span);
  };

  const convertToTelegramHtml = (node: Node): string => {
    if (node.nodeType === Node.TEXT_NODE) return node.textContent || '';
    if (node.nodeType === Node.ELEMENT_NODE) {
      const el = node as HTMLElement;
      let inner = '';
      el.childNodes.forEach(child => inner += convertToTelegramHtml(child));
      const tag = el.tagName.toLowerCase();
      
      if (['b', 'strong'].includes(tag) || el.style.fontWeight === 'bold') return `<b>${inner}</b>`;
      if (['i', 'em'].includes(tag) || el.style.fontStyle === 'italic') return `<i>${inner}</i>`;
      if (['u'].includes(tag) || el.style.textDecoration.includes('underline')) return `<u>${inner}</u>`;
      if (['s', 'strike'].includes(tag) || el.style.textDecoration.includes('line-through')) return `<s>${inner}</s>`;
      if (tag === 'code') return `<code>${inner}</code>`;
      if (tag === 'pre') return `<pre>${inner}</pre>`;
      if (el.getAttribute('data-tg-spoiler') === 'true' || tag === 'tg-spoiler') return `<tg-spoiler>${inner}</tg-spoiler>`;
      if (tag === 'a' && el.getAttribute('href')) return `<a href="${el.getAttribute('href')}">${inner}</a>`;
      if (['div', 'p'].includes(tag)) return `\n${inner}`;
      if (tag === 'br') return '\n';
      return inner;
    }
    return '';
  };

  const handleLinkParse = () => {
    try {
        const url = new URL(postLink);
        const parts = url.pathname.split('/').filter(Boolean);
        if (parts.length >= 2) {
            const last = parts[parts.length - 1];
            const secondLast = parts[parts.length - 2];
            if (!isNaN(parseInt(last))) {
                setEditMsgId(last);
                if (parts.includes('c')) {
                    const idIdx = parts.indexOf('c') + 1;
                    setChatId('-100' + parts[idIdx]);
                } else if(secondLast !== 'c' && isNaN(Number(secondLast))) {
                    setChatId('@' + secondLast);
                }
                addToast('success', 'ID & Chat Extracted!');
                return;
            }
        }
        throw new Error('Invalid');
    } catch (e) {
        addToast('error', 'Use format: t.me/user/123');
    }
  };

  const handleAddButton = (newRow: boolean) => {
    if (!newBtnText || !newBtnUrl) {
      addToast('error', 'Fields required');
      return;
    }
    const fixedUrl = newBtnUrl.match(/^https?:\/\//) ? newBtnUrl : `https://${newBtnUrl}`;
    const btn: InlineButton = { 
        text: newBtnText, 
        url: fixedUrl.startsWith('http') ? fixedUrl : undefined, 
        callback_data: !fixedUrl.startsWith('http') ? newBtnUrl : undefined 
    };
    if (newRow || buttons.length === 0) {
      setButtons([...buttons, [btn]]);
    } else {
      const newButtons = [...buttons];
      newButtons[newButtons.length - 1].push(btn);
      setButtons(newButtons);
    }
    setNewBtnText('');
    setNewBtnUrl('');
  };

  const getHtml = () => editorRef.current ? convertToTelegramHtml(editorRef.current).trim() : '';

  const handleSaveDraft = () => {
    const html = getHtml();
    if (!html) return addToast('info', 'Content empty');
    const draft: MessageDraft = {
        id: Date.now().toString(),
        chatId,
        html,
        buttons,
        timestamp: Date.now()
    };
    onSaveDraft(draft);
    addToast('success', 'Saved to Drafts');
  };

  const handleSend = async () => {
    if (!botToken) {
        addToast('error', 'Bot Token Required');
        return;
    }
    if (!chatId) {
        addToast('error', 'Select Channel First');
        return;
    }
    const html = getHtml();

    // Validation for New Post
    if (!isEditMode && !html && !file && !mediaUrl && mediaType !== MediaType.TEXT) {
        addToast('error', 'Please add a file or text');
        return;
    }

    setIsSending(true);
    const api = new TelegramService(botToken);

    try {
        if (isEditMode) {
            if(!editMsgId) throw new Error("Message ID missing. Paste link first.");
            const mid = parseInt(editMsgId);
            
            // Logic for Non-Destructive Editing
            let updated = false;

            if (editOptions.text) {
                // If updating text, we check if user wants to update buttons too.
                // If editOptions.buttons is TRUE, we send the new buttons list (even if empty to clear).
                // If editOptions.buttons is FALSE, we send undefined (to keep existing buttons).
                const buttonsToSend = editOptions.buttons ? buttons : undefined;

                if (mediaType === MediaType.TEXT) {
                    await api.editMessageText(chatId, mid, html, 'HTML', buttonsToSend);
                } else {
                    await api.editMessageCaption(chatId, mid, html, 'HTML', buttonsToSend);
                }
                updated = true;
            } 
            
            // If ONLY buttons are being updated (and text is unchecked)
            if (!editOptions.text && editOptions.buttons) {
                await api.editMessageReplyMarkup(chatId, mid, buttons);
                updated = true;
            }

            if(updated) addToast('success', 'Message Updated!');
            else addToast('info', 'Nothing selected to update.');

        } else {
            // New Post Logic
            if (mediaType === MediaType.TEXT) {
                await api.sendMessage(chatId, html, 'HTML', buttons, silent, protect, disableWebPreview);
            } else if (mediaType === MediaType.PHOTO) {
                await api.sendPhoto(chatId, file || mediaUrl, html, 'HTML', buttons, silent, protect, hasSpoiler);
            } else if (mediaType === MediaType.VIDEO) {
                await api.sendVideo(chatId, file || mediaUrl, html, 'HTML', buttons, silent, protect, hasSpoiler);
            } else if (mediaType === MediaType.DOCUMENT) {
                await api.sendDocument(chatId, file || mediaUrl, html, 'HTML', buttons, silent, protect);
            }
            addToast('success', 'Sent Successfully!');
        }
    } catch (e: any) {
        addToast('error', e.message);
    } finally {
        setIsSending(false);
    }
  };

  // Dynamic Styles
  const accentColor = getColorClass(accent, 'text');
  const accentBg = getColorClass(accent, 'bg');
  const accentBorder = getColorClass(accent, 'border');
  const accentRing = getColorClass(accent, 'ring');

  return (
    <div className="glass-panel p-4 sm:p-6 rounded-3xl shadow-lg space-y-6 pb-24 animate-slide-up">
        
        {/* Mode Switcher */}
        <div className="flex bg-slate-100 dark:bg-black/30 p-1.5 rounded-2xl">
            <button 
                onClick={() => { setIsEditMode(false); setEditOptions({ text: true, buttons: true }); }}
                className={`flex-1 py-2.5 rounded-xl text-xs font-bold uppercase tracking-wider transition-all ${!isEditMode ? `bg-white dark:bg-slate-800 shadow ${accentColor}` : 'text-slate-400'}`}
            >
                New Post
            </button>
            <button 
                onClick={() => { setIsEditMode(true); setEditOptions({ text: true, buttons: false }); }}
                className={`flex-1 py-2.5 rounded-xl text-xs font-bold uppercase tracking-wider transition-all ${isEditMode ? `bg-white dark:bg-slate-800 shadow ${accentColor}` : 'text-slate-400'}`}
            >
                Edit Post
            </button>
        </div>

        {/* Target Selector */}
        <div className="grid grid-cols-1 gap-4 animate-fade-in">
            {!isEditMode ? (
                <div>
                     <CustomSelect 
                        options={[...savedChats.map(c => ({ id: c.id, label: c.title, subLabel: String(c.id) })), { id: 'manual', label: 'Manual Input' }]}
                        value={chatId}
                        onChange={setChatId}
                        placeholder="Select Channel"
                        color={accent}
                     />
                    {(chatId === 'manual' || (chatId && !savedChats.find(c => String(c.id) === chatId))) && (
                        <input
                            type="text"
                            value={chatId === 'manual' ? '' : chatId}
                            onChange={(e) => setChatId(e.target.value)}
                            placeholder="Chat ID or @username"
                            className={`mt-2 w-full px-4 py-3 rounded-2xl bg-slate-50 dark:bg-black/20 border border-slate-200 dark:border-slate-700 outline-none focus:${accentBorder} transition-colors`}
                        />
                    )}
                </div>
            ) : (
                <div className="flex gap-2">
                    <input
                        type="text"
                        value={postLink}
                        onChange={(e) => setPostLink(e.target.value)}
                        placeholder="Paste Link (t.me/c/123/456)"
                        className={`flex-1 px-4 py-3 rounded-2xl bg-slate-50 dark:bg-black/40 border border-slate-200 dark:border-slate-700 outline-none focus:${accentBorder} transition-colors`}
                    />
                    <button onClick={handleLinkParse} className={`px-4 bg-opacity-20 ${accentBg} ${accentColor} rounded-2xl`}>
                        <LinkIcon size={20} />
                    </button>
                </div>
            )}
        </div>

        {/* Edit Mode Options */}
        {isEditMode && (
            <div className="bg-amber-50 dark:bg-amber-900/10 p-4 rounded-2xl border border-amber-200 dark:border-amber-800/30">
                <span className="text-xs font-bold text-amber-500 uppercase tracking-wider mb-2 block">What to update?</span>
                <div className="flex flex-wrap gap-4">
                    <label className={`flex-1 flex items-center justify-between p-3 rounded-xl border cursor-pointer transition-all ${editOptions.text ? `${accentBg} bg-opacity-10 border-${accent}-500` : 'bg-white dark:bg-slate-900 border-slate-200 dark:border-slate-700'}`}>
                        <div className="flex items-center gap-2">
                            <input type="checkbox" checked={editOptions.text} onChange={() => setEditOptions({...editOptions, text: !editOptions.text})} className="hidden" />
                            {editOptions.text ? <CheckCircle2 size={18} className={accentColor} /> : <div className="w-[18px] h-[18px] rounded-full border-2 border-slate-300"></div>}
                            <span className={`text-sm font-bold ${editOptions.text ? 'text-slate-800 dark:text-white' : 'text-slate-500'}`}>Text/Caption</span>
                        </div>
                    </label>
                    <label className={`flex-1 flex items-center justify-between p-3 rounded-xl border cursor-pointer transition-all ${editOptions.buttons ? `${accentBg} bg-opacity-10 border-${accent}-500` : 'bg-white dark:bg-slate-900 border-slate-200 dark:border-slate-700'}`}>
                        <div className="flex items-center gap-2">
                             <input type="checkbox" checked={editOptions.buttons} onChange={() => setEditOptions({...editOptions, buttons: !editOptions.buttons})} className="hidden" />
                             {editOptions.buttons ? <CheckCircle2 size={18} className={accentColor} /> : <div className="w-[18px] h-[18px] rounded-full border-2 border-slate-300"></div>}
                            <span className={`text-sm font-bold ${editOptions.buttons ? 'text-slate-800 dark:text-white' : 'text-slate-500'}`}>Buttons</span>
                        </div>
                    </label>
                </div>
                <p className="text-[10px] text-amber-600/70 dark:text-amber-500/70 mt-2 font-medium">
                    * Unselected items will keep their existing content on Telegram.
                </p>
            </div>
        )}

        {/* Media Attachments */}
        {!isEditMode && (
            <MediaUploader 
                mediaType={mediaType}
                setMediaType={setMediaType}
                file={file}
                setFile={setFile}
                mediaUrl={mediaUrl}
                setMediaUrl={setMediaUrl}
                useUrl={useUrl}
                setUseUrl={setUseUrl}
                hasSpoiler={hasSpoiler}
                setHasSpoiler={setHasSpoiler}
                accent={accent}
            />
        )}

        {/* Editor */}
        <div className={`relative group ${isEditMode && !editOptions.text ? 'opacity-50 pointer-events-none grayscale' : ''}`}>
            <div className="flex items-center justify-between px-1 mb-2">
                <span className="text-xs font-bold text-slate-400 uppercase">
                    {mediaType === MediaType.TEXT || isEditMode ? 'Message Text' : 'Media Caption'}
                </span>
                <button onClick={handleSaveDraft} className={`flex items-center gap-1 text-[10px] font-bold ${accentColor} hover:opacity-80 px-2 py-1 rounded-lg transition-colors`}>
                    <Save size={12} /> Save Draft
                </button>
            </div>
            
            {/* Editor Area */}
            <div 
                ref={editorRef}
                contentEditable
                className={`w-full h-56 p-4 rounded-t-2xl bg-white dark:bg-slate-900/80 border border-b-0 border-slate-200 dark:border-white/5 outline-none focus:${accentRing} overflow-y-auto text-sm leading-relaxed`}
                style={{ whiteSpace: 'pre-wrap' }}
            ></div>

            {/* Toolbar at BOTTOM */}
            <div className="flex items-center gap-1 p-2 bg-slate-100 dark:bg-black/30 rounded-b-2xl border border-t-0 border-slate-200 dark:border-white/5">
                {[
                    { i: <Bold size={16} />, c: 'bold' },
                    { i: <Italic size={16} />, c: 'italic' },
                    { i: <Code size={16} />, c: 'formatBlock', v: 'PRE' },
                    { i: <LinkIcon size={16} />, f: handleLink },
                    { i: <EyeOff size={16} />, f: insertSpoiler }
                ].map((btn: any, idx) => (
                    <button 
                        key={idx} 
                        onClick={() => btn.f ? btn.f() : execCmd(btn.c, btn.v)} 
                        className="p-2 hover:bg-white dark:hover:bg-white/10 rounded-lg text-slate-600 dark:text-slate-400 transition-colors"
                        title={btn.f === handleLink ? "Add Link" : ""}
                    >
                        {btn.i}
                    </button>
                ))}
            </div>
        </div>
        
        {/* Settings (Preview, Silent, etc) */}
        {!isEditMode && (
            <div className="flex flex-wrap gap-4 px-2">
                 <label className="flex items-center gap-2 cursor-pointer select-none">
                    <div className={`w-4 h-4 rounded border ${silent ? `${accentBg} border-transparent` : 'border-slate-300 dark:border-slate-600'} flex items-center justify-center transition-colors`}>
                        {silent && <EyeOff size={10} className="text-white" />}
                    </div>
                    <input type="checkbox" checked={silent} onChange={() => setSilent(!silent)} className="hidden" />
                    <span className="text-xs font-bold text-slate-500">Silent</span>
                </label>
                
                 <label className="flex items-center gap-2 cursor-pointer select-none">
                    <div className={`w-4 h-4 rounded border ${protect ? `${accentBg} border-transparent` : 'border-slate-300 dark:border-slate-600'} flex items-center justify-center transition-colors`}>
                         {protect && <EyeOff size={10} className="text-white" />}
                    </div>
                    <input type="checkbox" checked={protect} onChange={() => setProtect(!protect)} className="hidden" />
                    <span className="text-xs font-bold text-slate-500">Protect</span>
                </label>

                {mediaType === MediaType.TEXT && (
                     <label className="flex items-center gap-2 cursor-pointer select-none">
                        <div className={`w-4 h-4 rounded border ${disableWebPreview ? `${accentBg} border-transparent` : 'border-slate-300 dark:border-slate-600'} flex items-center justify-center transition-colors`}>
                            {disableWebPreview && <Eye size={10} className="text-white" />}
                        </div>
                        <input type="checkbox" checked={disableWebPreview} onChange={() => setDisableWebPreview(!disableWebPreview)} className="hidden" />
                        <span className="text-xs font-bold text-slate-500">Hide Preview</span>
                    </label>
                )}
            </div>
        )}

        {/* Buttons Builder */}
        <div className={`border border-slate-200 dark:border-white/10 rounded-2xl p-4 bg-slate-50/50 dark:bg-black/20 ${isEditMode && !editOptions.buttons ? 'opacity-50 pointer-events-none grayscale' : ''}`}>
            <h3 className="font-bold text-xs text-slate-400 uppercase tracking-wider mb-3 flex items-center gap-2">
                <GripHorizontal size={14} /> Inline Buttons
            </h3>
            
            <div className="flex flex-col gap-2 mb-3">
                {buttons.map((row, ri) => (
                    <div key={ri} className="flex gap-2">
                        {row.map((btn, bi) => (
                            <div key={bi} className={`flex-1 py-2 px-2 bg-opacity-10 ${accentBg} ${accentColor} text-xs font-medium rounded-lg border border-opacity-20 ${accentBorder} text-center truncate`}>
                                {btn.text}
                            </div>
                        ))}
                        <button onClick={() => {
                            const newBtns = [...buttons];
                            newBtns.splice(ri, 1);
                            setButtons(newBtns);
                        }} className="text-rose-400 px-1 hover:bg-rose-500/10 rounded"><X size={14}/></button>
                    </div>
                ))}
            </div>

            <div className="flex flex-col gap-2">
                <div className="flex gap-2">
                    <input type="text" placeholder="Label" value={newBtnText} onChange={(e) => setNewBtnText(e.target.value)} className="flex-[2] bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-xl px-3 py-2 text-sm outline-none" />
                    <input type="text" placeholder="URL" value={newBtnUrl} onChange={(e) => setNewBtnUrl(e.target.value)} className="flex-[3] bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-xl px-3 py-2 text-sm outline-none" />
                </div>
                <div className="flex gap-2">
                    <button onClick={() => handleAddButton(false)} className="flex-1 py-2 bg-slate-200 dark:bg-slate-800 rounded-xl text-xs font-bold hover:bg-slate-300">Add to Row</button>
                    <button onClick={() => handleAddButton(true)} className={`flex-1 py-2 bg-opacity-20 ${accentBg} ${accentColor} rounded-xl text-xs font-bold hover:opacity-80`}>New Row</button>
                </div>
            </div>
        </div>

        {/* Action Button */}
        <button 
            onClick={handleSend}
            disabled={isSending}
            className={`w-full py-4 ${accentBg} hover:opacity-90 text-white rounded-2xl font-bold text-lg shadow-xl shadow-opacity-30 flex items-center justify-center gap-2 transition-all active:scale-95 disabled:opacity-70 disabled:active:scale-100`}
        >
            {isSending ? (
                <div className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin"></div>
            ) : (
                <>
                   {isEditMode ? <><Save size={20} /> Update Selected</> : <><Send size={20} /> Send Now</>}
                </>
            )}
        </button>
    </div>
  );
};