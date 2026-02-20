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
APK স্বয়ংক্রিয়ভাবে GitHub Actions দ্বারা build হবে। Build complete হলে [Actions](https://github.com/jubairbro/SENSEIGRAM/actions) page থেকে APK ডাউনলোড করতে পারবেন।

> **Note**: প্রথম build হতে কিছু সময় লাগতে পারে। [Actions tab](https://github.com/jubairbro/SENSEIGRAM/actions) এ গিয়ে build status দেখুন।

### Requirements
- Android 7.0 (API 24) or higher
- Internet connection
- Telegram Bot Token (from [@BotFather](https://t.me/BotFather))

---

## Installation

1. Download the APK from [Releases](https://github.com/jubairbro/SENSEIGRAM/releases/latest)
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
1. Go to Menu → Targets
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
# Clone the repository
git clone https://github.com/jubairbro/SENSEIGRAM.git
cd SENSEIGRAM

# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Install on connected device
./gradlew installDebug
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
├── services/            # Android services
└── SenseiGramApp.kt     # Application class
```

---

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## Support

- **Telegram Channel**: [Join Here](https://t.me/+5ygHfkZxVBc0Mjdl)
- **Issues**: [GitHub Issues](https://github.com/jubairbro/SENSEIGRAM/issues)
