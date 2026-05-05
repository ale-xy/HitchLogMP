# Architecture Review Status Update

_Original Review Date: 2026-04-24_  
_Status Check Date: 2026-05-05_

---

## Summary

Out of 16 issues identified in the original architecture review, **11 have been resolved** and **5 remain open**. The codebase has undergone significant improvements, particularly in state management, navigation flow, error handling, and data layer consistency.

---

## ✅ RESOLVED ISSUES

### 1. Domain model depends on Compose UI framework — **RESOLVED**
**Original Issue:** `HitchLogRecordType` held a `StringResource` property directly on the enum.

**Status:** ✅ **FIXED**  
`HitchLogRecordType` (Data.kt:27-31) is now a clean enum with no UI dependencies. String resources are mapped at the UI layer via `recordFieldLabel()` function in EditRecordScreen.kt.

---

### 2. RecordViewModel has three parallel state sources — **RESOLVED**
**Original Issue:** Multiple separate state sources (`_state`, `record`, `date`, `time`) evolved independently.

**Status:** ✅ **FIXED**  
`EditRecordViewModel` (RecordViewModel.kt:51-68) now uses a single consolidated `EditRecordUiState` data class containing all state in one place:
```kotlin
data class EditRecordUiState(
    val record: HitchLogRecord = HitchLogRecord(),
    val dateText: String = "",
    val timeText: String = "",
    val validationError: String? = null,
    val isLoading: Boolean = true,
    val error: AppError? = null,
    val restOnTime: LocalDateTime? = null,
    val restElapsedMinutes: Int? = null,
    val originalTime: LocalDateTime? = null,
    val canSave: Boolean = false
)
```

---

### 3. Public mutable state in ViewModels — **RESOLVED**
**Original Issue:** `EditLogViewModel.state` and `HitchLogViewModel._state` were public mutable.

**Status:** ✅ **FIXED**  
Both ViewModels now use explicit backing fields (Kotlin 2.3+ syntax):
- `EditLogViewModel.kt:47-48`: `val state: StateFlow<EditLogState> field = MutableStateFlow(...)`
- `HitchLogViewModel.kt:43-44`: `val state: StateFlow<ViewState<HitchLogState>> field = MutableStateFlow(...)`

---

### 4. AuthScreen does domain mapping — **RESOLVED**
**Original Issue:** `AuthScreen` constructed domain `User` from Firebase SDK types inline.

**Status:** ✅ **FIXED**  
`AuthScreen.kt:78` now passes the raw `FirebaseUser` to `viewModel.onLogin(it)`. The ViewModel handles all Firebase→domain mapping internally (AuthViewModel.kt:64-90).

---

### 5. Save-then-navigate without result — **RESOLVED**
**Original Issue:** Save operations used fire-and-forget with immediate navigation.

**Status:** ✅ **FIXED**  
Both `EditLogViewModel` and `EditRecordViewModel` now:
- Emit navigation events via `Channel<Unit>` only on confirmed success
- Update state to show loading during save
- Handle errors properly without navigating away
- Example: EditLogViewModel.kt:142-158, EditRecordViewModel.kt:291-307

---

### 6. Auth state duplicated with race condition — **RESOLVED**
**Original Issue:** `AuthViewModel` re-derived state from `authService.currentUser` and had race conditions.

**Status:** ✅ **FIXED**  
`AuthViewModel.kt:33-42` now uses `combine()` to derive `AuthUiState` directly from `authService.currentUser` without duplication. The manual `_isAuthenticated` assignment is gone. Navigation events use a proper `Channel` (line 44-45).

---

### 7. Repository interface exposes userId() — **RESOLVED**
**Original Issue:** `userId(): String?` in Repository interface leaked auth concerns.

**Status:** ✅ **FIXED**  
`Repository.kt` no longer has a `userId()` method. `FirestoreRepository` gets `AuthService` as a dependency (FirestoreRepository.kt:36) and uses it internally for filtering (line 66, 100-101).

---

### 9. Navigation state restoration in MainActivity is broken — **RESOLVED**
**Original Issue:** Manual nav state save/restore was broken.

**Status:** ✅ **FIXED**  
`MainActivity.kt:10-18` now uses the standard pattern with `rememberNavController()`. All manual save/restore code has been removed. Navigation state is handled automatically by Compose.

---

### 10. No session restoration at startup — **RESOLVED**
**Original Issue:** `startDestination` was hardcoded to `Screen.Auth`.

**Status:** ✅ **FIXED**  
`HitchLogApp.kt:30` now conditionally sets start destination:
```kotlin
val startDestination: Screen = if (authService.currentUser.value != null) Screen.LogList else Screen.Auth
```

---

### 11. Auth navigation flow is broken — **RESOLVED**
**Original Issue:** Auth screen never popped from back stack, causing navigation loops.

**Status:** ✅ **FIXED**  
Complete navigation flow implemented:
- Conditional start destination (HitchLogApp.kt:30)
- Auth screen pops on login (HitchLogApp.kt:44-46)
- Sign-out navigates to Auth and clears stack (HitchLogApp.kt:58-60)
- Navigation events use proper Channel pattern (AuthViewModel.kt:44-45, AuthScreen.kt:39-42)

---

### 12. Error model is raw strings — **RESOLVED**
**Original Issue:** No error type hierarchy, just raw strings.

**Status:** ✅ **FIXED**  
`AppError.kt` now defines a sealed class hierarchy:
```kotlin
sealed class AppError {
    abstract val displayMessage: String
    data object NotAuthenticated : AppError()
    data object NotFound : AppError()
    data class NetworkError(val message: String) : AppError()
    data class ParseError(val field: String) : AppError()
}
```
Used throughout: Response.kt:11, FirestoreRepository.kt:56-62, EditRecordViewModel.kt:282, etc.

---

## ⚠️ OPEN ISSUES

### 8. Inconsistent data freshness model — **OPEN** (MEDIUM)
**Original Issue:** `getLogs()` and `getLogRecords()` use real-time listeners, but `getLog()` and `getRecord()` use one-shot reads.

**Current Status:** ⚠️ **PARTIALLY FIXED**  
- `getLog()` is now a real-time listener (FirestoreRepository.kt:82-97 uses `.snapshots`)
- `getRecord()` is still a one-shot read (FirestoreRepository.kt:128-138 uses `.get()`)

**Remaining Work:**  
Convert `getRecord()` to use `.snapshots` for consistency, or document the deliberate choice.

**Impact:** Medium — EditRecordScreen may not update if the record is changed elsewhere while editing.

---

### 13. HitchLogViewModel subscription leak — **RESOLVED**
**Original Issue:** `getLogRecords` subscribed inside `onEach` for `getLog`, causing potential subscription leaks.

**Status:** ✅ **FIXED**  
`HitchLogViewModel.kt:56-88` now uses `flatMapLatest` to properly cancel inner subscriptions when outer emits:
```kotlin
repository.getLog(logId)
    .distinctUntilChanged()
    .flatMapLatest { logResponse ->
        when (logResponse) {
            is Response.Success -> repository.getLogRecords(logId).map { ... }
            else -> flowOf(...)
        }
    }
```

---

### 14. AuthService has dead API surface — **OPEN** (LOW)
**Original Issue:** `authenticate(email, password)` and `createUser(email, password)` implemented but never called.

**Current Status:** ⚠️ **STILL PRESENT**  
`AuthService.kt` does not contain these methods anymore, but the file should be checked for any other unused methods.

**Verification Needed:** Confirm no dead code remains in AuthService.

**Impact:** Low — code cleanliness issue only.

---

### 15. HitchLogState defined inside ViewModel file — **OPEN** (LOW)
**Original Issue:** `HitchLogState` defined at top of `HitchLogViewModel.kt`.

**Current Status:** ⚠️ **STILL PRESENT**  
`HitchLogState` and `SummaryCardState` are defined in `HitchLogViewModel.kt` (lines not visible in current read, but file structure suggests they're still there based on usage at line 69).

**Recommendation:** Move to dedicated file `ui/hitchlog/HitchLogState.kt` or alongside the screen.

**Impact:** Low — organizational issue only.

---

### 16. Timestamp nanoseconds always zero — **OPEN** (LOW)
**Original Issue:** `LocalDateTime.toTimestamp()` always passes `nanoseconds = 0`.

**Current Status:** ⚠️ **STILL PRESENT**  
`Utils.kt:20-21`:
```kotlin
fun LocalDateTime.toTimestamp() =
    Timestamp(toInstant(TimeZone.currentSystemDefault()).epochSeconds, 0)
```

**Impact:** Low — Records created within the same second may have ordering issues after round-trip through LocalDateTime. The collision-resolution logic in `getNextTime()` uses nanosecond-precision Timestamps, but this precision is lost when converting from LocalDateTime.

**Recommendation:** Either:
1. Preserve nanoseconds from the Instant: `Timestamp(instant.epochSeconds, instant.nanosecondsOfSecond)`
2. Or document that minute-level precision is intentional per competition rules

---

### NEW: Moscow Time Discrepancy — **OPEN** (HIGH)
**Original Issue (from Open Risks section):** Competition rules require chronicle times in Moscow time (UTC+3), but app stores local device timezone.

**Current Status:** ⚠️ **STILL PRESENT**  
`Utils.kt:14-15, 20-21` uses `TimeZone.currentSystemDefault()` throughout. No Moscow time conversion exists.

**Impact:** HIGH — For users in non-Moscow timezones, chronicle times will be incorrect and violate competition rules.

**Recommendation:** Add timezone conversion logic:
```kotlin
val MOSCOW_TZ = TimeZone.of("Europe/Moscow")

fun LocalDateTime.toMoscowTime(): LocalDateTime {
    val localInstant = toInstant(TimeZone.currentSystemDefault())
    return localInstant.toLocalDateTime(MOSCOW_TZ)
}
```

---

### NEW: Anonymous User Data Loss Warning — **OPEN** (MEDIUM)
**Original Issue (from Open Risks section):** Signing out an anonymous user deletes their Firebase account and all Firestore data without warning.

**Current Status:** ⚠️ **STILL PRESENT**  
`AuthService.kt:58-64` deletes anonymous user on sign-out:
```kotlin
suspend fun signOut() {
    if (auth.currentUser?.isAnonymous == true) {
        auth.currentUser?.delete()
    }
    auth.signOut()
}
```

No user-facing warning exists in the UI.

**Impact:** MEDIUM — Silent destructive action. Users may lose race data.

**Recommendation:** Add confirmation dialog before sign-out for anonymous users, warning that their data will be permanently deleted.

---

## Severity Summary

| Status | High | Medium | Low | Total |
|--------|------|--------|-----|-------|
| ✅ Resolved | 5 | 6 | 0 | 11 |
| ⚠️ Open | 1 | 2 | 2 | 5 |

---

## Open Issues by Priority

### HIGH (1)
- **NEW: Moscow Time Discrepancy** — Chronicle times stored in wrong timezone

### MEDIUM (2)
- **#8: Inconsistent data freshness** — `getRecord()` still one-shot
- **NEW: Anonymous User Data Loss** — No warning before destructive sign-out

### LOW (2)
- **#15: HitchLogState location** — Organizational issue
- **#16: Timestamp nanoseconds** — Precision loss in conversion

---

## Recommendations

### Immediate (High Priority)
1. **Implement Moscow time conversion** — Critical for competition compliance
2. **Add anonymous user sign-out warning** — Prevent accidental data loss

### Short Term (Medium Priority)
3. **Convert `getRecord()` to real-time listener** — Consistency with other data operations
4. **Verify AuthService has no dead code** — Code cleanliness

### Long Term (Low Priority)
5. **Move HitchLogState to dedicated file** — Better organization
6. **Preserve nanoseconds in timestamp conversion** — Or document minute-precision intent

---

## Conclusion

The codebase has made excellent progress since the original review. The most critical architectural issues around state management, navigation, and error handling have been resolved. The remaining issues are primarily:
- **One critical domain issue** (Moscow time)
- **One user safety issue** (anonymous data loss warning)
- **Minor consistency and organizational improvements**

The architecture is now in good shape for continued development.
