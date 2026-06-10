<p align="center">
  <img src="https://img.shields.io/badge/Android-000000?style=for-the-badge&logo=android&logoColor=3DDC84" />
  <img src="https://img.shields.io/badge/Kotlin-000000?style=for-the-badge&logo=kotlin&logoColor=7F52FF" />
  <img src="https://img.shields.io/badge/Jetpack_Compose-000000?style=for-the-badge&logo=jetpackcompose&logoColor=4285F4" />
  <img src="https://img.shields.io/badge/Supabase-000000?style=for-the-badge&logo=supabase&logoColor=3FCF8E" />
</p>

<h1 align="center">👁️ PeeP.</h1>

<p align="center">
  <strong>See what your friends are up to — in real time.</strong>
  <br />
  <em>Native Android • Kotlin • Jetpack Compose • Supabase</em>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/minSdk-26-blue?style=flat-square" />
  <img src="https://img.shields.io/badge/targetSdk-35-brightgreen?style=flat-square" />
  <img src="https://img.shields.io/badge/Compose_BOM-2025.05-4285F4?style=flat-square" />
  <img src="https://img.shields.io/badge/Hilt-2.56-orange?style=flat-square" />
  <img src="https://img.shields.io/badge/License-MIT-yellow?style=flat-square" />
</p>

---

## What is PeeP?

**PeeP** is a social Android app that lets you see which app your friends are currently using — in real time. Tap the **Peep** button, and within seconds you'll know if they're watching YouTube, doom-scrolling Instagram, or just chilling.

> **Think of it as a playful "Are you free?" signal for your inner circle.**

This is the **native Android** implementation — built from scratch in **Kotlin + Jetpack Compose**, with no cross-platform frameworks. Pure native performance.

---

## ⚡ Core Features

| | Feature | How it works |
|---|---|---|
| 👁️ | **Real-Time Peep** | Tap a friend → silent FCM wake → `UsageStatsManager` detects their foreground app → result displayed in under 5 seconds |
| 📡 | **Background Broadcasting** | Foreground service polls the device every 30s, upserts status to Supabase via PostgREST |
| 🔔 | **Push Notifications** | FCM data messages for silent device wake + notification messages for "You were peeped!" alerts |
| 👥 | **Friend System** | Search → Send Request → Accept/Reject → Bidirectional friendship with RLS-enforced privacy |
| 🔐 | **Auth + Session** | Supabase Auth with automatic JWT refresh via OkHttp `Authenticator` — no expired token errors |
| 🥷 | **Stealth Mode** | When caught using PeeP itself, shows playful messages like *"Also stalking someone 🕵️"* |
| 🫣 | **Cooldown System** | 30-second cooldown per friend prevents peep-spam |
| 📱 | **Boot Persistence** | `BOOT_COMPLETED` receiver restarts the broadcast service automatically after device reboot |

---

## 🏗️ Architecture

```
┌──────────────────────────────────────────────────────────────┐
│                    Presentation Layer                         │
│  ┌─────────────────────────────────────────────────────────┐ │
│  │              Jetpack Compose UI                         │ │
│  │  HomeScreen · FriendsScreen · NotificationsScreen       │ │
│  │  ProfileScreen · LoginScreen · SignupScreen             │ │
│  └─────────────────────┬───────────────────────────────────┘ │
│                        │                                      │
│  ┌─────────────────────▼───────────────────────────────────┐ │
│  │               ViewModels (Hilt-injected)                │ │
│  │  HomeVM · FriendsVM · NotificationsVM · AuthVM          │ │
│  └─────────────────────┬───────────────────────────────────┘ │
├────────────────────────┼──────────────────────────────────────┤
│                  Domain / Data Layer                          │
│  ┌─────────────────────▼───────────────────────────────────┐ │
│  │              Repositories                               │ │
│  │  AuthRepository · FriendRepository · StatusRepository   │ │
│  └─────────────────────┬───────────────────────────────────┘ │
│                        │                                      │
│  ┌─────────────────────▼───────────────────────────────────┐ │
│  │        SupabaseClient (OkHttp + DataStore)              │ │
│  │  ┌──────────────────────────────────────────────────┐   │ │
│  │  │ • Auto-refresh JWT via OkHttp Authenticator      │   │ │
│  │  │ • Proactive refresh every ~50 min in background  │   │ │
│  │  │ • Mutex-protected token refresh (no race conds)  │   │ │
│  │  │ • Suspendable OkHttp calls via coroutines        │   │ │
│  │  └──────────────────────────────────────────────────┘   │ │
│  └─────────────────────────────────────────────────────────┘ │
├──────────────────────────────────────────────────────────────┤
│                   Service Layer                               │
│  ┌───────────────────┐  ┌────────────────────────────────┐   │
│  │ StatusBroadcast   │  │ PeepFirebaseMessaging          │   │
│  │ Service           │  │ Service                        │   │
│  │ (Foreground)      │  │ (FCM data + notification)      │   │
│  │ 30s polling loop  │  │ Silent wake for peep requests  │   │
│  └───────────────────┘  └────────────────────────────────┘   │
│  ┌───────────────────┐  ┌────────────────────────────────┐   │
│  │ UsageStatsHelper  │  │ AppNameMapper                  │   │
│  │ (foreground app   │  │ (package → friendly name)      │   │
│  │  detection)       │  │ 30+ apps mapped                │   │
│  └───────────────────┘  └────────────────────────────────┘   │
│  ┌───────────────────┐                                        │
│  │ BootReceiver      │  ← Restarts service after reboot      │
│  └───────────────────┘                                        │
└──────────────────────────────────────────────────────────────┘
                         │
                    ┌────▼────────────────────────────────┐
                    │        Supabase Backend             │
                    │  Auth · PostgREST · Edge Functions  │
                    │  Realtime · RLS Policies            │
                    └─────────────────────────────────────┘
```

---

## 🛠️ Tech Stack

| Layer | Technology | Why |
|---|---|---|
| **Language** | Kotlin 2.x | Null-safety, coroutines, DSL-friendly |
| **UI** | Jetpack Compose + Material 3 | Declarative, reactive, modern Android UI |
| **DI** | Hilt (Dagger) + KSP | Compile-time dependency injection |
| **Networking** | OkHttp 4.12 | Interceptors for auth, authenticator for auto-retry on 401 |
| **Serialization** | Kotlinx Serialization | Type-safe JSON without reflection |
| **Session** | DataStore Preferences | Encrypted-compatible, coroutine-native persistence |
| **Backend** | Supabase (Auth, PostgREST, Edge Functions) | Open-source Firebase alternative with Postgres |
| **Push** | Firebase Cloud Messaging | Silent data messages + visible notifications |
| **Background** | Foreground Service (`specialUse`) | Keeps status broadcasting alive in background |
| **Build** | Gradle KTS + Version Catalogs | Type-safe build scripts |
| **Min SDK** | 26 (Android 8.0) | Covers 95%+ of active devices |
| **Target SDK** | 35 (Android 15) | Latest platform compliance |

---

## 📂 Project Structure

```
app/src/main/java/com/anonymous/peep/
├── PeepApp.kt                          # @HiltAndroidApp entry point
├── MainActivity.kt                     # Single-activity, Compose host
│
├── data/
│   ├── SupabaseClient.kt               # HTTP client with auto JWT refresh
│   ├── model/
│   │   └── Models.kt                   # @Serializable data classes
│   └── repository/
│       ├── AuthRepository.kt           # Sign up, sign in, sign out
│       ├── FriendRepository.kt         # Friends, requests, search
│       └── StatusRepository.kt         # User status CRUD
│
├── service/
│   ├── StatusBroadcastService.kt       # Foreground service — 30s polling
│   ├── PeepFirebaseMessagingService.kt # FCM handler (silent + visible)
│   ├── UsageStatsHelper.kt             # UsageStatsManager wrapper
│   ├── AppNameMapper.kt               # Package name → friendly emoji string
│   └── BootReceiver.kt                # BOOT_COMPLETED → restart service
│
├── ui/
│   ├── components/
│   │   ├── FriendCard.kt               # Reusable friend card with Peep button
│   │   └── PeepToast.kt               # Animated toast notification
│   ├── navigation/
│   │   └── PeepNavigation.kt          # NavHost with auth guard
│   ├── screens/
│   │   ├── auth/
│   │   │   ├── LoginScreen.kt
│   │   │   └── SignupScreen.kt
│   │   ├── home/
│   │   │   └── HomeScreen.kt           # Friend list + Peep flow
│   │   ├── friends/
│   │   │   └── FriendsScreen.kt        # Search + friend requests
│   │   ├── notifications/
│   │   │   └── NotificationsScreen.kt  # "Who peeped you" feed
│   │   └── profile/
│   │       └── ProfileScreen.kt        # Avatar, username, sign out
│   └── theme/
│       ├── Color.kt                    # Pure black & white palette
│       ├── Theme.kt                    # Material 3 dark theme
│       └── Type.kt                     # Typography scale
│
└── viewmodel/
    ├── AuthViewModel.kt                # Auth state + session management
    ├── HomeViewModel.kt                # Friends list + peep logic
    ├── FriendsViewModel.kt             # Search + friend requests
    ├── NotificationsViewModel.kt       # Peep history feed
    └── ProfileViewModel.kt            # Profile data + sign out
```

---

## 🔧 Engineering Highlights

### 🔑 Bulletproof JWT Management
The `SupabaseClient` implements a **dual-layer token refresh** strategy:

1. **Reactive (OkHttp Authenticator)** — On any `401`, the authenticator automatically refreshes the token and retries the request. A `X-Retry-After-Refresh` header prevents infinite loops.
2. **Proactive (Background Service)** — Every ~50 minutes (100 broadcast cycles), the foreground service proactively refreshes the JWT *before* it expires.
3. **Thread-safe** — Token refresh is protected by a Kotlin `Mutex` to prevent race conditions from concurrent requests.

### 📡 Background Status Broadcasting
- **Foreground Service** with `specialUse` type — survives app backgrounding
- **30-second polling loop** via `Handler` + `Runnable`
- **`BOOT_COMPLETED` receiver** restarts the service after device reboot
- **`START_STICKY`** — Android restarts the service if killed by the system

### 🛡️ Database Security (Supabase RLS)
- Users can only read statuses of **accepted friends**
- Users can only write their **own status**
- Friend requests are insert-only by the sender, update-only by the receiver
- Peep history is visible only to sender and recipient

---

## 🚀 Getting Started

### Prerequisites

- **Android Studio** Ladybug (2024.2+) or newer
- **JDK 17**
- A [Supabase](https://supabase.com/) project
- A [Firebase](https://console.firebase.google.com/) project with Cloud Messaging enabled

### Setup

```bash
# 1. Clone the repository
git clone https://github.com/mahidharnagolu/PeeP.git
cd PeeP
git checkout native-android

# 2. Add your google-services.json
#    Download from Firebase Console → Project Settings → Android app
#    Place in: app/google-services.json

# 3. Configure Supabase credentials
#    In app/build.gradle.kts, update:
#    buildConfigField("String", "SUPABASE_URL", "\"your-url\"")
#    buildConfigField("String", "SUPABASE_ANON_KEY", "\"your-key\"")

# 4. Run the SQL migrations against your Supabase project
#    supabase/schema.sql → Core tables
#    supabase/user_status.sql → Status tracking
#    supabase/add_fcm_token.sql → FCM token storage

# 5. Build and run
./gradlew installDebug
```

### Permissions

The app requires the following special permission:

| Permission | Purpose |
|---|---|
| `PACKAGE_USAGE_STATS` | Detect which app the user is currently using |
| `FOREGROUND_SERVICE` | Keep broadcasting status in the background |
| `POST_NOTIFICATIONS` | Show "You were peeped!" notifications |
| `RECEIVE_BOOT_COMPLETED` | Restart broadcast service after reboot |

> **Note:** `PACKAGE_USAGE_STATS` requires the user to manually enable it in **Settings → Apps → Special access → Usage access**.

---

## 📦 Build

```bash
# Debug build
./gradlew assembleDebug

# Release build (requires signing config)
./gradlew assembleRelease

# Install on connected device
./gradlew installDebug
```

The release build has **R8 minification** and **resource shrinking** enabled for a smaller APK.

---

## 🗄️ Database Schema

```sql
profiles     (id, username, avatar_url, daily_peeps_remaining, fcm_token)
friends      (id, user_id, friend_id, status)     -- 'pending' | 'accepted'
peeps        (id, from_user_id, to_user_id, detected_app, friendly_name)
user_status  (user_id, current_app, friendly_name, updated_at)
```

All tables use **Row Level Security (RLS)** — see `supabase/*.sql` for policies.

---

## 👤 Author

**Mahe** — [@mahidharnagolu](https://github.com/mahidharnagolu)

---

<p align="center">
  <strong>Built with 👁️ and ❤️ in pure Kotlin.</strong>
  <br />
  <sub>No React Native. No Flutter. Just Android.</sub>
</p>
