# Auth UI Options Research: KMP / Compose Multiplatform

_Date: 2026-04-24_

---

## No FirebaseUI Equivalent Exists for KMP

There is no drop-in auth UI library for Compose Multiplatform. Every option found either covers only social OAuth buttons, targets Supabase instead of Firebase, or provides SDK logic with no UI. The email flow screens must be built custom.

---

## What You Get for Free (Libraries)

### KMPAuth — upgrade from 2.2.0 to 2.3.1

Already in the project at 2.2.0; latest stable is 2.3.1 (January 2025).

Covers:
- Google Sign-In — button + Firebase credential flow
- Apple Sign-In — button + Firebase credential flow (Android + iOS)
- GitHub Sign-In — button + Firebase credential flow
- Facebook — added in 2.4.0-alpha / 2.5.0-alpha as a separate module (not yet stable)

This covers Google and Apple with no custom code beyond what is already in the project.

### GitLive Firebase SDK — upgrade from 2.1.0 to 2.4.0

Exposes all needed backend calls in `commonMain`:

| Call | Purpose |
|---|---|
| `createUserWithEmailAndPassword` | Sign up |
| `signInWithEmailAndPassword` | Sign in |
| `sendPasswordResetEmail` / `confirmPasswordReset` | Forgot password flow |
| `sendEmailVerification` | Post-registration verification |
| `user.reload()` + `isEmailVerified` | Check verification status |
| `PhoneAuthProvider` + `PhoneAuthCredential` | Phone/SMS auth |

---

## What Must Be Built Custom

### Email auth screens (`commonMain`, Compose Multiplatform)

| Screen | SDK calls |
|---|---|
| Sign In | `signInWithEmailAndPassword` |
| Sign Up | `createUserWithEmailAndPassword` → triggers email verification |
| Forgot Password | `sendPasswordResetEmail` + success confirmation state |
| Email Verification gate | `sendEmailVerification`, `user.reload()`, poll `isEmailVerified` |

These are standard Compose screens with a ViewModel driving a `StateFlow<AuthUiState>`. The backend calls all exist in the GitLive SDK — only the UI and flow orchestration are missing.

### Phone auth — hardest, `expect/actual` required

The GitLive SDK exposes `PhoneAuthProvider` in `commonMain`, but the verification callback mechanism is fundamentally platform-specific:
- **Android** — verification result comes via an Activity callback / broadcast
- **iOS** — requires APNS silent push configuration; callback arrives via an app delegate method

This means phone auth needs `expect/actual` bridge implementations in `androidMain` / `iosMain` in addition to common UI screens (phone number entry + OTP input). It also requires Firebase console APNS configuration for iOS.

---

## Recommended Approach

```
KMPAuth 2.3.1          — Google + Apple buttons and credential flow (upgrade from 2.2.0)
GitLive SDK 2.4.0      — all email/password + phone backend calls (upgrade from 2.1.0)

Custom Compose screens (commonMain):
  SignInScreen           — email + password fields + social buttons
  SignUpScreen           — email + password + confirm password
  ForgotPasswordScreen   — email field → reset email sent confirmation
  EmailVerificationScreen — "check your inbox" + re-send + reload + proceed

expect/actual bridge (only if phone auth is in scope):
  androidMain/PhoneAuthHandler.kt
  iosMain/PhoneAuthHandler.kt
```

The auth ViewModel drives navigation via a `StateFlow<AuthState>` derived from `authService.authStateChanged`. When Firebase confirms the user is signed in and email is verified, the app navigates to `LogList`. This replaces the automatic screen-routing role that FirebaseUI-Android previously handled.

---

## Recommendation on Phone Auth

Implement it last or treat it as optional. Google + Apple + Email covers the vast majority of users. Phone auth introduces APNS configuration overhead on iOS, `expect/actual` bridging, and SMS cost. If the target audience primarily has Google accounts, phone auth adds more complexity than value.

---

## Library Landscape (All Options Evaluated)

| Library | Supports Firebase | Auth Methods | UI Provided | Status |
|---|---|---|---|---|
| KMPAuth 2.3.1 | Yes | Google, Apple, GitHub | Buttons only | Stable, actively maintained |
| GitLive firebase-auth 2.4.0 | Yes | All (logic only) | None | Stable, actively maintained |
| firebase-auth-kmp 1.0.4 | Yes | Email, Google, Apple, Anon | None | Oct 2025 |
| kmp-multi-auth 1.0.4 | No (Supabase only) | Google, Apple, Magic Link | Full screens | Mar 2026 |
| firebase-cmp 1.0.2 | Yes (REST API) | Email only | None | May 2025 |
| KotlinMultiplatformAuth 0.3.4 | Partial (Supabase for email/phone) | Google, Apple, Supabase flows | Minimal | Apr 2026 |
| FirebaseUI-Android | Android only | All | Full screens | Not KMP |
