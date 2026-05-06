# Firebase Configuration Setup

This project uses Firebase and requires configuration credentials. These credentials are kept secret and not committed to the repository.

## Setup Instructions

### 1. Android/iOS Development

Copy `local.properties.example` to `local.properties` and fill in your Firebase credentials:

```bash
cp local.properties.example local.properties
```

Then edit `local.properties` and add your Firebase configuration:

```properties
firebase.apiKey=YOUR_API_KEY_HERE
firebase.authDomain=YOUR_AUTH_DOMAIN_HERE
firebase.projectId=YOUR_PROJECT_ID_HERE
firebase.storageBucket=YOUR_STORAGE_BUCKET_HERE
firebase.gcmSenderId=YOUR_GCM_SENDER_ID_HERE
firebase.applicationId=YOUR_APPLICATION_ID_HERE
```

**Note:** `local.properties` is already in `.gitignore` and will not be committed.

### 2. Web Development

For web builds, you have two options:

#### Option A: Environment Variables (Recommended for CI/CD)

Set environment variables before building:

```bash
export FIREBASE_API_KEY="your_api_key"
export FIREBASE_AUTH_DOMAIN="your_auth_domain"
export FIREBASE_PROJECT_ID="your_project_id"
export FIREBASE_STORAGE_BUCKET="your_storage_bucket"
export FIREBASE_GCM_SENDER_ID="your_gcm_sender_id"
export FIREBASE_APPLICATION_ID="your_application_id"

./gradlew jsBrowserDevelopmentRun
```

### 3. Download google-services.json (Android)

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project
3. Go to Project Settings (gear icon)
4. Scroll down to "Your apps" section
5. Find your Android app
6. Click **"Download google-services.json"**
7. Place it in `composeApp/google-services.json`

**Note:** `google-services.json` is gitignored and will not be committed.

### 4. Download GoogleService-Info.plist (iOS)

1. In Firebase Console → Project Settings
2. Find your iOS app
3. Click **"Download GoogleService-Info.plist"**
4. Place it in `iosApp/GoogleService-Info.plist`

**Note:** `GoogleService-Info.plist` is gitignored and will not be committed.

### 5. Getting Firebase Credentials (for local.properties)

The values in `local.properties` can be found in your `google-services.json`:
- `firebase.apiKey` → `client[0].api_key[0].current_key`
- `firebase.projectId` → `project_info.project_id`
- `firebase.storageBucket` → `project_info.storage_bucket`
- `firebase.gcmSenderId` → `project_info.project_number`
- `firebase.applicationId` → `client[0].client_info.mobilesdk_app_id`
- `firebase.authDomain` → Usually `{projectId}.firebaseapp.com`

## Security Notes

- **Never commit** `local.properties` or `.env` files
- **Never commit** files containing API keys or secrets
- The example files (`local.properties.example`, `.env.example`) are safe to commit as they contain no real credentials
- For production deployments, use environment variables or secure secret management services

## Files to Keep Secret

- `local.properties` - Contains Firebase config for Android/iOS
- `.env` - Contains Firebase config for web
- `composeApp/google-services.json` - Android Firebase config
- `iosApp/GoogleService-Info.plist` - iOS Firebase config

All of these files are already in `.gitignore`.

## Example Files (Safe to Commit)

- `local.properties.example` - Template for local.properties
- `.env.example` - Template for .env
- `composeApp/google-services.json.example` - Template for google-services.json

These example files contain placeholder values and are safe to commit to version control.
