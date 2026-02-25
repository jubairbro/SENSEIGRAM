<div align="center">
  <img src="https://img.icons8.com/color/120/telegram-app.png" alt="SenseiGram Logo"/>
  <h1>SenseiGram</h1>
  <p><b>Advanced Native Android Telegram Bot Manager & Client System</b></p>
  <p>The ultimate serverless tool for managing Telegram bots, sending advanced messages, and handling channels/groups straight from your phone.</p>
</div>

[![Build Status](https://img.shields.io/github/actions/workflow/status/jubairbro/SENSEIGRAM/android.yml?style=flat-square)](https://github.com/jubairbro/SENSEIGRAM/actions)
[![Kotlin Version](https://img.shields.io/badge/Kotlin-1.9.22-blue.svg?style=flat-square&logo=kotlin)](https://kotlinlang.org)
[![Platform](https://img.shields.io/badge/Platform-Android-green.svg?style=flat-square&logo=android)](https://www.android.com/)

---

## 🌟 Overview

SenseiGram goes beyond a simple bot sender. It is a comprehensive Telegram Bot Client that allows you to manage your bot identity, fetch interacting users, construct rich messages with inline button colors, and manage multiple target chats without writing a single line of code.

### 🔥 50+ Key Features

#### 🤖 Bot Management & Identity
1. **Connect via API Token** from @BotFather.
2. **Edit Bot Name** directly from the app.
3. **Edit Bot Description** (What can this bot do?).
4. **Edit Short Description** (Profile bio).
5. **Upload Profile Photo** from device.
6. **Remove Profile Photo** instantly.
7. **Connection Status Check** with one-tap validation.
8. **Token Visibility Toggle** for privacy.
9. **Automatic Bot @username detection.**
10. **Prepared API Config** (`api_id: 26158708`, `api_hash: 5f4602d47f32aabce2cbe0ab1244171f`) for future TDLib client integration.

#### 💬 Messaging & Broadcasting
11. **Send Text Messages** to anyone using their Chat ID.
12. **Send Photos** via direct upload.
13. **Send Photos** via URL.
14. **Send Videos** via direct upload.
15. **Send Videos** via URL.
16. **Send Documents/Files** via direct upload.
17. **Send Documents/Files** via URL.
18. **Fetch Recent Users:** Pull a list of all ordinary users who recently started the bot.
19. **Send to Ordinary Users** that interacted with the bot.
20. **Silent Messages** (No push notifications).
21. **Protect Content** (Disable forwarding and saving).
22. **Hide Link Previews** for cleaner messages.
23. **Media Spoilers** (Hide photos/videos with animation).

#### 🎛 Advanced Inline Buttons Builder
24. **Visual Inline Keyboard Builder** (Popup/Dialog based).
25. **Add Multiple Rows** of buttons.
26. **Add Multiple Buttons per Row.**
27. **URL Buttons** (Redirect to links).
28. **Callback Data Buttons** (Trigger background actions).
29. **Colored Inline Buttons:** Support for new Telegram features (Primary/Blue, Success/Green, Danger/Red).
30. **Dynamic Button Preview** while building.
31. **Edit Existing Button Configurations** before sending.

#### ✍️ Rich Text Formatting
32. **HTML Formatting** Engine.
33. **Bold Text** tool.
34. **Italic Text** tool.
35. **Monospace Code** tool.
36. **Hyperlink Insertion** tool (with custom dialog).
37. **Text Spoiler** tool.

#### ✏️ Editing & Drafting
38. **Edit Sent Text Messages** using message link.
39. **Edit Sent Media Captions.**
40. **Edit Sent Inline Keyboards** without changing text.
41. **Link Parser:** Auto-extract Chat ID and Message ID from Telegram post links.
42. **Save Message Drafts** locally.
43. **Resume Drafts** anytime.
44. **Delete Obsolete Drafts.**

#### 🎯 Target Management
45. **Chat Lookup Engine:** Search by @username or ID.
46. **View Chat Info:** See Title, Username, Type, and Member Count.
47. **Save Targets:** Keep a list of favorite Channels/Groups/Users.
48. **Quick Target Selection** via dropdown in Composer.
49. **Remove Targets** from saved list.

#### 🎨 Customization & UI/UX
50. **True Dark Mode** support.
51. **True AMOLED Mode** for deep blacks and battery saving.
52. **Light Mode** support.
53. **System Theme Matching.**
54. **Dynamic Color Application:** Fixes color misalignments across layouts.
55. **5 Accent Colors:** Emerald, Blue, Violet, Rose, Amber.
56. **Smooth Scrolling** toggle.
57. **Haptic Feedback** toggle.
58. **Material Design 3 Components.**

---

## 🛠 Tech Stack

| Component | Technology |
|-----------|-----------|
| **Language** | Kotlin 1.9.22 |
| **Min SDK** | 24 (Android 7.0 Nougat) |
| **Target SDK** | 34 (Android 14) |
| **Architecture** | MVVM + ViewBinding |
| **Networking** | Retrofit + OkHttp |
| **Storage** | SharedPreferences / JSON |
| **UI Components** | Material Components for Android |

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Iguana | 2023.2.1 or newer.
- A Telegram Bot Token from [@BotFather](https://t.me/botfather).

### Build Instructions

1. Clone the repository:
```bash
git clone https://github.com/jubairbro/SENSEIGRAM.git
```
2. Open the project in Android Studio.
3. Sync Gradle and build the project:
```bash
./gradlew clean build
```
4. Build the APKs:
```bash
./gradlew assembleDebug
./gradlew assembleRelease
```

---

## 🔐 API Configuration (For Developers)

The app operates via the **Telegram Bot API**. However, for advanced custom client features, the following Telegram App API credentials are incorporated for future TDLib expansion:

```ini
[telegram]
api_id = 26158708
api_hash = 5f4602d47f32aabce2cbe0ab1244171f
```
*(These credentials are used strictly for TDLib/MTProto implementation to convert the app into a full-fledged client. The current bot features require the Bot Token.)*

---

## 📄 License

This project is open-source and available under the MIT License.

<p align="center">
  Made with 🖤 by JubairSensei & The Community
</p>