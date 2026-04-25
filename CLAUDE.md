# HitchlogMP — Developer Guide for Claude

## Project Overview

HitchlogMP is a **Kotlin Multiplatform** (KMP) mobile/web app for logging hitchhiking race activity during [Guild competitions](#competition-domain). Participants record lifts, walks, checkpoints, rest periods, and other race events in real time. The app syncs via Firebase Firestore.

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
| Logging | Napier + lighthouse logging |
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
    Utils.kt          # DateTime helpers
  androidMain/        # MainActivity, MainApplication, Platform.android.kt
  iosMain/            # MainViewController.kt, Platform.ios.kt
  wasmJsMain/         # main.kt, Platform.wasmJs.kt (disabled)
```

---

## Domain Model

### `HitchLog` — race log (aggregate root)
```kotlin
data class HitchLog(
    var id: String,         // UUID, assigned on create
    val userId: String,     // Firebase Auth UID
    val raceId: String,     // Associated race/competition
    val teamId: String,     // Team ID (empty for solo)
    val name: String,       // User-chosen display name
    val creationTime: Timestamp
)
```

### `HitchLogRecord` — single timeline event
```kotlin
data class HitchLogRecord(
    val id: String,                       // UUID
    val time: LocalDateTime,              // Local system timezone
    val type: HitchLogRecordType,
    val text: String                      // Free-form note
)
```

### `HitchLogRecordType` — enum of race events
| Value | Meaning |
|---|---|
| `START` | Race start |
| `LIFT` | Got a ride (hitchhike) |
| `GET_OFF` | Exited a vehicle |
| `WALK` | Began walking |
| `WALK_END` | Finished walking |
| `CHECKPOINT` | Passed a control point (КП) |
| `MEET` | Met another competitor |
| `REST_ON` | Entered rest mode |
| `REST_OFF` | Exited rest mode |
| `OFFSIDE_ON` | Entered "вне игры" (offside) mode |
| `OFFSIDE_OFF` | Exited "вне игры" mode |
| `FINISH` | Race finish |
| `RETIRE` | Withdrew (сход с трассы) |
| `FREE_TEXT` | Free-form note |

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

### `Repository` Interface
```kotlin
interface Repository {
    fun userId(): String?
    fun getLogs(): Flow<Response<List<HitchLog>>>
    fun getLog(logId: String): Flow<Response<HitchLog>>
    fun addLog(log: HitchLog): Flow<Response<Unit>>
    fun updateLog(log: HitchLog): Flow<Response<Unit>>
    fun deleteLog(id: String): Flow<Response<Unit>>
    fun getLogRecords(logId: String): Flow<Response<List<HitchLogRecord>>>
    fun getRecord(logId: String, recordId: String): Flow<Response<HitchLogRecord>>
    fun addRecord(logId: String, record: HitchLogRecord): Flow<Response<Unit>>
    fun updateRecord(logId: String, record: HitchLogRecord): Flow<Response<Unit>>
    fun deleteRecord(logId: String, record: HitchLogRecord): Flow<Response<Unit>>
    fun saveRecord(logId: String, record: HitchLogRecord): Flow<Response<Unit>>
}
```

### `Response<T>` — sealed data transport
- `Loading<T>()` — operation in progress
- `Success<T>(data: T)` — success
- `Failure<T>(errorMessage: String)` — error

---

## UI Layer

### Navigation (`Screen` sealed interface)
```kotlin
Screen.Auth                             // Login
Screen.LogList                          // List all user's logs
Screen.EditLog(logId: String = "")      // Create/edit a log (empty id = new)
Screen.Log(logId: String)               // View log + all records
Screen.EditRecord(logId, recordId = "", recordType = FREE_TEXT)  // Create/edit record
```

### ViewState — UI state pattern
- `ViewState.Loading` — show spinner
- `ViewState.Show<T>(value: T)` — display data
- `ViewState.Error(error: String)` — show error

### ViewModels
| ViewModel | State | Key Methods |
|---|---|---|
| `AuthViewModel` | `currentUser: StateFlow<User?>` | `onAnonymousLogin()`, `onLogin(user)`, `onSignOut()` |
| `LogListViewModel` | `state: StateFlow<ViewState<List<HitchLog>>>` | passive — observes `getLogs()` |
| `EditLogViewModel` | `state: MutableStateFlow<ViewState<HitchLog>>` | `updateName()`, `saveLog()`, `deleteLog()` |
| `HitchLogViewModel` | `state: StateFlow<ViewState<HitchLogState>>` | passive — combines log + records |
| `RecordViewModel` | `state: StateFlow<ViewState<HitchLogRecord>>` | `updateDate()`, `updateTime()`, `updateText()`, `save()`, `delete()` |

`RecordViewModel` uses date format `dd.MM.yyyy` and time format `HH:mm` when parsing user input.

---

## Dependency Injection (Koin)

```kotlin
single { AuthService(Firebase.auth) }
singleOf(::FirestoreRepository).bind<Repository>()
viewModelOf(::AuthViewModel)
viewModelOf(::LogListViewModel)
viewModelOf(::EditLogViewModel)
viewModel { HitchLogViewModel(get(), get()) }          // logId param + repository
viewModel { params -> RecordViewModel(get(), params[0], params[1], params[2]) }
// params: logId, recordId, recordType
```

---

## Platform Entry Points

| Platform | File | Notes |
|---|---|---|
| Android | `MainActivity.kt`, `MainApplication.kt` | Koin initialized with `androidContext()` |
| iOS | `MainViewController.kt` | `ComposeUIViewController` with Koin init |
| Web | `wasmJsMain/main.kt` | `ComposeViewport` — currently disabled in build |

---

## DateTime Utilities (`Utils.kt`)

- `Instant.localTZDateTime()` — converts to `LocalDateTime` in device timezone
- `Timestamp.toLocalDateTime()` — Firebase Timestamp → LocalDateTime
- `LocalDateTime.toTimestamp()` — LocalDateTime → Firebase Timestamp

Chronicle (хроника) times must be in **Moscow time** (per competition rules), but the app stores local device timezone. This discrepancy should be considered in any timezone-aware features.

---

## Known Issues / TODOs

| Location | Issue |
|---|---|
| `AuthScreen.kt:84` | Google sign-in error not shown to user |
| `AuthViewModel.kt:39` | Anonymous login error not handled |
| `RecordViewModel.kt:79` | Date parse failure is silently swallowed |
| `RecordViewModel.kt:90` | Record save error not propagated to UI |
| `LogListScreen.kt:95` | Edit button placeholder — not fully implemented |
| `wasmJsMain` | WASM target disabled in `build.gradle.kts` |

---

## Competition Domain

The app is built for **Guild hitchhiking race** (Гильдия спортивного автостопа) competitions. Rules are in `Правила соревнований Гильдии.md`. Key concepts that inform the data model:

### Race Flow
1. **Старт (START)** — each participant chooses their starting position; arbiter assigns a calculated start time
2. **Движение** — alternating between lifts (`LIFT`/`GET_OFF`) and walking (`WALK`/`WALK_END`)
3. **КП (CHECKPOINT)** — mandatory control points; must be visited in order; participant affixes a control mark
4. **Финиш (FINISH)** — arrival at finish point

### Key Modes
- **Rest (REST_ON/REST_OFF)** — structured rest budget defined as `k/m` (k hours, dividable into at most m parts). Rest time is subtracted from race time. Must be taken/cancelled at the same point (≤24m radius). Rest position priority is not preserved.
- **Вне игры / Offside (OFFSIDE_ON/OFFSIDE_OFF)** — "out of game" mode; race clock keeps running; participant cannot advance on the route; can be set/cancelled retroactively. Position priority is not preserved.

### Race Time Calculation
```
race_time = finish_time − calculated_start_time − rest_used + penalties
```
For pairs: multiply by coefficient (male pair × 0.9, mixed × 1.0, female pair × 1.05).

### Chronicle (Хроника)
The app IS the digital хроника. Required entries per rules:
- Start fact and time
- Every `LIFT` (car brand mandatory per rules, currently just text)
- Every `GET_OFF` with location
- Significant walking (`WALK`)
- `CHECKPOINT` with time + race info from the КП
- `REST_ON`/`REST_OFF` timestamps
- `OFFSIDE_ON`/`OFFSIDE_OFF` timestamps
- `RETIRE` with time and location
- `FINISH` fact and time

Chronicle must be in **Moscow time (UTC+3)**, accurate to 1 minute.

### Penalties (relevant to app data integrity)
- Missing chronicle entry → disqualification
- Rest cancelled outside the 24m radius → disqualification  
- "Вне игры" mode violation → disqualification
- Early rest cancellation → 5× time penalty

### Teams (Командные соревнования)
Format: `n+k` (n leaders + k followers). Team logs reference `teamId`. Followers (`ведомые`) have infinite rest budget and limited chronicle requirements.

---

## Development Guidelines

### Code style
- Follow Kotlin official code style (set in `gradle.properties`)
- JVM target: 17
- All shared code goes in `commonMain`; platform-specific only when necessary

### Adding a new record type
1. Add enum value to `HitchLogRecordType` in `domain/Data.kt`
2. Add string resource in `composeResources/`
3. No other changes needed — the type is serialized as its name string in Firestore

### Adding a new screen
1. Add a `data class` or `data object` to `Screen` in `ui/Nav.kt`
2. Add composable + `composable<Screen.NewScreen>` entry in `HitchLogApp.kt`
3. Create ViewModel in `ui/viewmodel/`, register in `di/AppModule.kt`

### Firebase rules
Firestore queries always filter by `userId` — no cross-user data access at the app level. Ensure any new queries include a `userId` filter.

### Timestamp handling
Records use `LocalDateTime` internally. The `getNextTime()` collision resolution in `FirestoreRepository` uses `Source.CACHE` — this means offline-created records with the same minute may have incorrect ordering until synced.
