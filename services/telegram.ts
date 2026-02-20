import { TelegramUser, TelegramChat, InlineButton } from '../types';

const BASE_URL = 'https://api.telegram.org/bot';

export class TelegramService {
  private token: string;

  constructor(token: string) {
    this.token = token;
  }

  private async request<T>(method: string, body?: any, isFormData: boolean = false): Promise<T> {
    const headers: HeadersInit = {};
    if (!isFormData) {
      headers['Content-Type'] = 'application/json';
    }

    const response = await fetch(`${BASE_URL}${this.token}/${method}`, {
      method: 'POST',
      headers,
      body: isFormData ? body : JSON.stringify(body),
    });

    const data = await response.json();
    if (!data.ok) {
      throw new Error(data.description || 'Telegram API Error');
    }
    return data.result;
  }

  async getMe(): Promise<TelegramUser> {
    return this.request<TelegramUser>('getMe');
  }

  async getChat(chatId: string | number): Promise<TelegramChat> {
    return this.request<TelegramChat>('getChat', { chat_id: chatId });
  }

  // --- Sending ---

  async sendMessage(chatId: string | number, text: string, parseMode: string, buttons: InlineButton[][], silent: boolean, protect: boolean, disableWebPreview: boolean) {
    return this.request('sendMessage', {
      chat_id: chatId,
      text,
      parse_mode: parseMode,
      disable_notification: silent,
      protect_content: protect,
      disable_web_page_preview: disableWebPreview,
      reply_markup: buttons?.length ? { inline_keyboard: buttons } : undefined
    });
  }

  async sendPhoto(chatId: string | number, photo: File | string, caption: string, parseMode: string, buttons: InlineButton[][], silent: boolean, protect: boolean, hasSpoiler: boolean) {
    const formData = new FormData();
    formData.append('chat_id', String(chatId));
    
    // Explicitly append name if it's a file to preserve emojis/chars
    if (photo instanceof File) {
        formData.append('photo', photo, photo.name);
    } else {
        formData.append('photo', photo);
    }

    if(caption) formData.append('caption', caption);
    if(parseMode) formData.append('parse_mode', parseMode);
    if(silent) formData.append('disable_notification', 'true');
    if(protect) formData.append('protect_content', 'true');
    if(hasSpoiler) formData.append('has_spoiler', 'true');
    if(buttons?.length) formData.append('reply_markup', JSON.stringify({ inline_keyboard: buttons }));
    
    return this.request('sendPhoto', formData, true);
  }

  async sendVideo(chatId: string | number, video: File | string, caption: string, parseMode: string, buttons: InlineButton[][], silent: boolean, protect: boolean, hasSpoiler: boolean) {
    const formData = new FormData();
    formData.append('chat_id', String(chatId));
    
    if (video instanceof File) {
        formData.append('video', video, video.name);
    } else {
        formData.append('video', video);
    }

    if(caption) formData.append('caption', caption);
    if(parseMode) formData.append('parse_mode', parseMode);
    if(silent) formData.append('disable_notification', 'true');
    if(protect) formData.append('protect_content', 'true');
    if(hasSpoiler) formData.append('has_spoiler', 'true');
    if(buttons?.length) formData.append('reply_markup', JSON.stringify({ inline_keyboard: buttons }));

    return this.request('sendVideo', formData, true);
  }

  async sendDocument(chatId: string | number, document: File | string, caption: string, parseMode: string, buttons: InlineButton[][], silent: boolean, protect: boolean) {
    const formData = new FormData();
    formData.append('chat_id', String(chatId));
    
    // Explicit filename passed here
    if (document instanceof File) {
        formData.append('document', document, document.name);
    } else {
        formData.append('document', document);
    }

    if(caption) formData.append('caption', caption);
    if(parseMode) formData.append('parse_mode', parseMode);
    if(silent) formData.append('disable_notification', 'true');
    if(protect) formData.append('protect_content', 'true');
    if(buttons?.length) formData.append('reply_markup', JSON.stringify({ inline_keyboard: buttons }));

    return this.request('sendDocument', formData, true);
  }

  // --- Editing ---

  /**
   * For editing, if buttons is undefined, we don't send reply_markup (keeps existing).
   * If buttons is [], we send empty inline_keyboard (removes buttons).
   */
  async editMessageText(chatId: string | number, messageId: number, text: string, parseMode: string, buttons?: InlineButton[][]) {
    const body: any = {
      chat_id: chatId,
      message_id: messageId,
      text,
      parse_mode: parseMode,
    };
    if (buttons !== undefined) {
        body.reply_markup = { inline_keyboard: buttons };
    }
    return this.request('editMessageText', body);
  }

  async editMessageCaption(chatId: string | number, messageId: number, caption: string, parseMode: string, buttons?: InlineButton[][]) {
    const body: any = {
      chat_id: chatId,
      message_id: messageId,
      caption,
      parse_mode: parseMode,
    };
    if (buttons !== undefined) {
        body.reply_markup = { inline_keyboard: buttons };
    }
    return this.request('editMessageCaption', body);
  }

  async editMessageReplyMarkup(chatId: string | number, messageId: number, buttons: InlineButton[][]) {
    return this.request('editMessageReplyMarkup', {
      chat_id: chatId,
      message_id: messageId,
      reply_markup: { inline_keyboard: buttons }
    });
  }
}