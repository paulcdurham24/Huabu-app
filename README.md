# Huabu 🌟

> **Your World. Your Page.** — A MySpace-inspired social network Android app.

## Overview

Huabu brings back the spirit of early 2000s personal expression on the web, reimagined as a modern Android app. Users get fully customizable profile pages, a Top 8 friends grid, profile songs, mood indicators, About Me sections, and a colorful neon aesthetic.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin |
| UI | Jetpack Compose + Material3 |
| Navigation | Navigation Compose |
| DI | Hilt |
| Database | Room |
| Network | Retrofit + OkHttp |
| Images | Coil |
| Build | AGP 8.5.2 |
| NDK | r28 (16 KB page-size aligned) |

## 16 KB Page Size Alignment

This project is fully configured for Android 15+ 16 KB ELF page-size alignment:

- **NDK version**: `28.0.13004108` (r28+)
- **CMake linker flags**: `-Wl,-z,max-page-size=16384 -Wl,-z,common-page-size=16384`
- **JNI packaging**: `useLegacyPackaging = false`
- **AGP**: 8.5.2 (supports 16 KB alignment verification)

## Features

- **Profile Pages** — MySpace-style customizable profiles with banner, avatar, bio, mood, song widget, About Me, Top 8 friends, interests, and stat counters
- **Feed** — Scrollable post feed with like/comment/share actions, mood tags, hashtags, and glitter animation header
- **Top Friends** — Classic 8-slot Top Friends grid on profile pages
- **Profile Song** — Music player widget showing current profile song
- **Messages** — Inbox with unread badges and conversation list
- **Friends** — Grid view of friends with online indicators, friend requests, and friend suggestions
- **Discover** — Search users by name/username/interests with trending tags
- **Compose Post** — Rich post composer with mood picker, tags, and media attachment stubs

## Project Structure

```
app/
├── src/main/
│   ├── cpp/                    # NDK native layer (16 KB aligned)
│   │   ├── CMakeLists.txt
│   │   └── huabu_native.cpp
│   ├── java/com/huabu/app/
│   │   ├── data/
│   │   │   ├── local/          # Room database + DAOs
│   │   │   ├── model/          # Data models
│   │   │   └── remote/         # Retrofit API service
│   │   ├── di/                 # Hilt modules
│   │   ├── ui/
│   │   │   ├── components/     # Shared composables
│   │   │   ├── navigation/     # NavGraph
│   │   │   ├── screens/        # Feature screens
│   │   │   └── theme/          # Colors, typography, theme
│   │   ├── HuabuApplication.kt
│   │   └── MainActivity.kt
│   └── res/
```

## Setup

1. Open in **Android Studio Ladybug** (or newer)
2. Set SDK path in `local.properties`:
   ```
   sdk.dir=C\:\\Users\\YourUser\\AppData\\Local\\Android\\Sdk
   ```
3. Ensure **NDK r28** is installed: SDK Manager → SDK Tools → NDK (Side by side) → 28.x
4. Sync Gradle and run on a device/emulator with API 26+

## Requirements

- Android Studio Ladybug (2024.2+)
- Android SDK 35
- NDK r28 (`28.0.13004108`)
- JDK 17
- Gradle 8.7

## Minimum SDK

API 26 (Android 8.0 Oreo)
