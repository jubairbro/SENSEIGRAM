<div align="center">
    <img src="https://github.com/jubairbro/Faw/raw/refs/heads/main/photos/Icons/app_icon.png" width="120" height="120" alt="SenseiGram Logo">
    
## SenseiGram
    
**Telegram Bot Manager**
    
A serverless Telegram bot management tool for Android.

[![Build APK](https://github.com/jubairbro/SENSEIGRAM/actions/workflows/build.yml/badge.svg)](https://github.com/jubairbro/SENSEIGRAM/actions/workflows/build.yml)
[![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg)](https://android-arsenal.com/api?level=24)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22-purple.svg)](https://kotlinlang.org)
</div>

---

## Features

- **Bot Connection** - Connect using Bot API Token with validation
- **Send Messages** - Send text messages to channels, groups, and private chats
- **Saved Chats** - Save frequently used chats for quick access
- **Subscription Dialog** - Optional channel subscription prompt

---

## Download

### Latest Build
Download APK from [Actions](https://github.com/jubairbro/SENSEIGRAM/actions) page.

1. Go to [Actions](https://github.com/jubairbro/SENSEIGRAM/actions)
2. Click on the latest successful build
3. Scroll down to "Artifacts" section
4. Download `senseigram-debug`

### Requirements
- Android 7.0 (API 24) or higher
- Internet connection
- Telegram Bot Token (from [@BotFather](https://t.me/BotFather))

---

## How to Use

### 1. Create a Bot
1. Open Telegram and search for [@BotFather](https://t.me/BotFather)
2. Send `/newbot` command
3. Follow instructions to create your bot
4. Copy the API Token

### 2. Connect Bot
1. Open SenseiGram
2. Paste your bot token
3. Tap "Connect"

### 3. Add Chats
1. Tap the + button
2. Enter channel/group username or ID
3. Chat will be saved for quick access

### 4. Send Messages
1. Tap "Compose Message" or select a saved chat
2. Enter your message
3. Tap "Send"

---

## Building from Source

```bash
git clone https://github.com/jubairbro/SENSEIGRAM.git
cd SENSEIGRAM
./gradlew assembleDebug
```

APK will be in `app/build/outputs/apk/debug/app-debug.apk`

---

## Tech Stack

- **Language**: Kotlin 1.9.22
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **Build**: Gradle 8.6 + AGP 8.4.0
- **Libraries**: OkHttp, Kotlinx Serialization, Coroutines, Material Design 3

---

## Support

- **Telegram Channel**: [Join Here](https://t.me/+5ygHfkZxVBc0Mjdl)
- **Issues**: [GitHub Issues](https://github.com/jubairbro/SENSEIGRAM/issues)
