# SenseiGram - Telegram Bot Manager

A powerful, native Android application for managing Telegram Bots directly from your phone. **No server required.** Built with Kotlin and Modern Android Development practices.

[![Android CI/CD](https://github.com/jubairbro/SENSEIGRAM/actions/workflows/android.yml/badge.svg)](https://github.com/jubairbro/SENSEIGRAM/actions)
[![Download](https://img.shields.io/github/v/release/jubairbro/SENSEIGRAM?label=Download)](https://github.com/jubairbro/SENSEIGRAM/releases/latest)

---

## Features

### Bot Management
- Connect bot using API token from @BotFather
- View connected bot info (name, username)
- One-tap disconnect/logout

### Messaging
- Send **Text**, **Photo**, **Video**, **Document** messages
- Upload media from device or via URL
- HTML formatting: **bold**, *italic*, `code`, [links](url), spoiler
- Silent messages (no notification)
- Protect content (no forward/save)
- Media spoiler overlay
- Disable web preview

### Inline Keyboard Builder
- Visual button builder with live preview
- Add buttons to row or create new row
- 5 color styles: Default, Primary, Success, Danger, Warning

### Message Editing
- Edit message text/caption
- Edit inline buttons only

### Chat Management
- Save favorite channels/groups/users
- Quick selection in composer
- Auto-validate chat ID before saving

### Drafts
- Save message drafts for later
- Auto-load in composer

### UI/UX
- Material Design 3
- 4 Theme modes: Light, Dark, AMOLED, System
- 5 Accent colors: Emerald, Blue, Violet, Rose, Amber
- Network status indicator
- Editable user name on welcome card

---

## Tech Stack

| Component | Technology |
|-----------|-----------|
| Language | Kotlin 1.9.22 |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 34 (Android 14) |
| Architecture | MVVM + ViewBinding |
| Networking | OkHttp 4.12 |
| Storage | SharedPreferences |
| Async | Kotlin Coroutines |
| CI/CD | GitHub Actions |

---

## Download

Get the latest APK from [Releases](https://github.com/jubairbro/SENSEIGRAM/releases/latest)

---

## Join & Support

- **Telegram:** [@JubairSensei](https://t.me/JubairSensei)
- **YouTube:** [@JubairSensei](https://youtube.com/@JubairSensei)
- **Support Group:** [@JubairZ](https://t.me/all_to_bn)

---

## Build

```bash
./gradlew assembleDebug      # Debug APK
./gradlew assembleRelease    # Release APK
./gradlew lintDebug          # Lint check
```

---

## License

This project is provided as-is for educational and personal use.
