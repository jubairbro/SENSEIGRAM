# AGENTS.md - SenseiGram Development Guide

## Project Overview

SenseiGram is an Android native Telegram Bot Manager application. It's a serverless tool for managing Telegram bots, sending messages and files to channels, groups, or private chats.

**Tech Stack:**
- Language: Kotlin 1.9.22
- Min SDK: 24, Target SDK: 34
- Architecture: MVVM with ViewBinding
- Key Libraries: Retrofit, OkHttp, Glide, DataStore, Navigation Component, Coroutines

---

## Build Commands

### Build Project
```bash
./gradlew build                    # Build entire project
./gradlew assembleDebug            # Build debug APK
./gradlew assembleRelease          # Build release APK
```

### Clean Build
```bash
./gradlew clean build              # Clean and rebuild
./gradlew clean assembleDebug      # Clean and build debug
```

### Lint
```bash
./gradlew lint                     # Run lint on all variants
./gradlew lintDebug                # Run lint on debug variant
./gradlew lintRelease              # Run lint on release variant
```

### Testing
```bash
./gradlew test                     # Run all unit tests
./gradlew testDebugUnitTest        # Run debug unit tests
./gradlew testReleaseUnitTest      # Run release unit tests

# Run a single test class
./gradlew test --tests "com.senseigram.utils.PreferenceManagerTest"

# Run a single test method
./gradlew test --tests "com.senseigram.utils.PreferenceManagerTest.testSaveToken"

# Run tests with coverage
./gradlew testDebugUnitTest coverageDebugUnitTestReport
```

### Android Instrumentation Tests
```bash
./gradlew connectedAndroidTest              # Run all instrumentation tests
./gradlew connectedDebugAndroidTest         # Run debug instrumentation tests
```

### Install on Device
```bash
./gradlew installDebug              # Install debug APK on connected device
./gradlew installRelease            # Install release APK
```

---

## Code Style Guidelines

### Imports
- Use explicit imports; avoid wildcard imports (`import package.*`)
- Sort imports alphabetically with grouping:
  1. Android imports (`android.*`, `androidx.*`)
  2. Kotlin imports (`kotlin.*`, `kotlinx.*`)
  3. Third-party imports (`com.squareup.*`, etc.)
  4. Project imports (`com.senseigram.*`)

### Formatting
- Use 4 spaces for indentation (no tabs)
- Max line length: 120 characters
- Use blank lines between logical sections
- Opening brace on same line as declaration

### Naming Conventions

**Classes/Interfaces:** PascalCase
```kotlin
class BotManagerActivity : AppCompatActivity()
interface MessageCallback
```

**Functions/Methods:** camelCase, descriptive names
```kotlin
fun sendMessageToChat(chatId: String, message: String)
private fun validateBotToken(token: String): Boolean
```

**Variables/Properties:** camelCase
- Use meaningful names: `botToken` not `bt`
- Boolean properties use `is/has/can` prefix: `isConnected`, `hasPermission`

**Constants:** UPPER_SNAKE_CASE in companion object or object
```kotlin
companion object {
    const val MAX_RETRY_COUNT = 3
    const val DEFAULT_TIMEOUT_MS = 30000L
}
```

**XML Resources:** snake_case
- Layouts: `activity_main.xml`, `fragment_compose.xml`, `item_saved_chat.xml`
- IDs: `@+id/sendButton`, `@+id/messageInputField`
- Strings: `R.string.send_message`, `R.string.error_connection_failed`

### Types
- Prefer `val` over `var` for immutability
- Use nullable types explicitly: `String?` instead of `String!`
- Use `lateinit` for non-null properties initialized after construction
- Use `by lazy` for expensive initializations
- Prefer Kotlin collections: `List`, `Set`, `Map` over Java counterparts

### Coroutines
- Use `viewModelScope` for ViewModel operations
- Use `lifecycleScope` for Activity/Fragment operations
- Always handle exceptions in coroutines:
```kotlin
viewModelScope.launch {
    try {
        repository.sendMessage(message)
    } catch (e: Exception) {
        _errorState.value = e.message
    }
}
```

### Error Handling
- Use `Result<T>` or sealed classes for operation outcomes
- Never catch generic `Exception` silently - always log or propagate
- Use custom exception classes for domain errors
- Show user-friendly error messages via strings resources

### Resource Handling
- All strings in `strings.xml`, never hardcoded in code
- Dimensions in `dimens.xml`
- Colors in `colors.xml`
- Use theme attributes for colors: `?attr/colorPrimary`

### Architecture Patterns
- MVVM with ViewModel and LiveData/StateFlow
- Repository pattern for data access
- Dependency injection via constructor injection
- Single responsibility for classes
- Use ViewBinding for all activities/fragments

### File Organization
```
app/src/main/java/com/senseigram/
├── data/
│   ├── model/           # Data classes
│   ├── repository/      # Data repositories
│   ├── local/           # Local storage (DataStore, etc.)
│   └── remote/          # API services, Retrofit interfaces
├── ui/
│   ├── activities/      # Activities
│   ├── fragments/       # Fragments
│   ├── adapters/        # RecyclerView adapters
│   ├── viewmodels/      # ViewModels
│   ├── theme/           # Theme, styles, colors
│   └── dialogs/         # Custom dialogs
├── utils/               # Utility classes
├── services/            # Android services
└── network/             # Network utilities
```

---

## Pre-commit Checklist

Before committing changes:
1. Run `./gradlew lint` and fix all warnings
2. Run `./gradlew test` and ensure all tests pass
3. Verify build with `./gradlew assembleDebug`
4. Check for hardcoded strings - use resources
5. Verify proguard rules if adding new libraries
