<p align="center">
  <h1 align="center">👁️ PeeP</h1>
  <p align="center">
    <strong>See what your friends are up to — in real time.</strong>
  </p>
  <p align="center">
    <a href="#features">Features</a> •
    <a href="#tech-stack">Tech Stack</a> •
    <a href="#getting-started">Getting Started</a> •
    <a href="#project-structure">Project Structure</a> •
    <a href="#architecture">Architecture</a> •
    <a href="#license">License</a>
  </p>
  <p align="center">
    <img src="https://img.shields.io/badge/React_Native-0.81-61DAFB?style=flat-square&logo=react&logoColor=white" alt="React Native" />
    <img src="https://img.shields.io/badge/Expo-54-000020?style=flat-square&logo=expo&logoColor=white" alt="Expo" />
    <img src="https://img.shields.io/badge/Supabase-Backend-3FCF8E?style=flat-square&logo=supabase&logoColor=white" alt="Supabase" />
    <img src="https://img.shields.io/badge/TypeScript-5.9-3178C6?style=flat-square&logo=typescript&logoColor=white" alt="TypeScript" />
    <img src="https://img.shields.io/badge/License-MIT-yellow?style=flat-square" alt="License" />
  </p>
</p>

---

## 📖 About

**PeeP** is a social mobile app that lets you see what apps your friends are currently using — in real time. Tap on a friend to "peep" them and instantly find out if they're watching YouTube, scrolling Instagram, or just chilling. It's a fun, lightweight way to stay connected without the noise of traditional social media.

> **Think of it as a playful "Are you free?" signal for your inner circle.**

---

## ✨ Features

| Feature | Description |
|---|---|
| 👁️ **Real-Time Activity** | See what app your friends are currently using |
| 📡 **Live Broadcasting** | Your activity status broadcasts automatically in the background |
| 🔔 **Push Notifications** | Get notified when someone peeps you via FCM |
| 👥 **Friend System** | Add friends and manage your inner circle |
| 🔐 **Authentication** | Secure sign-up / login via Supabase Auth |
| 🎨 **Dark Mode First** | Sleek pure black & white theme |
| 📱 **Tab Navigation** | Home, Notifications & Profile with smooth tab bar |
| 🫣 **Haptic Feedback** | Subtle vibrations for peep interactions |

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| **Framework** | [React Native](https://reactnative.dev/) `0.81` + [Expo](https://expo.dev/) `54` |
| **Routing** | [Expo Router](https://docs.expo.dev/router/introduction/) (file-based) |
| **State Management** | [Zustand](https://github.com/pmndrs/zustand) |
| **Backend / DB** | [Supabase](https://supabase.com/) (Auth, Postgres, Realtime, Edge Functions) |
| **Push Notifications** | Expo Notifications + FCM via Supabase Edge Functions |
| **Native Module** | Custom `UsageStats` Expo Module (Android `UsageStatsManager` API) |
| **Animations** | React Native Reanimated |
| **Icons** | Lucide React Native |
| **Language** | TypeScript `5.9` |

---

## 🚀 Getting Started

### Prerequisites

- **Node.js** ≥ 18
- **npm** or **yarn**
- **Expo CLI** — `npm install -g expo-cli`
- **Android Studio** (for Android development / emulator)
- A [Supabase](https://supabase.com/) project with the required schema (see `supabase/schema.sql`)

### Installation

```bash
# 1. Clone the repository
git clone https://github.com/wtfmahe/PeeP.git
cd PeeP

# 2. Install dependencies
npm install

# 3. Set up environment variables
#    Create a .env file in the root with your Supabase credentials:
#    EXPO_PUBLIC_SUPABASE_URL=your_supabase_url
#    EXPO_PUBLIC_SUPABASE_ANON_KEY=your_anon_key

# 4. Set up the database
#    Run the SQL files in the `supabase/` folder against your Supabase project:
#    - schema.sql        → Core tables (profiles, peeps, friends)
#    - user_status.sql   → Real-time status tracking table
#    - add_fcm_token.sql → FCM token storage for push notifications

# 5. Start the dev server
expo start

# 6. Run on Android
expo run:android
```

> ⚠️ **Note:** The UsageStats native module requires **Android** with **Usage Access** permission. This feature is not available on iOS.

---

## 📂 Project Structure

```
PeeP/
├── app/                        # Expo Router — file-based routing
│   ├── (tabs)/                 # Bottom tab navigation group
│   │   ├── _layout.tsx         # Tab bar configuration
│   │   ├── index.tsx           # Home screen — friend list + peep
│   │   ├── notifications.tsx   # Notifications screen
│   │   └── profile.tsx         # Profile screen
│   ├── auth/                   # Auth flow
│   │   ├── _layout.tsx         # Auth stack layout
│   │   ├── login.tsx           # Login screen
│   │   └── signup.tsx          # Sign-up screen
│   ├── _layout.tsx             # Root layout — auth guard + navigation
│   └── friends.tsx             # Add/manage friends screen
├── components/
│   ├── 3d/                     # 3D components (PeepEye)
│   ├── feature/                # Feature-specific components
│   │   └── FriendCard.tsx      # Friend card with peep button
│   └── ui/                     # Reusable UI components
│       └── Toast.tsx           # Custom toast notifications
├── constants/
│   └── Colors.ts               # Theme & color system
├── lib/
│   └── supabase.ts             # Supabase client & type definitions
├── modules/
│   └── usage-stats/            # Custom Expo native module
│       ├── android/            # Kotlin — UsageStatsManager integration
│       ├── ios/                # Swift — placeholder
│       └── src/                # TypeScript bindings
├── services/
│   ├── NotificationService.ts  # Push notification management
│   ├── PeepService.ts          # Peep business logic
│   ├── StatusBroadcaster.ts    # Status broadcasting service
│   └── MockPeepService.ts     # Mock service for testing
├── stores/
│   ├── authStore.ts            # Zustand — authentication state
│   └── friendStore.ts          # Zustand — friend list & status state
├── supabase/
│   ├── schema.sql              # Core database schema
│   ├── user_status.sql         # Status tracking table
│   ├── add_fcm_token.sql       # FCM token migration
│   └── functions/
│       └── send-peep-notification/  # Edge function for push notifications
├── assets/                     # App icons, splash screen, sounds
├── app.json                    # Expo configuration
├── package.json                # Dependencies & scripts
└── tsconfig.json               # TypeScript configuration
```

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────┐
│                   React Native UI               │
│  ┌───────────┐  ┌──────────────┐  ┌───────────┐ │
│  │   Home    │  │ Notifications│  │  Profile  │ │
│  │  (Tabs)   │  │    (Tabs)    │  │  (Tabs)   │ │
│  └─────┬─────┘  └──────────────┘  └───────────┘ │
│        │                                         │
│  ┌─────▼──────────────────────────────────────┐  │
│  │         Zustand State Management           │  │
│  │    authStore  ·  friendStore                │  │
│  └─────┬──────────────────────────────────────┘  │
│        │                                         │
│  ┌─────▼──────────────────────────────────────┐  │
│  │          Service Layer                     │  │
│  │  PeepService · StatusBroadcaster ·         │  │
│  │  NotificationService                       │  │
│  └─────┬──────────────────────────────────────┘  │
├────────┼─────────────────────────────────────────┤
│  ┌─────▼──────────────────────────────────────┐  │
│  │       Native Module (Android)              │  │
│  │    UsageStatsManager — foreground app      │  │
│  └────────────────────────────────────────────┘  │
└────────┬─────────────────────────────────────────┘
         │
    ┌────▼────────────────────────────────────┐
    │           Supabase Backend              │
    │  ┌──────────┐  ┌───────────────────┐    │
    │  │   Auth   │  │  Postgres (RLS)   │    │
    │  └──────────┘  │  profiles · peeps │    │
    │  ┌──────────┐  │  friends · status │    │
    │  │ Realtime │  └───────────────────┘    │
    │  └──────────┘  ┌───────────────────┐    │
    │                │  Edge Functions   │    │
    │                │  (push notifs)    │    │
    │                └───────────────────┘    │
    └─────────────────────────────────────────┘
```

### How It Works

1. **Status Broadcasting** — When the app is active, it polls the Android `UsageStatsManager` every 15s (30s in background) to detect the foreground app, and upserts the status to Supabase.
2. **Real-Time Sync** — The app subscribes to Supabase Realtime channels to receive live friend status updates without polling.
3. **Peeping** — Tapping a friend's card fetches their latest status, records the peep in the database, and triggers a push notification via a Supabase Edge Function.
4. **Auth Guard** — Expo Router's layout system handles auth-based navigation: unauthenticated users are redirected to login, authenticated users land on the tab navigator.

---

## 🤝 Contributing

Contributions, issues, and feature requests are welcome! Feel free to check the [issues page](https://github.com/wtfmahe/PeeP/issues).

1. Fork the project
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 📄 License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.

---

## 👤 Author

**Mahe**

- GitHub: [@wtfmahe](https://github.com/wtfmahe)

---

<p align="center">
  Made with 👁️ and ❤️
</p>
