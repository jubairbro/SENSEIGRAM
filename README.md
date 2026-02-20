<div align="center">
    <img src="https://github.com/jubairbro/Faw/raw/refs/heads/main/photos/Icons/app_icon.png" width="120" height="120" alt="SenseiGram Logo">
    
## SenseiGram
    
**Telegram Bot Manager**
    
A professional serverless Telegram bot management tool for Android.

[![Build APK](https://github.com/jubairbro/SENSEIGRAM/actions/workflows/build.yml/badge.svg)](https://github.com/jubairbro/SENSEIGRAM/actions/workflows/build.yml)
[![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg)](https://android-arsenal.com/api?level=24)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22-purple.svg)](https://kotlinlang.org)
</div>

---

## Features

### Bot Connection
- Connect using Bot API Token
- Token validation
- Secure local storage with encryption
- Bot info display

### Message Composer
- Text message support with HTML formatting
- Media attachments (Photo, Video, Document)
- Inline keyboard builder
- Silent mode
- Content protection
- Draft saving

### Saved Chats
- Save frequently used channels/groups
- Quick access to saved targets
- Auto-detect chat type

### Theme & Customization
- Light theme
- Dark theme
- AMOLED theme (pure black)
- System default
- 5 accent colors (Emerald, Blue, Violet, Rose, Amber)
- Glassmorphism UI design

### Other Features
- Offline detection
- Remote announcement system
- Subscription dialog
- First-time tutorial

---

## Download

### Build Status
APK is automatically built by GitHub Actions. Once build is complete, download APK from [Actions](https://github.com/jubairbro/SENSEIGRAM/actions) page.

> **Note**: First build may take a few minutes. Check [Actions tab](https://github.com/jubairbro/SENSEIGRAM/actions) for build status.

### Requirements
- Android 7.0 (API 24) or higher
- Internet connection
- Telegram Bot Token (from [@BotFather](https://t.me/BotFather))

---

## Installation

1. Download the APK from [Actions](https://github.com/jubairbro/SENSEIGRAM/actions) or [Releases](https://github.com/jubairbro/SENSEIGRAM/releases)
2. Enable "Install from Unknown Sources" in your device settings
3. Open the downloaded APK and install
4. Launch SenseiGram and enter your bot token

---

## Getting Started

### 1. Create a Bot
1. Open Telegram and search for [@BotFather](https://t.me/BotFather)
2. Send `/newbot` command
3. Follow the instructions to create your bot
4. Copy the API Token

### 2. Connect Your Bot
1. Open SenseiGram
2. Enter your bot token
3. Tap "Connect"
4. Your bot is now connected!

### 3. Add Target Chats
1. Go to Menu -> Targets
2. Enter channel/group username or ID
3. Tap the + button to save

### 4. Send Messages
1. Go to Compose tab
2. Select a target chat
3. Write your message
4. Attach media if needed
5. Tap Send!

---

## Building from Source

### Prerequisites
- Android Studio Hedgehog or later
- JDK 17
- Android SDK 34

### Build Steps
```bash
git clone https://github.com/jubairbro/SENSEIGRAM.git
cd SENSEIGRAM
./gradlew assembleDebug
```

---

## Tech Stack

- **Language**: Kotlin 1.9.22
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **Architecture**: MVVM with ViewBinding

### Libraries
- [Retrofit](https://square.github.io/retrofit/) - HTTP client
- [OkHttp](https://square.github.io/okhttp/) - Network layer
- [Glide](https://github.com/bumptech/glide) - Image loading
- [DataStore](https://developer.android.com/topic/libraries/architecture/datastore) - Preferences
- [Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) - Async operations
- [Navigation Component](https://developer.android.com/guide/navigation) - Navigation
- [Material Design 3](https://m3.material.io/) - UI components

---

## Project Structure

```
app/src/main/java/com/senseigram/
├── data/
│   ├── model/           # Data classes
│   ├── local/           # Local storage (DataStore)
│   └── remote/          # API services
├── ui/
│   ├── activities/      # Activities
│   ├── fragments/       # Fragments
│   ├── adapters/        # RecyclerView adapters
│   └── viewmodel/       # ViewModels
├── utils/               # Utility classes
└── SenseiGramApp.kt     # Application class
```

---

## Support

- **Telegram Channel**: [Join Here](https://t.me/+5ygHfkZxVBc0Mjdl)
- **Issues**: [GitHub Issues](https://github.com/jubairbro/SENSEIGRAM/issues)
