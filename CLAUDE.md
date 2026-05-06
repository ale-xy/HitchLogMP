# HitchlogMP — Developer Guide

## Project Overview

**Kotlin Multiplatform** mobile/web app for logging hitchhiking race activity during Guild competitions. Records lifts, walks, checkpoints, rest periods, and race events in real time. Syncs via Firebase Firestore.

**Platforms:** Android (API 24+), iOS (Arm64, X64, Simulator), Web (JS)

**Tech Stack:** Kotlin 2.0.21 | Compose Multiplatform 1.6.11 | Firebase (GitLive SDK 2.4.0) | Koin 4.0.0 | Navigation 2.8.0-alpha10

---

## Architecture

### Repository Structure
```
composeApp/src/
  commonMain/kotlin/org/gmautostop/hitchlogmp/
    domain/     # Models, Repository, Response, User
    data/       # FirestoreRepository, AuthService, DTOs
    di/         # Koin modules
    ui/         # Screens, ViewModels, Navigation
  androidMain/  # Android entry point
  iosMain/      # iOS entry point
  jsMain/       # Web entry point
```

### Domain Model
- **HitchLog:** id, userId, raceId, teamId, name, creationTime
- **HitchLogRecord:** id, time (LocalDateTime), type, text
- **Record types:** START, LIFT, GET_OFF, WALK, WALK_END, CHECKPOINT, MEET, REST_ON, REST_OFF, OFFSIDE_ON, OFFSIDE_OFF, FINISH, RETIRE, FREE_TEXT

### Firestore Structure
```
logs/{logId} → HitchLog fields
  └── records/{recordId} → FirestoreHitchLogRecord
```
- Logs: query by `userId`, order by `creationTime DESC`
- Records: order by `timestamp ASC`
- `getNextTime()` handles timestamp collisions (adds 1 second if same minute)
- **Known issue:** App stores local timezone, but competition rules require Moscow time (UTC+3)

### Navigation
`Screen` sealed interface: Auth, LogList, EditLog, Log, EditRecord

### ViewModels
All use `ViewState<T>`: Loading, Show(value), Error(error)
- `AuthViewModel`, `LogListViewModel`, `EditLogViewModel`, `HitchLogViewModel`, `RecordViewModel`
- Date format: `dd.MM.yyyy`, Time format: `HH:mm`

---

## Development Guidelines

**Code Style:** Kotlin official, JVM 17, shared code in `commonMain`

**Strings:** Use `stringResource(Res.string.*)` — never hardcode. Include `contentDescription` for all icons/images.

**Firebase:** All queries must filter by `userId`

**Explicit Backing Fields (Kotlin 2.3+):**

Use the `field` keyword to create a backing field with a different type than the public property. This is perfect for exposing read-only types backed by mutable implementations.

**✅ CORRECT Usage:**
```kotlin
// StateFlow backed by MutableStateFlow
val state: StateFlow<T>
    field = MutableStateFlow(value)

// SharedFlow backed by MutableSharedFlow
val events: SharedFlow<T>
    field = MutableSharedFlow()

// List backed by MutableList
val items: List<String>
    field = mutableListOf()
```

**❌ WRONG - Don't add type annotation to field:**
```kotlin
// ❌ WRONG - field should not have type annotation
val state: StateFlow<T>
    field: MutableStateFlow<T> = MutableStateFlow(value)
```

**❌ WRONG - Don't use with transformations:**
```kotlin
// ❌ WRONG - use traditional backing property instead
val events: Flow<T>
    field = Channel<T>().receiveAsFlow()  // Transformation applied!

// ✅ CORRECT - use traditional backing property
private val _events = Channel<T>()
val events: Flow<T> = _events.receiveAsFlow()
```

**When to use explicit backing fields:**
- ✅ Simple type narrowing (MutableStateFlow → StateFlow)
- ✅ Direct assignment without transformation
- ✅ Same underlying object, different interface

**When NOT to use:**
- ❌ Any transformation (`.receiveAsFlow()`, `.asSharedFlow()`, `.asStateFlow()`)
- ❌ Computed properties
- ❌ Delegated properties

**Git:** Auto-stage code files only. Never commit unless explicitly requested.

---

## CI/CD & Deployment

### Web Deployment (Automatic)
- **Dev:** Push to `develop` → https://hitchlog-dev.web.app
- **Prod:** Push to `master` → https://hitchlog.web.app
- Manual: `./deploy-dev.sh`, `./deploy-prod.sh`, `./deploy-both.sh`

### Android Release (Tag-triggered)
1. Push tag: `git tag v0.2.0 && git push origin v0.2.0`
2. GitHub Actions builds signed APKs with ProGuard
3. Creates release at: https://github.com/ale-xy/HitchLogMP/releases

**Version format:** `v{MAJOR}.{MINOR}.{PATCH}` → versionCode = `MAJOR * 10000 + MINOR * 100 + PATCH`
- `v0.1.0` → `100`, `v1.2.3` → `10203`, `v2.0.15` → `20015`

**APK variants:** arm64-v8a (modern), armeabi-v7a (older), x86_64 (emulators)

**Local build:**
```bash
export VERSION_NAME="0.2.0" VERSION_CODE="200"
./gradlew :composeApp:assembleRelease
```

### GitHub Secrets Required

**Web deployment:**
- `FIREBASE_SERVICE_ACCOUNT_HITCHLOGMP` - Service account JSON
- `FIREBASE_WEB_API_KEY` - Browser key (HTTP referrer restrictions)
- `FIREBASE_GCM_SENDER_ID` = `869765129540`

**Android release:**
- `ANDROID_KEYSTORE_BASE64` - Base64-encoded `hitchlog-release.jks`
- `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`, `ANDROID_KEY_PASSWORD`
- `FIREBASE_API_KEY` - Android key (SHA fingerprint restrictions)
- `FIREBASE_GCM_SENDER_ID` = `869765129540`
- `FIREBASE_APPLICATION_ID` - Android app ID

**Firebase config architecture:**
- Public values (authDomain, projectId, storageBucket) in `FirebasePublicConfig.kt` (committed)
- Secret values (apiKey, gcmSenderId, applicationId) in `local.properties` (local) or GitHub Secrets (CI)
- Platform-specific: Web uses `FIREBASE_WEB_API_KEY`, Android uses `FIREBASE_API_KEY`

**Security:**
- Never commit `hitchlog-release.jks` or `local.properties`
- Backup keystore securely - required for all future app updates
- Release keystore SHA-1: `BC:7D:B1:1F:A8:C7:98:C1:B2:27:7B:87:62:A0:44:0B:88:5E:8D:94`
- Must be registered in Firebase Console for Android app authentication

---

## Skills

Use the appropriate skill when working on specific aspects of the codebase:

**Architecture & Structure:**
- `android-module-structure` — module layout, dependency rules, Gradle conventions
- `kotlin-project-modularization` — module boundaries, visibility control
- `kotlin-project-architecture-review` — architecture review, layer boundaries

**Implementation:**
- `kotlin-project-feature-implementation` — implementing/extending features
- `android-presentation-mvi` — ViewModels, State/Action/Event, UI models
- `android-compose-ui` — Composables, recomposition, animations, previews
- `android-navigation` — type-safe navigation, nav graphs
- `android-data-layer` — repositories, data sources, DTOs, mappers
- `android-di-koin` — Koin DI setup and module definitions
- `android-error-handling` — Result wrapper, error types, error flows
- `kotlin-explicit-backing-fields` — Kotlin 2.3+ explicit backing field syntax for read-only/mutable type pairs

**Quality & Maintenance:**
- `kotlin-project-code-review` — code review for architecture, correctness
- `kotlin-project-bugfix` — diagnosing and fixing bugs
- `android-testing` — ViewModel tests, Compose UI tests, test doubles
- `kotlin-kmp-refactor-safety` — safe refactoring discipline

**Platform & Integration:**
- `kotlin-platform-kmp-bridges` — expect/actual, platform-specific code
- `kotlin-ui-compose-multiplatform` — shared UI in Compose Multiplatform
- `kotlin-data-kmp-data-layer` — KMP data layer patterns
