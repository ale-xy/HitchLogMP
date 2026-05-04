# HitchlogMP — Developer Guide for Claude

## Project Overview

HitchlogMP is a **Kotlin Multiplatform** (KMP) mobile/web app for logging hitchhiking race activity during Guild competitions. Participants record lifts, walks, checkpoints, rest periods, and other race events in real time. The app syncs via Firebase Firestore.

**Platforms:** Android (API 24+), iOS (Arm64, X64, Simulator), Web/WASM (currently disabled in build)

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin 2.0.21 with Multiplatform |
| UI | Compose Multiplatform 1.6.11 + Material3 |
| Backend | Firebase Firestore + Firebase Auth (GitLive KMP SDK 2.1.0) |
| Auth | KMPAuth 2.2.0 (Firebase, Google Sign-In) |
| DI | Koin 4.0.0 |
| Navigation | Compose Multiplatform Navigation 2.8.0-alpha10 |
| Serialization | Kotlinx Serialization 1.7.3 |
| DateTime | Kotlinx DateTime |
| Build | Gradle with version catalog (`gradle/libs.versions.toml`) |

---

## Repository Structure

```
composeApp/src/
  commonMain/kotlin/org/gmautostop/hitchlogmp/
    domain/           # Models, Repository interface, Response, User
    data/             # FirestoreRepository, AuthService, FirestoreHitchLogRecord DTO
    di/               # Koin AppModule + initialization
    ui/               # Screens, ViewModels, Nav routes
    app/              # Platform-independent init (Google auth)
    Platform.kt       # expect interface
    Utils.kt          # DateTime helpers (LocalDateTime ↔ Timestamp conversion)
  androidMain/        # MainActivity, MainApplication, Platform.android.kt
  iosMain/            # MainViewController.kt, Platform.ios.kt
  wasmJsMain/         # main.kt, Platform.wasmJs.kt (disabled)
```

---

## Domain Model

**HitchLog** (aggregate root): id, userId, raceId, teamId, name, creationTime  
**HitchLogRecord** (timeline event): id, time (LocalDateTime), type (HitchLogRecordType), text

**Record types:** START, LIFT, GET_OFF, WALK, WALK_END, CHECKPOINT, MEET, REST_ON, REST_OFF, OFFSIDE_ON, OFFSIDE_OFF, FINISH, RETIRE, FREE_TEXT

---

## Data Layer

### Firestore Structure
```
Collection: logs
└── Document: {logId}          (HitchLog fields)
    └── Subcollection: records
        └── Document: {recordId}   (FirestoreHitchLogRecord fields)
```

**Key details:**
- Logs queried by `userId`, ordered by `creationTime DESC`
- Records ordered by `timestamp ASC`
- `FirestoreHitchLogRecord` is a DTO that maps `LocalDateTime ↔ Firestore Timestamp` with nanosecond precision
- `getNextTime()` handles timestamp collisions: if a record already exists within the same minute, it adds 1 second to ensure ordering
- DateTime helpers in `Utils.kt`: `Instant.localTZDateTime()`, `Timestamp.toLocalDateTime()`, `LocalDateTime.toTimestamp()`
- **Timezone discrepancy:** Chronicle (хроника) must be Moscow time (UTC+3) per competition rules, but app stores local device timezone

### Repository & Response
`Repository` interface in `domain/` — standard CRUD for HitchLog + HitchLogRecord, returns `Flow<Response<T>>`  
`Response<T>`: Loading, Success(data), Failure(errorMessage)

---

## UI Layer

### Navigation
`Screen` sealed interface in `ui/Nav.kt`:
- `Auth` — Login
- `LogList` — List all user's logs
- `EditLog(logId: String = "")` — Create/edit log (empty id = new)
- `Log(logId: String)` — View log + all records
- `EditRecord(logId, recordId = "", recordType = FREE_TEXT)` — Create/edit record

### ViewModels
All use `ViewState<T>`: Loading, Show(value), Error(error)

| ViewModel | State | Key Methods |
|---|---|---|
| `AuthViewModel` | `currentUser: StateFlow<User?>` | `onAnonymousLogin()`, `onLogin(user)`, `onSignOut()` |
| `LogListViewModel` | `ViewState<List<HitchLog>>` | passive — observes `getLogs()` |
| `EditLogViewModel` | `ViewState<HitchLog>` | `updateName()`, `saveLog()`, `deleteLog()` |
| `HitchLogViewModel` | `ViewState<HitchLogState>` | passive — combines log + records |
| `RecordViewModel` | `ViewState<HitchLogRecord>` | `updateDate()`, `updateTime()`, `updateText()`, `save()`, `delete()` |

`RecordViewModel` uses date format `dd.MM.yyyy` and time format `HH:mm` when parsing user input.

### Dependency Injection
Koin setup in `di/AppModule.kt`:
- `AuthService` (single), `FirestoreRepository` (single→Repository)
- All ViewModels via `viewModelOf` or `viewModel { }`
- `RecordViewModel` takes params: logId, recordId, recordType

---

## Known Issues / TODOs

| Location | Issue |
|---|---|
| `AuthViewModel.kt:52` | Anonymous login error logged but not shown to user |
| `LogListScreen.kt:113` | Edit button functionality unclear — needs review |
| `wasmJsMain` | WASM target disabled in `build.gradle.kts` |

---

## Competition Domain

App built for **Guild hitchhiking races** (Гильдия спортивного автостопа). Full rules in `Правила соревнований Гильдии.md`.

**Chronicle (хроника)** = official race log. App IS the digital chronicle. Mandatory entries: START, LIFT (car brand required), GET_OFF (location), CHECKPOINT, REST_ON/OFF timestamps, OFFSIDE_ON/OFF timestamps, FINISH/RETIRE. Must be **Moscow time (UTC+3)**, accurate to 1 minute. **Known issue:** app stores local device timezone.

**Race modes:**
- **REST** (REST_ON/OFF) — rest budget `k/m` hours. Subtracts from race time. Must start/end at same point (≤24m radius).
- **OFFSIDE** (вне игры, OFFSIDE_ON/OFF) — clock runs, no route advancement. Can be set retroactively.

**Race time:** `finish_time − calculated_start_time − rest_used + penalties`

---

## Development Guidelines

**Code style:** Kotlin official (see `gradle.properties`), JVM 17, shared code in `commonMain`

**Add record type:** 1) Add to `HitchLogRecordType` enum in `domain/Data.kt`, 2) Add string resource in `composeResources/`. Type serializes as name string.

**Add screen:** 1) Add to `Screen` sealed interface in `ui/Nav.kt`, 2) Add composable + route in `HitchLogApp.kt`, 3) Create ViewModel in `ui/viewmodel/`, register in `di/AppModule.kt`

**Strings:** Never use hardcoded strings in composables or any user-visible code. All text must come from string resources via `stringResource(Res.string.*)` in composables. This includes `contentDescription` for all icons and images (decorative-only elements may use `contentDescription = null`).

**Firebase:** All queries filter by `userId`. New queries must include `userId` filter.

**Timestamps:** `getNextTime()` uses `Source.CACHE` for collision detection — offline records with same minute may order incorrectly until synced.

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

**Quality & Maintenance:**
- `kotlin-project-code-review` — code review for architecture, correctness
- `kotlin-project-bugfix` — diagnosing and fixing bugs
- `android-testing` — ViewModel tests, Compose UI tests, test doubles
- `kotlin-kmp-refactor-safety` — safe refactoring discipline

**Platform & Integration:**
- `kotlin-platform-kmp-bridges` — expect/actual, platform-specific code
- `kotlin-ui-compose-multiplatform` — shared UI in Compose Multiplatform
- `kotlin-data-kmp-data-layer` — KMP data layer patterns
