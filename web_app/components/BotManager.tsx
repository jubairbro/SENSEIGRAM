import React, { useState } from 'react';
import { Eye, EyeOff, Plus, Users, Bot, Key, Trash2, Smartphone, Moon, Sun, Monitor, Search, User } from 'lucide-react';
import { TelegramService } from '../services/telegram';
import { SavedChat, TelegramUser, AppTheme, ColorAccent, TelegramChat } from '../types';
import { getColorClass } from '../utils/colors';

interface Props {
  botToken: string;
  setBotToken: (token: string) => void;
  botUser: TelegramUser | null;
  setBotUser: (user: TelegramUser | null) => void;
  savedChats: SavedChat[];
  setSavedChats: (chats: SavedChat[]) => void;
  addToast: (type: 'success' | 'error' | 'info', msg: string) => void;
  theme: AppTheme;
  setTheme: (theme: AppTheme) => void;
}

export const BotManager: React.FC<Props> = ({
  botToken,
  setBotToken,
  botUser,
  setBotUser,
  savedChats,
  setSavedChats,
  addToast,
  theme,
  setTheme
}) => {
  const [showToken, setShowToken] = useState(false);
  const [newChatId, setNewChatId] = useState('');
  const [isLoadingChat, setIsLoadingChat] = useState(false);
  const [isValidatingBot, setIsValidatingBot] = useState(false);
  
  // User Info Lookup
  const [lookupId, setLookupId] = useState('');
  const [lookupResult, setLookupResult] = useState<TelegramChat | null>(null);
  const [isLookingUp, setIsLookingUp] = useState(false);

  const storedAccent = (localStorage.getItem('accent') as ColorAccent) || ColorAccent.EMERALD;
  const accentText = getColorClass(storedAccent, 'text');
  const accentBg = getColorClass(storedAccent, 'bg');
  const accentFrom = getColorClass(storedAccent, 'from');
  const accentTo = getColorClass(storedAccent, 'to');
  const accentBorder = getColorClass(storedAccent, 'border');
  const accentShadow = getColorClass(storedAccent, 'shadow');
  const accentIconBg = getColorClass(storedAccent, 'iconBg');

  const validateBot = async () => {
    if (!botToken) return;
    setIsValidatingBot(true);
    try {
      const api = new TelegramService(botToken);
      const user = await api.getMe();
      setBotUser(user);
      addToast('success', `Connected as ${user.first_name}`);
    } catch (e: any) {
      addToast('error', `Invalid Token: ${e.message}`);
      setBotUser(null);
    } finally {
      setIsValidatingBot(false);
    }
  };

  const handleLookup = async () => {
      if(!lookupId || !botToken) return;
      setIsLookingUp(true);
      setLookupResult(null);
      
      let target = lookupId.trim();
      if (!target.startsWith('@') && !target.startsWith('-') && isNaN(Number(target))) {
         // If it's just a string like "username", prepend @
         target = '@' + target;
      }
      
      try {
          const api = new TelegramService(botToken);
          const res = await api.getChat(target);
          setLookupResult(res);
      } catch(e: any) {
          addToast('error', `Not Found: ${e.message}`);
      } finally {
          setIsLookingUp(false);
      }
  };

  const handleAddChat = async () => {
    if (!newChatId || !botToken) return;
    setIsLoadingChat(true);
    
    // Auto-fix username format
    let target = newChatId.trim();
    if (!target.startsWith('@') && !target.startsWith('-') && isNaN(Number(target))) {
        target = '@' + target;
    }

    try {
      const api = new TelegramService(botToken);
      const chat = await api.getChat(target);
      
      const exists = savedChats.find(c => c.id === chat.id);
      if (exists) {
        addToast('info', 'Chat already saved');
        return;
      }

      const newSavedChat: SavedChat = {
        id: chat.id,
        title: chat.title || chat.username || 'Unknown Chat',
        type: chat.type,
        addedAt: Date.now(),
      };

      setSavedChats([...savedChats, newSavedChat]);
      addToast('success', 'Channel Added!');
      setNewChatId('');
    } catch (e: any) {
      addToast('error', `Not Found: ${e.message}`);
    } finally {
      setIsLoadingChat(false);
    }
  };

  const removeChat = (id: number) => {
    if(confirm('Remove this channel?')) {
        setSavedChats(savedChats.filter(c => c.id !== id));
    }
  };

  return (
    <div className="space-y-6 animate-fade-in">
      <div className="flex items-center justify-between px-2">
         <h1 className={`text-3xl font-bold bg-gradient-to-r ${accentFrom} ${accentTo} bg-clip-text text-transparent`}>Menu</h1>
         <span className={`text-xs opacity-60 font-mono tracking-widest uppercase ${accentText}`}>System Config</span>
      </div>

      {/* Theme Card */}
      <div className="glass-panel p-6 rounded-3xl shadow-sm">
        <h2 className="text-sm font-bold mb-4 uppercase text-slate-400 tracking-wider">Appearance</h2>
        <div className="grid grid-cols-4 gap-2">
            {[
                { id: AppTheme.LIGHT, icon: Sun, label: 'Light' },
                { id: AppTheme.DARK, icon: Moon, label: 'Dark' },
                { id: AppTheme.AMOLED, icon: Smartphone, label: 'Amoled' },
                { id: AppTheme.SYSTEM, icon: Monitor, label: 'System' },
            ].map((t) => (
                <button 
                    key={t.id}
                    onClick={() => setTheme(t.id)}
                    className={`flex flex-col items-center gap-2 p-3 rounded-2xl border transition-all duration-300 ${theme === t.id ? `${accentBorder} ${accentIconBg} shadow-lg ${accentShadow}` : 'border-transparent hover:bg-slate-100 dark:hover:bg-slate-800'}`}
                >
                    <t.icon size={20} className={theme === t.id ? accentText.split(' ')[0] : 'text-slate-400'} />
                    <span className={`text-[10px] font-bold ${theme === t.id ? accentText : 'text-slate-500'}`}>{t.label}</span>
                </button>
            ))}
        </div>
      </div>

      {/* Bot Setup */}
      <div className="glass-panel p-6 rounded-3xl shadow-sm">
        <h2 className="text-sm font-bold mb-4 uppercase text-slate-400 tracking-wider flex items-center gap-2">
          <Bot size={16} /> Connection
        </h2>
        
        <div className="space-y-4">
          <div className="relative">
            <div className="flex gap-2">
              <div className="relative flex-1">
                <input
                  type={showToken ? 'text' : 'password'}
                  value={botToken}
                  onChange={(e) => setBotToken(e.target.value)}
                  placeholder="Bot Token..."
                  className={`w-full pl-10 pr-4 py-3.5 rounded-2xl bg-slate-100 dark:bg-black/40 border-transparent focus:bg-white dark:focus:bg-slate-900 border focus:${accentBorder} focus:border-opacity-50 transition-all outline-none font-mono text-sm`}
                />
                <Key className="absolute left-3 top-3.5 text-slate-400" size={18} />
              </div>
              <button
                onClick={() => setShowToken(!showToken)}
                className={`p-3.5 bg-slate-100 dark:bg-slate-800/50 rounded-2xl hover:${accentText} transition-colors`}
              >
                {showToken ? <EyeOff size={20} /> : <Eye size={20} />}
              </button>
            </div>
            <button
                onClick={validateBot}
                disabled={isValidatingBot}
                className="w-full mt-3 py-3 bg-slate-900 dark:bg-white text-white dark:text-black rounded-xl font-bold text-sm hover:scale-[1.02] active:scale-95 transition-all disabled:opacity-50"
              >
                {isValidatingBot ? 'Validating...' : 'Check Connection'}
            </button>
          </div>

          {botUser && (
            <div className={`flex items-center gap-3 p-4 ${accentIconBg} border border-opacity-20 ${accentBorder} rounded-2xl animate-fade-in`}>
              <div className={`w-2.5 h-2.5 rounded-full ${accentBg} animate-pulse`}></div>
              <span className={`text-sm font-bold ${accentText}`}>
                Connected: @{botUser.username}
              </span>
            </div>
          )}
        </div>
      </div>
      
      {/* User Lookup Tool */}
      <div className="glass-panel p-6 rounded-3xl shadow-sm">
          <h2 className="text-sm font-bold mb-4 uppercase text-slate-400 tracking-wider flex items-center gap-2">
            <Search size={16} /> Lookup Info
          </h2>
          
          <div className="flex gap-2">
            <input 
                type="text" 
                placeholder="User ID or @username" 
                value={lookupId}
                onChange={(e) => setLookupId(e.target.value)}
                onKeyDown={(e) => e.key === 'Enter' && handleLookup()}
                className={`flex-1 px-4 py-3.5 rounded-2xl bg-slate-100 dark:bg-black/40 border-transparent focus:bg-white dark:focus:bg-slate-900 border focus:${accentBorder} focus:border-opacity-50 transition-all outline-none`}
            />
            <button 
                onClick={handleLookup}
                disabled={isLookingUp || !botToken}
                className={`flex items-center justify-center w-12 ${accentBg} text-white rounded-2xl shadow-lg ${accentShadow} transition-all active:scale-90 disabled:opacity-50 hover:brightness-110`}
            >
                <Search size={20} />
            </button>
          </div>
          
          {lookupResult && (
              <div className="mt-4 p-4 rounded-2xl bg-slate-100 dark:bg-black/20 border border-slate-200 dark:border-white/5 animate-fade-in">
                  <div className="flex items-center gap-3 mb-3">
                      <div className={`w-10 h-10 rounded-full ${accentIconBg} ${accentText} flex items-center justify-center`}>
                          <User size={20} />
                      </div>
                      <div className="overflow-hidden">
                          <h3 className="font-bold text-slate-800 dark:text-white truncate">{lookupResult.title || lookupResult.username || 'Unknown'}</h3>
                          <p className="text-xs text-slate-400 uppercase font-bold tracking-wider">{lookupResult.type}</p>
                      </div>
                  </div>
                  <div className="space-y-1">
                      <div className="flex justify-between text-xs">
                          <span className="text-slate-500">ID</span>
                          <span className="font-mono select-all text-slate-700 dark:text-slate-300">{lookupResult.id}</span>
                      </div>
                      {lookupResult.username && (
                          <div className="flex justify-between text-xs">
                              <span className="text-slate-500">Username</span>
                              <span className="font-mono select-all text-blue-500">@{lookupResult.username}</span>
                          </div>
                      )}
                      {lookupResult.title && (
                          <div className="flex justify-between text-xs">
                              <span className="text-slate-500">Title</span>
                              <span className="font-medium text-slate-700 dark:text-slate-300 truncate max-w-[150px]">{lookupResult.title}</span>
                          </div>
                      )}
                  </div>
              </div>
          )}
      </div>

      {/* Channel Manager */}
      <div className="glass-panel p-6 rounded-3xl shadow-sm">
        <h2 className="text-sm font-bold mb-4 uppercase text-slate-400 tracking-wider flex items-center gap-2">
          <Users size={16} /> Targets
        </h2>

        <div className="flex gap-2 mb-6">
          <input
            type="text"
            value={newChatId}
            onChange={(e) => setNewChatId(e.target.value)}
            placeholder="@channel or ID"
            className={`flex-1 px-4 py-3.5 rounded-2xl bg-slate-100 dark:bg-black/40 border-transparent focus:bg-white dark:focus:bg-slate-900 border focus:${accentBorder} focus:border-opacity-50 transition-all outline-none`}
          />
          <button
            onClick={handleAddChat}
            disabled={isLoadingChat || !botToken}
            className={`flex items-center justify-center w-12 ${accentBg} text-white rounded-2xl shadow-lg ${accentShadow} transition-all active:scale-90 disabled:opacity-50 hover:brightness-110`}
          >
            <Plus size={24} />
          </button>
        </div>

        <div className="space-y-3">
          {savedChats.length === 0 ? (
            <div className="text-center py-6 text-slate-400 text-xs bg-slate-100/50 dark:bg-slate-900/30 rounded-2xl border border-dashed border-slate-300 dark:border-slate-700">
              No channels added yet.
            </div>
          ) : (
            savedChats.map((chat) => (
              <div
                key={chat.id}
                className={`flex items-center justify-between p-3 pl-4 rounded-2xl bg-white/50 dark:bg-black/20 border border-slate-100 dark:border-white/5 hover:${accentBorder} hover:border-opacity-30 transition-colors`}
              >
                <div className="flex-1 overflow-hidden">
                  <h3 className="font-bold text-sm truncate text-slate-700 dark:text-slate-200">{chat.title}</h3>
                  <div className="flex items-center gap-2 text-[10px] text-slate-400 font-mono">
                    <span className="bg-slate-200 dark:bg-white/10 px-1.5 py-0.5 rounded capitalize">{chat.type}</span>
                    <span className="truncate">{chat.id}</span>
                  </div>
                </div>
                <button 
                  onClick={() => removeChat(chat.id)}
                  className="p-2 text-rose-500 hover:bg-rose-500/10 rounded-xl transition-colors"
                >
                  <Trash2 size={18} />
                </button>
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
};