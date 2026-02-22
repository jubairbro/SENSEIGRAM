# SenseiGram - Telegram Bot Manager

A native Android application for managing Telegram Bots. **No server required.**

[![Download](https://img.shields.io/github/v/release/jubairbro/SENSEIGRAM?label=Download)](https://github.com/jubairbro/SENSEIGRAM/releases/latest)

---

## Features

- Connect bot using API token from @BotFather
- Send **Text**, **Photo**, **Video**, **Document** messages
- Upload media from device or via URL
- HTML formatting: **bold**, *italic*, `code`, links, spoiler
- Silent messages, protect content, media spoiler
- Visual inline keyboard builder with 5 color styles
- Edit sent messages (text, caption, buttons)
- Save favorite channels/groups for quick access
- Draft messages for later
- 4 Theme modes: Light, Dark, AMOLED, System
- 5 Accent colors
- Material Design 3

---

## Download

Get the latest APK from [Releases](https://github.com/jubairbro/SENSEIGRAM/releases/latest)

---

## Tech Stack

| Component | Technology |
|-----------|-----------|
| Language | Kotlin |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 34 (Android 14) |
| Architecture | MVVM + ViewBinding |
| Networking | OkHttp |
| CI/CD | GitHub Actions |

---

## Build

```bash
./gradlew assembleDebug
./gradlew assembleRelease
```
