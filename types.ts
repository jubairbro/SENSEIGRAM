export enum AppTheme {
  LIGHT = 'light',
  DARK = 'dark',
  AMOLED = 'amoled',
  SYSTEM = 'system',
}

export enum ColorAccent {
  EMERALD = 'emerald',
  BLUE = 'blue',
  VIOLET = 'violet',
  ROSE = 'rose',
  AMBER = 'amber',
}

export enum MediaType {
  TEXT = 'text',
  PHOTO = 'photo',
  VIDEO = 'video',
  DOCUMENT = 'document',
}

export interface TelegramUser {
  id: number;
  is_bot: boolean;
  first_name: string;
  username?: string;
  can_join_groups?: boolean;
  can_read_all_group_messages?: boolean;
  supports_inline_queries?: boolean;
}

export interface TelegramChat {
  id: number;
  title?: string;
  username?: string;
  type: string;
  photo?: {
    small_file_id: string;
    big_file_id: string;
  };
}

export interface SavedChat {
  id: number;
  title: string;
  type: string;
  addedAt: number;
}

export interface RemoteConfig {
  api_id: string | null;
  api_hash: string | null;
}

export interface Announcement {
  show: boolean;
  title: string;
  text: string;
  buttonText?: string;
  buttonUrl?: string;
}

export interface InlineButton {
  text: string;
  url?: string;
  callback_data?: string;
}

export interface MessageDraft {
  id: string;
  chatId: string;
  html: string;
  buttons: InlineButton[][];
  timestamp: number;
}