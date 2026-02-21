import { RemoteConfig } from '../types';

export const fetchRemoteConfig = async (): Promise<RemoteConfig> => {
  try {
    const response = await fetch('https://raw.githubusercontent.com/jubairbro/Faw/refs/heads/main/folder/config.ini');
    if (!response.ok) throw new Error('Failed to fetch config');
    
    const text = await response.text();
    const config: RemoteConfig = {
      api_id: null,
      api_hash: null,
    };

    // Simple INI parser
    const lines = text.split('\n');
    for (const line of lines) {
      const trimmed = line.trim();
      if (!trimmed || trimmed.startsWith(';')) continue;
      
      const parts = trimmed.split('=');
      if (parts.length === 2) {
        const key = parts[0].trim();
        const value = parts[1].trim();
        
        if (key === 'api_id') config.api_id = value;
        if (key === 'api_hash') config.api_hash = value;
      }
    }
    
    return config;
  } catch (error) {
    console.error('Config fetch error:', error);
    return { api_id: null, api_hash: null };
  }
};