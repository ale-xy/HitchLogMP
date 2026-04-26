# Architecture Review: HitchlogMP

_Date: 2026-04-24_

---

## Verdict: Acceptable with Revisions

The overall shape is reasonable — clear layering, ViewModels, Repository abstraction, Koin DI, proper KMP source set usage. But there are several compounding issues across state management, domain purity, and data layer consistency that will create real maintenance cost as the app grows.

---

## Architecture Summary

Single-module KMP app (`composeApp`), one Gradle module. All features live in `commonMain`. Layers: `domain` → `data` → `di` → `ui/viewmodel` → `ui` (screens). Firebase Firestore + Auth as the backend, KMPAuth for Google sign-in. Five screens, five ViewModels, one `Repository` interface with `FirestoreRepository` as the sole implementation.

---

## What Is Structurally Sound

- ViewModels exist for all screens and hold state as `StateFlow` — the right pattern for KMP Compose.
- `Repository` interface in `domain` correctly hides Firestore implementation details from the rest of the app.
- `FirestoreHitchLogRecord` DTO is properly isolated to `data/` and not leaked upward.
- `AuthService` correctly uses `authStateChanged` as a hot `StateFlow`, so all consumers stay in sync.
- Navigation uses type-safe `@Serializable` routes — correct for Compose Navigation 2.8+.
- Koin DI properly scopes `AuthService` and `FirestoreRepository` as singletons with ViewModel bindings.
- `commonMain`-only source set usage is correct — no platform APIs leak into shared code (with one exception below).

---

## Issues

### 1. Domain model depends on Compose UI framework — HIGH

`HitchLogRecordType` holds a `StringResource` property (`val text: StringResource`) directly on the enum. `StringResource` is from `org.jetbrains.compose.resources` — a UI presentation framework type. This ties the domain model permanently to Compose, makes it untestable without Compose resources, and contradicts the purpose of the domain layer.

**File:** `domain/Data.kt:43`

**Recommendation:** Remove `StringResource` from the enum. Map enum → display string at the UI edge (e.g., a top-level function or a companion object in the UI layer). The enum values are already named well enough to drive that mapping.

---

### 2. `RecordViewModel` has three parallel state sources — HIGH

`RecordViewModel` exposes:
- `_state: MutableStateFlow<ViewState<HitchLogRecord>>` — the canonical ViewState
- `record: mutableStateOf(HitchLogRecord())` — a separate Compose state for the full record
- `date: mutableStateOf<String>("")` and `time: mutableStateOf<String>("")` — two more Compose states for formatted date/time strings

These evolve independently. The UI reads `record.value` for text and type, reads `date`/`time` for the editable fields, and uses `_state` for loading/error. There is no single point of truth for what the "current record" is. When `save()` is called, `saveDate()` parses back from the string fields into `record.value` just before writing — the record is in an inconsistent state until that moment.

**File:** `viewmodel/RecordViewModel.kt`

**Recommendation:** Consolidate into a single `UiState` data class:
```kotlin
data class EditRecordUiState(
    val record: HitchLogRecord,
    val dateText: String,
    val timeText: String,
    val isLoading: Boolean,
    val error: String?
)
```
Expose as one `StateFlow<EditRecordUiState>`. The ViewModel updates it atomically. Eliminates the parsing-before-save pattern entirely.

---

### 3. `EditLogViewModel.state` and `HitchLogViewModel._state` are public mutable — HIGH

`EditLogViewModel.state` is declared as `val state = MutableStateFlow<...>` — not `.asStateFlow()`. Any code that obtains the ViewModel can write to it directly.

`HitchLogViewModel._state` is public (`val _state = MutableStateFlow<...>`) — the leading underscore convention signals "private", but the visibility is `public`.

**Files:** `viewmodel/EditLogViewModel.kt:19`, `viewmodel/HitchLogViewModel.kt:29`

**Recommendation:**
```kotlin
// EditLogViewModel
private val _state = MutableStateFlow<ViewState<HitchLog>>(ViewState.Loading)
val state = _state.asStateFlow()

// HitchLogViewModel
private val _state = MutableStateFlow<ViewState<HitchLogState>>(ViewState.Loading)
val state = _state.asStateFlow()
```

---

### 4. `AuthScreen` does domain mapping — HIGH

`AuthScreen.kt:81`:
```kotlin
viewModel.onLogin(User(it.uid, it.isAnonymous))
```
The composable constructs a domain `User` from a Firebase SDK `FirebaseUser` inline. Mapping from Firebase SDK types to domain types belongs in the data layer (`AuthService.toUser()` already exists). The composable should not know about Firebase types or domain construction.

Additionally, `AuthScreen` renders different UI states by inspecting `currentUser` directly rather than consuming a single unified `UiState`. This means login/logout/anonymous UI variations are controlled by null-checking presentation logic spread across multiple `if (currentUser == null || currentUser?.isAnonymous == true)` checks in the composable.

**Recommendation:** Move the Firebase→User mapping into `AuthViewModel`. The Google sign-in callback should pass the raw Firebase result to the ViewModel; the ViewModel calls `authService` or maps it internally. The screen should receive a single `AuthUiState` that drives all its rendering.

---

### 5. Save-then-navigate without result — HIGH

In both `EditLogScreen` and `EditRecordScreen`, the save button handler is:
```kotlin
Button(onClick = {
    viewModel.saveLog()  // or viewModel.save()
    finish()             // navigates away immediately
})
```
`saveLog()` uses `.launchIn(viewModelScope)` — fire-and-forget. If the Firestore write fails, the user is already gone from the screen. There's no confirmation the operation succeeded.

**Files:** `EditLogScreen.kt:57-60`, `EditRecordScreen.kt:51-55`

**Recommendation:** Save operations should update ViewState. On success, emit a navigation effect (one-shot `Channel<Unit>` or `SharedFlow`). The screen observes this effect via `LaunchedEffect` and navigates only on confirmed success. The save button should also be disabled while `ViewState.Loading` to prevent double-submits.

---

### 6. Auth state duplicated between `AuthService` and `AuthViewModel` — MEDIUM

`AuthService` owns a `StateFlow<User?>` built from `auth.authStateChanged`. `AuthViewModel` then re-derives `_currentUser` and `_isAuthenticated` by collecting from `authService.currentUser`. The ViewModel is re-publishing state that already exists as a flow. `isAuthenticated` is also redundant — it is `currentUser != null`.

Furthermore, `onAnonymousLogin()` manually sets `_isAuthenticated.value = true` before the `authService.currentUser` flow emits. This is a race condition: the ViewModel's state claims authenticated before the auth service confirms it.

**Recommendation:** `AuthViewModel` should expose `authService.currentUser` directly (mapped to a UI state), not re-derive it. Remove `_isAuthenticated` — derive it inline. Remove the manual `_isAuthenticated.value = true` assignment in `onAnonymousLogin()`.

---

### 7. `Repository` interface exposes `userId()` — MEDIUM

`userId(): String?` in the `Repository` interface leaks auth identity into the domain layer. The repository should own its own query scope (filtering by user) internally, without exposing who that user is to callers.

`EditLogViewModel` calls `repository.userId()` to populate the `userId` field when creating a new log. This is an auth concern surfacing in a ViewModel through the repository interface.

**Recommendation:** Remove `userId()` from `Repository`. `FirestoreRepository` gets `AuthService` as a dependency (it already does) and uses it internally. ViewModels never need to know the user ID.

---

### 8. Inconsistent data freshness model — MEDIUM

`getLogs()` and `getLogRecords()` use Firestore real-time snapshot listeners — they emit whenever Firestore data changes. `getLog()` and `getRecord()` use one-shot reads wrapped in `repositoryFlow`.

This means: the log list and record list update live, but a single log's detail (name, etc.) shown in `HitchLogScreen` does not update if changed elsewhere. `HitchLogViewModel` chains a one-shot `getLog()` with a real-time `getLogRecords()` — so records live-update but the log header does not.

**Recommendation:** Make `getLog()` a snapshot listener too (consistent with `getLogs()`), or document the deliberate choice. The inconsistency will cause subtle freshness bugs when the same data is displayed in multiple screens simultaneously.

---

### 9. Navigation state restoration in `MainActivity` is broken — MEDIUM

```kotlin
private var navController: NavHostController? = null  // never assigned

override fun onSaveInstanceState(...) {
    navController?.let { ... }  // always null, never saves
}
```
The `navController` field is never assigned — `rememberNavController()` returns a remembered value inside `setContent {}`, not assigned to the field. So nav state is never saved or restored on process death.

**File:** `androidMain/MainActivity.kt`

**Recommendation:** Use the standard pattern:
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
        val navController = rememberNavController()
        HitchLogApp(navController)
    }
}
```
Nav back stack is already handled by `rememberNavController` with `SavedStateHandle` under the hood via the Compose runtime — no manual save/restore needed. Remove the broken manual implementation.

---

### 10. No session restoration at startup — MEDIUM

`HitchLogApp` hardcodes `startDestination = Screen.Auth`. Every cold start sends the user to the auth screen, even if they are already signed in. `AuthService.currentUser` has an `initialValue = auth.currentUser?.toUser()` — the current user is synchronously available at startup.

**Recommendation:** The start destination should be conditional:
```kotlin
val startDestination = if (authService.currentUser.value != null) Screen.LogList else Screen.Auth
```
Or have `AuthScreen` auto-navigate immediately if already authenticated (it already does this via `LaunchedEffect`, but the user still briefly sees the auth screen).

---

### 11. Auth navigation flow is broken — MEDIUM

`Screen.Auth` is hardcoded as `startDestination` and is never popped from the back stack after login. This causes three problems:
- Every cold start while already signed in routes through Auth (covered partly by #10, but the fix there alone is not enough)
- Pressing back from `LogList` returns to Auth, which immediately auto-forwards to `LogList` again because `LaunchedEffect(uiState.isAuthenticated)` re-fires on re-entry — visible blink
- There is no explicit sign-out path; the only way to reach Auth after login is the broken back-press loop above

**Depends on:** #9 resolved first.

**Recommendation:**

1. Remove the broken `navController` field and `onSaveInstanceState` from `MainActivity` (Issue #9). `rememberNavController()` already restores the back stack on process death via `SavedStateHandle` — no manual implementation needed.

2. Make `startDestination` conditional on current auth state (subsumes #10):
```kotlin
val startDestination = if (authService.currentUser.value != null) Screen.LogList else Screen.Auth
```

3. Replace `LaunchedEffect(uiState.isAuthenticated)` in `AuthScreen` with a one-shot `Channel<Unit>` in `AuthViewModel` that fires only on an active login event, not on re-entry while already authenticated:
```kotlin
private val _navigationEvent = Channel<Unit>(Channel.CONFLATED)
val navigationEvent = _navigationEvent.receiveAsFlow()
```
Emit in `onLogin` and `onAnonymousLogin`. `AuthScreen` collects via `LaunchedEffect(Unit)`.

4. Pop `Screen.Auth` on successful login in `HitchLogApp.kt`:
```kotlin
navController.navigate(Screen.LogList) {
    popUpTo(Screen.Auth) { inclusive = true }
}
```

5. Add an explicit sign-out action in `LogListScreen` that navigates to `Screen.Auth` and clears the app stack:
```kotlin
navController.navigate(Screen.Auth) {
    popUpTo(Screen.LogList) { inclusive = true }
}
```

**Result:** Cold start while signed in → `LogList` directly. Back from `LogList` → exits app. Sign out via explicit action → `Auth`. Process death with back stack `[LogList, Log(...)]` → restored automatically.

**Files:** `androidMain/MainActivity.kt`, `ui/HitchLogApp.kt`, `ui/viewmodel/AuthViewModel.kt`, `ui/AuthScreen.kt`, `ui/screens/LogListScreen.kt`

---

### 12. Error model is raw strings throughout — MEDIUM

`Response.Failure(errorMessage: String)`, `ViewState.Error(error: String)`, and direct string display in UI. There is no error type hierarchy. Callers cannot distinguish network errors from auth errors from "not found" errors. Parse errors in `RecordViewModel.saveDate()` are completely silenced with an empty `catch` block.

**Recommendation:** Define a sealed `AppError` type (or at minimum an enum) in the domain layer. Distinguish at least: `NotAuthenticated`, `NotFound`, `NetworkError`, `ParseError`. Map at the repository boundary. This enables the UI to show appropriate messages and enables future retry logic.

---

### 13. `HitchLogViewModel` data-loading chain is fragile — MEDIUM

```kotlin
repository.getLog(logId)
    .onEach { response ->
        when (response) {
            is Response.Success -> {
                repository.getLogRecords(logId)
                    .collect { recordResponse -> ... }
            }
        }
    }.collect()
```

`getLogRecords` is subscribed inside `onEach` for `getLog`. If `getLog` emits a second value (e.g., once converted to a real-time listener), a new `getLogRecords` subscription is launched without cancelling the old one. This causes duplicate emissions and a subscription leak.

**Recommendation:** Use `flatMapLatest` to cancel the inner subscription when the outer emits:
```kotlin
repository.getLog(logId)
    .flatMapLatest { logResponse ->
        when (logResponse) {
            is Response.Success -> repository.getLogRecords(logId).map { ... }
            else -> flowOf(logResponse.toViewState())
        }
    }
    .collect { _state.value = it }
```

---

### 14. `AuthService` has dead API surface — LOW

`authenticate(email, password)` and `createUser(email, password)` are implemented but never called anywhere in the codebase.

**Recommendation:** Remove them or mark them explicitly as `// planned: email/password auth`.

---

### 15. `HitchLogState` defined inside a ViewModel file — LOW

`HitchLogState(log, records)` is defined at the top of `HitchLogViewModel.kt`. UI models used as ViewModel output belong in a dedicated model file or alongside the screen they serve.

---

### 16. Timestamp nanoseconds always zero — LOW

`LocalDateTime.toTimestamp()` in `Utils.kt` always passes `nanoseconds = 0`. The collision-resolution logic in `getNextTime()` uses nanosecond-precision Timestamps for ordering. Records created within the same second will collide after a round-trip through `LocalDateTime`.

---

## Severity Summary

| # | Issue | Severity |
|---|---|---|
| 1 | Domain model references `StringResource` | High |
| 2 | `RecordViewModel` split state | High |
| 3 | Public mutable state in two ViewModels | High |
| 4 | `AuthScreen` does domain mapping | High |
| 5 | Fire-and-forget saves with immediate navigation | High |
| 6 | Auth state duplicated with race condition | Medium |
| 7 | `userId()` in Repository interface | Medium |
| 8 | Inconsistent real-time vs one-shot reads | Medium |
| 9 | Nav state restoration broken in `MainActivity` | Medium |
| 10 | No session restoration at startup | Medium |
| 11 | Auth navigation flow broken (blink, no sign-out path) | Medium |
| 12 | Error model is raw strings / parse errors silenced | Medium |
| 13 | `flatMapLatest` needed in `HitchLogViewModel` | Medium |
| 14 | Dead methods in `AuthService` | Low |
| 15 | `HitchLogState` in ViewModel file | Low |
| 16 | Nanoseconds lost in timestamp conversion | Low |

---

## Suggested Target Structure

No modularization needed at this scale — one module is correct. Within `commonMain`, the main structural changes:

```
domain/
  Data.kt            — remove StringResource from HitchLogRecordType
  Repository.kt      — remove userId()
  Response.kt        — consider sealed AppError
  User.kt

data/
  AuthService.kt     — remove dead authenticate/createUser
  FirestoreRepository.kt — getLog() → snapshot listener; userId filtering internal
  FirestoreHitchLogRecord.kt

ui/
  viewmodel/
    AuthViewModel.kt       — expose authService.currentUser directly; remove isAuthenticated
    LogListViewModel.kt    — no change needed
    EditLogViewModel.kt    — private _state + .asStateFlow()
    HitchLogViewModel.kt   — flatMapLatest; private _state
    RecordViewModel.kt     — single EditRecordUiState; no mutableStateOf
  screens/
    AuthScreen.kt          — no Firebase types; consume AuthUiState only
    EditLogScreen.kt       — navigate on confirmed success via effect
    EditRecordScreen.kt    — navigate on confirmed success via effect
    HitchLogScreen.kt      — move timeFormat out of composable
    LogListScreen.kt
  HitchLogApp.kt           — conditional start destination
  Nav.kt

androidMain/
  MainActivity.kt    — remove broken nav state save/restore
```

---

## Open Risks

- **`HitchLogViewModel` subscription leak** (issue #12) — exists today in production code and will produce duplicated UI updates if `getLog()` ever emits more than once (it does not with the current one-shot implementation, but once made real-time it will).
- **Anonymous user data loss** — signing out an anonymous user deletes their Firebase account and all their Firestore data. There is no user-facing warning. This is a silent destructive action.
- **Moscow time discrepancy** — competition rules require chronicle times in Moscow time (UTC+3), but the app stores local device timezone. For users in other timezones this will produce incorrect chronicle times. No timezone conversion exists.
- **`getNextTime()` uses `Source.CACHE`** — collision resolution only looks at cached records. If the device has no cached data (first open, or after cache eviction), two records at the same minute will not be deduplicated. This can produce inconsistent ordering after a sync.
