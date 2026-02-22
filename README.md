# SenseiGram - Telegram Bot Manager

A powerful, native Android application for managing Telegram Bots directly from your phone. No server required. Built with Kotlin and Modern Android Development practices.

---

## 50+ Features

### Bot & Account Management
| # | Feature | Description |
|---|---------|-------------|
| 1 | Multi-Bot Token Support | Store and switch between multiple bot tokens |
| 2 | Token Verification | Validates bot token via `getMe` API before saving |
| 3 | Bot Profile Display | Shows bot name and username after connecting |
| 4 | Quick Disconnect | One-tap logout and token clearing |
| 5 | Persistent Login | Auto-login on app restart if token exists |
| 6 | Secure Token Storage | Token stored in private SharedPreferences |

### Messaging Engine
| # | Feature | Description |
|---|---------|-------------|
| 7 | Send Text Messages | Plain text or formatted messages to any chat |
| 8 | HTML Parse Mode | Full HTML formatting: bold, italic, code, links, spoiler |
| 9 | Send Photos (File) | Upload photos from device storage |
| 10 | Send Photos (URL) | Send photos by providing a direct URL |
| 11 | Send Videos (File) | Upload videos from device storage |
| 12 | Send Videos (URL) | Send videos by providing a direct URL |
| 13 | Send Documents (File) | Upload any file type as a document |
| 14 | Send Documents (URL) | Send documents by URL |
| 15 | Media Captions | Add formatted captions to photos, videos, documents |
| 16 | Silent Messages | Send without notification sound (`disable_notification`) |
| 17 | Protect Content | Prevent forwarding and saving (`protect_content`) |
| 18 | Media Spoiler | Hide media behind a spoiler overlay (`has_spoiler`) |
| 19 | Disable Link Preview | Send text without URL preview expansion |
| 20 | Message to Any Chat | Send to channels, groups, supergroups, or private chats |

### Inline Keyboard Builder
| # | Feature | Description |
|---|---------|-------------|
| 21 | Visual Button Builder | Add/remove inline buttons with a visual editor |
| 22 | URL Buttons | Buttons that open external links |
| 23 | Callback Buttons | Buttons with `callback_data` for bot interaction |
| 24 | Colored Button Preview | Real-time preview with 5 color styles |
| 25 | Default Color (Gray) | Standard Telegram button appearance |
| 26 | Primary Color (Blue) | Blue-styled button for primary actions |
| 27 | Success Color (Green) | Green-styled button for confirmations |
| 28 | Danger Color (Red) | Red-styled button for destructive actions |
| 29 | Warning Color (Yellow) | Yellow-styled button for warnings |
| 30 | Multi-Row Keyboards | Each button rendered as a separate keyboard row |

### Message Editing
| # | Feature | Description |
|---|---------|-------------|
| 31 | Edit Message Text | Update text of already-sent messages |
| 32 | Edit Message Caption | Update captions on media messages |
| 33 | Edit Reply Markup | Update inline keyboard without changing message content |

### Chat Management
| # | Feature | Description |
|---|---------|-------------|
| 34 | Save Chats | Save frequently-used chat IDs with custom names |
| 35 | Quick Chat Selection | Tap a saved chat to instantly open composer |
| 36 | Edit Saved Chats | Update name or ID of saved chats |
| 37 | Delete Saved Chats | Remove chats with confirmation dialog |
| 38 | Chat Validation | Verifies chat ID via `getChat` API before saving |
| 39 | Chat Type Detection | Auto-detects channel, group, supergroup, private |
| 40 | Sort by Recent | Saved chats sorted by last-used time |

### Drafts System
| # | Feature | Description |
|---|---------|-------------|
| 41 | Save Drafts | Save message + buttons as a draft for later |
| 42 | Draft Preview | See message preview and target chat in draft list |
| 43 | Load Draft | Tap a draft to open it in composer |
| 44 | Delete Drafts | Remove unwanted drafts |
| 45 | Draft Limit | Automatically keeps only the 10 most recent drafts |

### User Interface
| # | Feature | Description |
|---|---------|-------------|
| 46 | Material Design 3 | Modern UI with Material Components |
| 47 | Clean Dashboard | Welcome card, saved chats, and drafts in one view |
| 48 | FAB Compose Button | Floating action button for quick message composition |
| 49 | Message Type Chips | Filter chips for Text, Photo, Video, Document |
| 50 | Character Counter | Live character count (max 4096 for Telegram) |
| 51 | File Picker | Native Android file picker for media upload |
| 52 | Progress Indicators | Loading spinners during API calls |
| 53 | Error Toasts | User-friendly error messages for all API failures |
| 54 | Accent Color System | 5 accent colors (Emerald, Blue, Violet, Rose, Amber) |

### Architecture & Code Quality
| # | Feature | Description |
|---|---------|-------------|
| 55 | MVVM Architecture | Clean separation of UI and business logic |
| 56 | ViewBinding | Type-safe view access, no `findViewById` |
| 57 | Kotlin Coroutines | Non-blocking async API calls |
| 58 | OkHttp Client | Robust HTTP client with configurable timeouts |
| 59 | JSON Parsing | Native `org.json` for lightweight parsing |
| 60 | GitHub Actions CI/CD | Automated build, lint, test, and release pipeline |

---

## Tech Stack

| Component | Technology |
|-----------|-----------|
| Language | Kotlin 1.9.22 |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 34 (Android 14) |
| Architecture | MVVM |
| Networking | OkHttp 4.12 |
| UI | Material Components + ViewBinding |
| Storage | SharedPreferences |
| Async | Kotlin Coroutines |
| CI/CD | GitHub Actions |

## Project Structure

```
app/src/main/
├── java/com/senseigram/
│   ├── App.kt                          # Application class
│   ├── data/
│   │   ├── Prefs.kt                    # Preferences, saved chats, drafts, models
│   │   └── TelegramApi.kt             # All Telegram Bot API calls + AccentColors
│   ├── ui/
│   │   ├── login/LoginActivity.kt      # Bot token login screen
│   │   ├── main/MainActivity.kt        # Dashboard with saved chats & drafts
│   │   ├── compose/ComposeActivity.kt  # Message composer with media & buttons
│   │   └── adapters/                   # RecyclerView adapters
│   │       ├── ButtonsAdapter.kt       # Inline button builder adapter
│   │       ├── ChatsAdapter.kt         # Saved chats list adapter
│   │       └── DraftsAdapter.kt        # Drafts list adapter
├── res/
│   ├── layout/                         # All XML layouts
│   ├── drawable/                       # Vector icons & selectors
│   ├── values/                         # Colors, strings, dimens, themes
│   └── mipmap-*/                       # App launcher icons
└── AndroidManifest.xml
```

## WebApp Example

The `web_app/` folder contains a ready-to-deploy Telegram Mini App (WebApp) example:

- Integrates with `telegram-web-app.js`
- Reads user data from `Telegram.WebApp.initDataUnsafe`
- Supports theme synchronization (`--tg-theme-*` CSS variables)
- Sends structured JSON data back to the bot via `sendData()`

## Build & Run

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run lint checks
./gradlew lintDebug

# Run unit tests
./gradlew testDebugUnitTest
```

## CI/CD Pipeline

Push to `main` branch or create a tag (`v*`) to trigger the GitHub Actions workflow:

1. **On every push/PR**: Lint > Test > Build Debug APK > Upload as artifact
2. **On version tag** (e.g., `v1.0.0`): Build Release APK > Create GitHub Release

## License

This project is provided as-is for educational and personal use.
