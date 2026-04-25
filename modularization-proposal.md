# Modularization Proposal: HitchlogMP Feature Expansion

_Date: 2026-04-24_

---

## Short Answer

The current single-module architecture can hold all features, but three of them — **photos, GPS, and Excel export** — will each require substantial platform-specific code that will make the single `build.gradle.kts` a dependency dumping ground and `androidMain`/`iosMain` source sets unwieldy. That is the real forcing function for modularization, not the logical complexity of the features.

**Email auth** requires building full custom UI flows in Compose Multiplatform — no drop-in FirebaseUI equivalent exists for KMP. The backend SDK calls are all available; only the screens are missing.

The other four features (**edit history, rest counting, smart buttons, race/participant data**) are pure Kotlin and fit cleanly in the existing structure with no module boundary changes.

---

## Feature-by-Feature Breakdown

### Fits in single module as-is

| Feature | Why it fits | What's needed |
|---|---|---|
| Rest time counting | Pure domain calculation | Add `restBudget: RestBudget` to `HitchLog`; a function scanning REST_ON/REST_OFF pairs; show result in `HitchLogViewModel` state |
| Smart record type buttons | Pure domain state machine | A `fun nextLikelyTypes(records: List<HitchLogRecord>): List<HitchLogRecordType>` function in domain; `HitchLogViewModel` exposes suggestions |
| Edit history | New Firestore subcollection + domain model | `RecordEdit` domain model; wrap mutations in `FirestoreRepository` to write history entries; new `HistoryScreen` + ViewModel |
| Race / checkpoint / participant | New data vertical | New domain models, new repository or new methods on existing one, new screens — grows the codebase but needs no new module |

### Requires custom UI but no platform code

**Email authentication** — There is no FirebaseUI equivalent for KMP. No library provides drop-in email/password screens for Compose Multiplatform. However, the GitLive Firebase SDK 2.4.0 (upgrade from current 2.1.0) exposes all needed calls in `commonMain`, and KMPAuth 2.3.1 (upgrade from current 2.2.0) already covers Google and Apple with branded buttons.

What must be built as custom Compose screens:

| Screen | GitLive SDK call |
|---|---|
| Sign In | `signInWithEmailAndPassword` |
| Sign Up | `createUserWithEmailAndPassword` |
| Forgot Password | `sendPasswordResetEmail` + success state |
| Email Verification gate | `sendEmailVerification`, `user.reload()`, poll `isEmailVerified` |

The auth ViewModel drives navigation via `StateFlow<AuthState>` derived from `authStateChanged` — replacing the automatic routing FirebaseUI handled. All four screens are pure Compose in `commonMain`, no platform code required.

**Phone auth** is the exception: the verification callback is platform-specific (Activity result on Android, APNS silent push on iOS), so it needs `expect/actual` bridges. It is the most complex auth type and the least critical — recommend implementing it last or not at all if Google + Apple + Email covers the user base.

**Auth library versions to use:**
- `io.github.mirzemehdi:kmpauth-firebase:2.3.1` — Google + Apple (upgrade from 2.2.0)
- `dev.gitlive:firebase-auth:2.4.0` — all backend calls (upgrade from 2.1.0)

### Pushes toward platform-specific code

**Photos** — Camera and gallery access have no KMP-native API. On Android you use `ActivityResultContracts.PickVisualMedia`; on iOS you need `PHPickerViewController`. These are `expect/actual` bridges. Firebase Storage (GitLive SDK has it) can be in `commonMain`. Image loading (Coil for KMP) also lives in `commonMain`. So: one platform bridge function (`expect fun pickPhoto(): Flow<ByteArray?>`) and new Firebase Storage service — manageable in a single module but adds meaningful platform source set weight.

**GPS location** — Same pattern. `FusedLocationProviderClient` on Android, `CLLocationManager` on iOS. Runtime permissions differ per platform. The domain side (`GeoPoint` — already commented out in `Data.kt`) is trivial. The `expect/actual` bridge is the heavy part. A background location use case (auto-logging position while driving) would also need a foreground Service on Android.

**Excel export** — There is no KMP-native Excel library. Realistic options: generate CSV (works everywhere, trivial), or use a platform bridge where Android uses Apache POI / a lightweight XLSX writer, and iOS either opens CSV directly or calls a native library. File sharing is also platform-specific (`FileProvider` + `ShareSheet` on Android, `UIActivityViewController` on iOS). This is the most platform-divergent feature of the seven.

---

## Recommendation: Light, Incremental Modularization — Not a Big-Bang Split

### Why not full feature modules right now

The app is currently ~1,400 lines. Even with all seven features it will likely stay under 8,000 lines. Full feature modularization at this size adds Gradle configuration overhead, makes `expect/actual` harder to wire across module boundaries, and introduces boilerplate that slows development without giving meaningful build-time wins on a project this small.

### The actual problem to solve first

Before expanding the codebase, fix the five HIGH severity issues from the architecture review. Adding photos/GPS/export on top of `RecordViewModel`'s fragmented state (three parallel state sources) and domain models with `StringResource` dependencies will make the new features significantly harder to implement and test correctly.

### The right split when you get to photos/GPS/export

Extract one thin platform-bridge module. Everything else stays in `:composeApp`.

```
settings.gradle.kts
  include(":composeApp")
  include(":platform")        ← new

:platform/
  src/
    commonMain/   — expect declarations
      LocationProvider.kt       (expect class)
      PhotoPicker.kt            (expect class)
      FileExporter.kt           (expect class)
    androidMain/  — Android implementations
    iosMain/      — iOS implementations
  build.gradle.kts  (KMP library, no Compose dependency)
```

`:composeApp` depends on `:platform`. The domain and data layers never depend on `:platform` — only ViewModels and screens that need the hardware bridges.

This gives you:
- Clean `expect/actual` contracts with all platform code isolated in one place
- `build.gradle.kts` in `:composeApp` stays focused on Firebase, Compose, and Koin
- No Compose dependency in `:platform` — platform code stays testable independently
- Easy to test: fake implementations of `LocationProvider`, `PhotoPicker`, `FileExporter` for ViewModel tests

### If race/participant data grows large

If that feature turns into a significant sub-product (race administration, judge tooling, live leaderboard), extract it into `:feature:race` at that point. Don't create the module until the feature scope is defined and its data model is stable — premature feature modules in KMP cost more than they save.

---

## Proposed Layout After All Features Are Added

```
:composeApp                       — Android/iOS entry points + app-level DI
  src/commonMain
    domain/
      chronicle/                  — HitchLog, HitchLogRecord, RecordType (no StringResource)
      race/                       — Race, Checkpoint, Participant
      auth/                       — User
    data/
      firestore/                  — FirestoreRepository, FirestoreHitchLogRecord
      storage/                    — PhotoStorageService (Firebase Storage)
      auth/                       — AuthService
    ui/
      auth/                       — SignInScreen, SignUpScreen, ForgotPasswordScreen, EmailVerificationScreen
      chronicle/                  — log list, record editing, history screens
      race/                       — race detail, checkpoint list, participant list
      export/                     — export screen (delegates to :platform FileExporter)

:platform                         — hardware bridges (camera, GPS, file export, phone auth)
  src/
    commonMain/
      expect class LocationProvider
      expect class PhotoPicker
      expect class FileExporter
      expect class PhoneAuthHandler   ← only if phone auth is in scope
    androidMain/   — concrete implementations
    iosMain/       — concrete implementations
```

---

## Sequencing

1. **Fix HIGH severity issues** (domain `StringResource`, `RecordViewModel` state, public mutable flows, save-then-navigate). These are prerequisites for everything else.
2. **Upgrade KMPAuth to 2.3.1 + GitLive SDK to 2.4.0** — minor version bumps, no structural change.
3. **Build email auth screens** (`SignInScreen`, `SignUpScreen`, `ForgotPasswordScreen`, `EmailVerificationScreen`) — pure Compose in `commonMain`, replaces the current placeholder `AuthScreen`.
4. **Add rest counting + smart buttons** — pure domain, no structural change.
5. **Add edit history** — new data subcollection, new screen, no platform code.
6. **Add race/checkpoint/participant** — new domain vertical, stays in `:composeApp`.
7. **Extract `:platform` module**, then add GPS → Photos → Export in that order (GPS and photos share location/media permissions setup; export is fully independent).
8. **Phone auth** (optional) — add `PhoneAuthHandler` expect/actual to `:platform` after everything else is stable.
