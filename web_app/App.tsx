import React, { useState, useEffect } from 'react';
import { Home, PenTool, Menu, Plus, Zap, History, ChevronRight, X, Youtube, Edit2, Wifi, WifiOff, Send } from 'lucide-react';
import { BotManager } from './components/BotManager';
import { Composer } from './components/Composer';
import { ToastContainer, ToastMessage } from './components/Toast';
import { fetchRemoteConfig } from './services/config';
import { AppTheme, SavedChat, TelegramUser, MessageDraft, ColorAccent, Announcement } from './types';
import { getColorClass, getHexColor } from './utils/colors';

function App() {
  // Default to SYSTEM if nothing stored
  const [theme, setTheme] = useState<AppTheme>(AppTheme.SYSTEM);
  const [accent, setAccent] = useState<ColorAccent>(ColorAccent.EMERALD);
  const [activeTab, setActiveTab] = useState<'home' | 'compose' | 'menu'>('home');
  const [showTutorial, setShowTutorial] = useState(false);
  const [userName, setUserName] = useState('');
  const [isEditingName, setIsEditingName] = useState(false);
  
  // Connection State
  const [isOnline, setIsOnline] = useState(navigator.onLine);
  
  // Remote Announcement
  const [announcement, setAnnouncement] = useState<Announcement | null>(null);

  // Data
  const [botToken, setBotToken] = useState('');
  const [savedChats, setSavedChats] = useState<SavedChat[]>([]);
  const [drafts, setDrafts] = useState<MessageDraft[]>([]);
  const [selectedChatId, setSelectedChatId] = useState<string>('');
  const [botUser, setBotUser] = useState<TelegramUser | null>(null);

  const [toasts, setToasts] = useState<ToastMessage[]>([]);

  const addToast = (type: 'success' | 'error' | 'info', message: string) => {
    const id = Date.now().toString();
    setToasts(prev => [...prev, { id, type, message }]);
  };
  const removeToast = (id: string) => setToasts(prev => prev.filter(t => t.id !== id));

  // --- Network Monitor ---
  useEffect(() => {
    const handleOnline = () => {
        setIsOnline(true);
        addToast('success', 'Back Online');
    };
    const handleOffline = () => {
        setIsOnline(false);
        addToast('error', 'No Internet Connection');
    };
    
    window.addEventListener('online', handleOnline);
    window.addEventListener('offline', handleOffline);
    return () => {
        window.removeEventListener('online', handleOnline);
        window.removeEventListener('offline', handleOffline);
    };
  }, []);

  // --- Initialization ---
  useEffect(() => {
    const loadInit = async () => {
      const storedTheme = localStorage.getItem('theme') as AppTheme;
      if (storedTheme) setTheme(storedTheme);
      
      const storedAccent = localStorage.getItem('accent') as ColorAccent;
      if (storedAccent) setAccent(storedAccent);
      
      const storedName = localStorage.getItem('userName');
      if (storedName) setUserName(storedName);
      else setIsEditingName(true); 

      const storedToken = localStorage.getItem('botToken');
      if (storedToken) setBotToken(storedToken);
      
      const storedChats = localStorage.getItem('savedChats');
      if (storedChats) setSavedChats(JSON.parse(storedChats));
      
      const storedDrafts = localStorage.getItem('drafts');
      if (storedDrafts) setDrafts(JSON.parse(storedDrafts));
      
      const hasSeenTut = localStorage.getItem('hasSeenTut');
      if (!hasSeenTut) setShowTutorial(true);
      
      await fetchRemoteConfig();
      fetchAnnouncement();
    };
    loadInit();
  }, []);

  const fetchAnnouncement = async () => {
    try {
        const res = await fetch('https://raw.githubusercontent.com/jubairbro/Faw/refs/heads/main/folder/announcement.json');
        if(res.ok) {
            const data: Announcement = await res.json();
            if(data.show) setTimeout(() => setAnnouncement(data), 3000);
        }
    } catch(e) { console.log('No announcement'); }
  };

  // --- Theme Logic ---
  useEffect(() => {
    localStorage.setItem('theme', theme);
    const root = document.documentElement;
    root.classList.remove('light', 'dark', 'amoled');

    if (theme === AppTheme.SYSTEM) {
        const systemDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
        if (systemDark) root.classList.add('dark');
        else root.classList.add('light');
        
        // Listener for system changes
        const listener = (e: MediaQueryListEvent) => {
            root.classList.remove('light', 'dark', 'amoled');
            if(e.matches) root.classList.add('dark');
            else root.classList.add('light');
        };
        const query = window.matchMedia('(prefers-color-scheme: dark)');
        query.addEventListener('change', listener);
        return () => query.removeEventListener('change', listener);
    } else if (theme === AppTheme.AMOLED) {
        root.classList.add('dark', 'amoled');
    } else {
        root.classList.add(theme);
    }
  }, [theme]);

  useEffect(() => {
      localStorage.setItem('accent', accent);
      document.documentElement.style.setProperty('--accent-color', getHexColor(accent));
  }, [accent]);

  useEffect(() => { localStorage.setItem('botToken', botToken); }, [botToken]);
  useEffect(() => { localStorage.setItem('savedChats', JSON.stringify(savedChats)); }, [savedChats]);
  useEffect(() => { localStorage.setItem('drafts', JSON.stringify(drafts)); }, [drafts]);
  useEffect(() => { if(userName) localStorage.setItem('userName', userName); }, [userName]);

  const handleSaveDraft = (draft: MessageDraft) => {
    const newDrafts = [draft, ...drafts].slice(0, 10);
    setDrafts(newDrafts);
  };

  const accentText = getColorClass(accent, 'text');
  const accentBg = getColorClass(accent, 'bg');

  return (
    <div className={`min-h-screen pb-20 md:pb-0 md:pl-64 transition-all duration-300 font-sans`}>
      
      <div className="pt-safe-expanded w-full bg-slate-50/80 dark:bg-slate-950/80 backdrop-blur-md sticky top-0 z-40 md:hidden"></div>

      {announcement && (
        <div className="fixed inset-0 z-[60] flex items-end sm:items-center justify-center bg-black/50 backdrop-blur-sm p-4 animate-fade-in">
             <div className="glass-panel p-6 rounded-3xl w-full max-w-sm mb-safe shadow-2xl animate-slide-up relative bg-white/80 dark:bg-slate-900/80">
                <button onClick={() => setAnnouncement(null)} className="absolute top-4 right-4 text-slate-400 hover:text-slate-600"><X size={20}/></button>
                <h3 className={`text-xl font-bold mb-2 ${accentText}`}>{announcement.title}</h3>
                <p className="text-slate-600 dark:text-slate-300 mb-6 text-sm leading-relaxed">{announcement.text}</p>
                {announcement.buttonText && (
                    <a href={announcement.buttonUrl || '#'} target="_blank" className={`block w-full text-center py-3 ${accentBg} text-white font-bold rounded-xl shadow-lg hover:opacity-90`}>
                        {announcement.buttonText}
                    </a>
                )}
             </div>
        </div>
      )}

      {showTutorial && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-6 animate-fade-in">
            <div className="bg-white dark:bg-slate-900 rounded-3xl p-8 max-w-sm w-full shadow-2xl relative">
                <button onClick={() => { setShowTutorial(false); localStorage.setItem('hasSeenTut', 'true'); }} className="absolute top-4 right-4 text-slate-400 hover:text-slate-600"><X/></button>
                <div className={`w-16 h-16 bg-opacity-20 ${accentBg} rounded-full flex items-center justify-center mb-6 text-4xl`}>👋</div>
                <h2 className="text-2xl font-bold mb-2">Welcome to TELE SENSEI</h2>
                <p className="text-slate-500 mb-6 leading-relaxed">Your professional serverless bot manager.</p>
                <button onClick={() => { setShowTutorial(false); localStorage.setItem('hasSeenTut', 'true'); }} className={`w-full py-3 ${accentBg} text-white rounded-xl font-bold hover:opacity-90 transition-colors`}>Get Started</button>
            </div>
        </div>
      )}

      {/* Sidebar (Desktop) */}
      <aside className="fixed left-0 top-0 h-full w-64 hidden md:flex flex-col border-r border-slate-200 dark:border-white/5 bg-white/80 dark:bg-black/40 backdrop-blur-xl z-20">
        <div className="p-8">
          <h1 className={`text-2xl font-black ${accentText} tracking-tight`}>TELE SENSEI</h1>
          <p className="text-[10px] text-slate-400 font-bold tracking-widest uppercase mt-1">Bot Command Center</p>
        </div>
        <nav className="flex-1 px-4 space-y-2">
          <NavBtn icon={Home} label="Home" active={activeTab === 'home'} onClick={() => setActiveTab('home')} accent={accent} />
          <NavBtn icon={PenTool} label="Composer" active={activeTab === 'compose'} onClick={() => setActiveTab('compose')} accent={accent} />
          <NavBtn icon={Menu} label="Menu" active={activeTab === 'menu'} onClick={() => setActiveTab('menu')} accent={accent} />
        </nav>
      </aside>

      <nav className="fixed bottom-0 left-0 w-full md:hidden bg-white/90 dark:bg-black/80 backdrop-blur-lg border-t border-slate-200 dark:border-white/5 flex justify-around p-3 z-30 pb-safe">
        <MobileNavBtn icon={Home} label="Home" active={activeTab === 'home'} onClick={() => setActiveTab('home')} accent={accent} />
        <MobileNavBtn icon={PenTool} label="Compose" active={activeTab === 'compose'} onClick={() => setActiveTab('compose')} accent={accent} />
        <MobileNavBtn icon={Menu} label="Menu" active={activeTab === 'menu'} onClick={() => setActiveTab('menu')} accent={accent} />
      </nav>

      <main className="px-4 pt-2 pb-20 md:p-10 max-w-4xl mx-auto space-y-6">
        <div className="md:hidden flex justify-between items-center mb-4">
           <div>
             <h1 className="text-xl font-black tracking-tight text-slate-800 dark:text-white">TELE SENSEI</h1>
             <p className={`text-[10px] ${accentText} font-bold uppercase tracking-wider`}>Serverless</p>
           </div>
           {botUser && <div className={`text-[10px] px-2 py-1 bg-opacity-10 ${accentBg} ${accentText} rounded-full font-mono border border-opacity-20 ${accentBg}`}>@{botUser.username}</div>}
        </div>
        
        {!isOnline && (
            <div className="bg-red-500/10 border border-red-500/20 text-red-600 dark:text-red-400 p-3 rounded-2xl flex items-center gap-3 text-sm font-bold animate-fade-in">
                <WifiOff size={18} />
                <span>You are currently offline.</span>
            </div>
        )}

        {activeTab === 'home' && (
           <div className="animate-fade-in space-y-6">
              {/* Stats / Welcome */}
              <div className={`p-8 rounded-[2rem] bg-gradient-to-br ${accent === ColorAccent.EMERALD ? 'from-emerald-600 to-slate-800' : accent === ColorAccent.BLUE ? 'from-blue-600 to-slate-800' : accent === ColorAccent.VIOLET ? 'from-violet-600 to-slate-800' : accent === ColorAccent.ROSE ? 'from-rose-600 to-slate-800' : 'from-amber-600 to-slate-800'} text-white shadow-2xl shadow-opacity-20 relative overflow-hidden group`}>
                  <div className="relative z-10">
                      {isEditingName ? (
                          <div className="mb-4">
                              <h2 className="text-3xl font-bold mb-2">Hello,</h2>
                              <input 
                                type="text" 
                                autoFocus
                                placeholder="Enter your name" 
                                value={userName}
                                onChange={(e) => setUserName(e.target.value)}
                                onBlur={() => { if(userName) setIsEditingName(false); }}
                                onKeyDown={(e) => { if(e.key === 'Enter' && userName) setIsEditingName(false); }}
                                className="bg-transparent border-b-2 border-white/30 text-3xl font-bold outline-none placeholder:text-white/30 w-full max-w-[200px]"
                              />
                          </div>
                      ) : (
                          <div className="flex items-center gap-2 mb-2 group/name cursor-pointer" onClick={() => setIsEditingName(true)}>
                              <h2 className="text-3xl font-bold">Hello {userName || 'Sensei'}.</h2>
                              <Edit2 size={16} className="opacity-0 group-hover/name:opacity-100 transition-opacity" />
                          </div>
                      )}
                      <p className="opacity-80 text-sm mb-6 max-w-xs">Ready to manage your empire? You have {savedChats.length} active channels.</p>
                      <div className="flex gap-3">
                        <button onClick={() => setActiveTab('compose')} className={`bg-white ${accentText} px-5 py-2.5 rounded-xl text-sm font-bold shadow-lg hover:scale-105 transition-transform`}>New Post</button>
                        <button onClick={() => setActiveTab('menu')} className="bg-black/20 px-5 py-2.5 rounded-xl text-sm font-bold backdrop-blur hover:bg-black/30 transition-colors">Config</button>
                      </div>
                  </div>
                  <Zap className="absolute right-4 bottom-4 text-white opacity-10 w-32 h-32 rotate-12 group-hover:rotate-0 transition-transform duration-700" />
              </div>

              {/* Saved Channels */}
              <div>
                <div className="flex items-center justify-between mb-4 px-2">
                    <h3 className="font-bold text-lg text-slate-700 dark:text-slate-200">Your Channels</h3>
                    <button onClick={() => setActiveTab('menu')} className={`text-xs ${accentText} font-bold uppercase hover:underline`}>Manage</button>
                </div>
                <div className="grid grid-cols-2 md:grid-cols-3 gap-3">
                    {savedChats.length === 0 ? (
                        <div onClick={() => setActiveTab('menu')} className="col-span-full py-10 border-2 border-dashed border-slate-300 dark:border-slate-700 rounded-3xl flex flex-col items-center justify-center text-slate-400 cursor-pointer hover:bg-slate-50 dark:hover:bg-white/5 transition-colors">
                            <Plus className="mb-2 opacity-50" />
                            <p className="text-sm">Add Channel</p>
                        </div>
                    ) : (
                        savedChats.map(chat => (
                            <div key={chat.id} onClick={() => { setSelectedChatId(String(chat.id)); setActiveTab('compose'); }} className={`bg-white dark:bg-slate-900 p-4 rounded-3xl border border-slate-100 dark:border-white/5 shadow-sm hover:shadow-lg hover:shadow-opacity-10 hover:border-opacity-30 ${accent === ColorAccent.EMERALD ? 'hover:shadow-emerald-500/10 hover:border-emerald-500' : 'hover:border-slate-400'} transition-all cursor-pointer group`}>
                                <div className={`w-10 h-10 mb-3 rounded-2xl bg-gradient-to-tr ${accent === ColorAccent.EMERALD ? 'from-emerald-100 to-slate-200 dark:from-emerald-900 dark:to-slate-800' : 'from-slate-100 to-slate-200 dark:from-slate-800 dark:to-slate-900'} flex items-center justify-center ${accentText} font-bold text-lg group-hover:scale-110 transition-transform`}>
                                    {chat.title.charAt(0).toUpperCase()}
                                </div>
                                <h4 className="font-bold text-sm truncate">{chat.title}</h4>
                                <p className="text-[10px] text-slate-400 font-mono truncate mt-1 opacity-70">{chat.id}</p>
                            </div>
                        ))
                    )}
                </div>
              </div>

              {/* Recent Drafts */}
              {drafts.length > 0 && (
                  <div>
                    <h3 className="font-bold text-lg text-slate-700 dark:text-slate-200 mb-4 px-2">Recent Drafts</h3>
                    <div className="space-y-3">
                        {drafts.map(draft => (
                            <div key={draft.id} className="glass-panel p-4 rounded-2xl flex items-center gap-4 hover:bg-white/60 dark:hover:bg-black/40 transition-colors cursor-pointer group">
                                <div className="p-3 bg-slate-100 dark:bg-white/5 rounded-xl text-slate-500"><History size={20}/></div>
                                <div className="flex-1 overflow-hidden">
                                    <p className="text-sm font-medium truncate" dangerouslySetInnerHTML={{ __html: draft.html.replace(/<[^>]*>?/gm, '') || 'Media Only' }} />
                                    <p className="text-[10px] text-slate-400 mt-1">{new Date(draft.timestamp).toLocaleTimeString()}</p>
                                </div>
                                <ChevronRight size={16} className={`text-slate-300 group-hover:${accentText.split(' ')[0]}`} />
                            </div>
                        ))}
                    </div>
                  </div>
              )}
           </div>
        )}

        {activeTab === 'compose' && (
           <div className="animate-slide-up">
             <Composer 
               botToken={botToken}
               savedChats={savedChats}
               initialChatId={selectedChatId}
               addToast={addToast}
               onSaveDraft={handleSaveDraft}
               accent={accent}
             />
           </div>
        )}

        {activeTab === 'menu' && (
           <div className="animate-slide-up pb-8">
             <BotManager 
               botToken={botToken}
               setBotToken={setBotToken}
               botUser={botUser}
               setBotUser={setBotUser}
               savedChats={savedChats}
               setSavedChats={setSavedChats}
               addToast={addToast}
               theme={theme}
               setTheme={setTheme}
             />
             
             <div className="mt-4 pb-4 px-2">
                 <h3 className="text-xs font-bold text-slate-400 uppercase mb-3">Settings</h3>
                 
                 {/* Color Accent */}
                 <div className="glass-panel p-4 rounded-3xl mb-4">
                     <div className="flex gap-4 justify-center">
                        {[ColorAccent.EMERALD, ColorAccent.BLUE, ColorAccent.VIOLET, ColorAccent.ROSE, ColorAccent.AMBER].map((c) => (
                            <button
                                key={c}
                                onClick={() => setAccent(c)}
                                className={`w-10 h-10 rounded-full flex items-center justify-center transition-transform active:scale-90 ring-offset-2 dark:ring-offset-slate-900 ${accent === c ? 'ring-2' : ''}`}
                                style={{ backgroundColor: getHexColor(c), borderColor: getHexColor(c) }}
                            >
                                <div className={`w-full h-full rounded-full bg-current ${accent === c ? '' : 'opacity-50 hover:opacity-100'}`}></div>
                            </button>
                        ))}
                     </div>
                 </div>

                 {/* Social Links Side-by-Side */}
                 <div className="flex gap-3">
                     <a href="https://t.me/JubairSensei" target="_blank" rel="noreferrer" className="flex-1 flex flex-col items-center justify-center p-3 rounded-2xl bg-blue-500/10 hover:bg-blue-500/20 text-blue-500 transition-colors border border-blue-500/20">
                         <Send size={20} className="mb-1" />
                         <span className="text-xs font-bold">Telegram</span>
                     </a>
                     <a href="https://youtube.com/@JubairSensei" target="_blank" rel="noreferrer" className="flex-1 flex flex-col items-center justify-center p-3 rounded-2xl bg-red-600/10 hover:bg-red-600/20 text-red-600 transition-colors border border-red-600/20">
                         <Youtube size={20} className="mb-1" />
                         <span className="text-xs font-bold">YouTube</span>
                     </a>
                 </div>
             </div>
           </div>
        )}
      </main>

      <ToastContainer toasts={toasts} removeToast={removeToast} />
    </div>
  );
}

const NavBtn = ({ icon: Icon, label, active, onClick, accent }: any) => {
    const accentText = getColorClass(accent, 'text');
    const accentBg = getColorClass(accent, 'bg');
    return (
        <button onClick={onClick} className={`w-full flex items-center gap-3 px-4 py-3.5 rounded-2xl transition-all ${active ? `bg-opacity-10 ${accentBg} ${accentText} font-bold` : 'text-slate-500 hover:bg-slate-50 dark:hover:bg-white/5'}`}>
            <Icon size={20} strokeWidth={active ? 2.5 : 2} />
            <span className="text-sm">{label}</span>
        </button>
    );
};

const MobileNavBtn = ({ icon: Icon, label, active, onClick, accent }: any) => {
    const accentText = getColorClass(accent, 'text');
    return (
        <button onClick={onClick} className={`flex flex-col items-center gap-1 p-2 rounded-2xl transition-all active:scale-95 ${active ? `${accentText}` : 'text-slate-400'}`}>
            <Icon size={24} strokeWidth={active ? 2.5 : 2} className={active ? 'drop-shadow-lg' : ''} />
            <span className="text-[10px] font-bold tracking-wide">{label}</span>
        </button>
    );
};

export default App;