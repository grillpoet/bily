[English](README.md) | [简体中文](README.zh-CN.md)

# Bily

A third-party Bilibili client for Android, built with Kotlin and Jetpack Compose. It only does search, playback, download, and local favorites management — no ads, no recommendation feed, no social features. All data comes from Bilibili's public APIs.

## Stack

UI is Jetpack Compose with Material3, using unidirectional data flow and ViewModels for state. Playback is Media3 ExoPlayer, handling HLS and DASH streams, with `Player.Listener` for playback state changes. Networking is OkHttp with kotlinx.serialization for JSON, request and response models live under `data/`. Images load via Coil, local storage uses DataStore Preferences, and favorites are serialized to JSON before being stored. Dependencies are managed through a Gradle Version Catalog, and release builds enable R8 and resource shrinking.

## Architecture

The code is layered, roughly MVVM:

    app/src/main/java/com/grillpoet/bily/
    ├── core/        Constants and Kotlin extensions
    ├── data/        API calls, download, favorites, data models
    ├── di/          Dependency injection container
    ├── nav/         Route definitions and navigation
    ├── ui/
    │   ├── screen/  Home, search, UP space, video player
    │   ├── theme/   Theme and colors
    │   ├── vm/      ViewModels
    │   ├── widget/  Reusable components
    │   └── icon/    Icons
    ├── LibiApp.kt
    └── MainActivity.kt

Network calls are wrapped in the `data/` layer, ViewModels call into them and expose UI state, and the Compose layer only consumes that state. DI is a lightweight manual container — no Hilt or Koin.

## Features

**Search and browsing**
Keyword search hits Bilibili's search API, results load with pagination. The UP space page pulls a creator's uploads, also paginated.

**Playback**
Media3 ExoPlayer as the playback engine. Speed ranges from 0.5x to 2x via `PlaybackParameters`. Gesture seeking is handled through touch events on `PlayerView`. Fullscreen switching uses `Configuration` change listeners together with `WindowInsetsController`. Background audio runs through `MediaSessionService`, controllable from the notification.

**Download**
Two quality options: 720P and 360P. Download tasks run on coroutines over the IO dispatcher, issuing OkHttp requests and writing to the app's private directory. Progress is exposed to the UI via Flow. A foreground service keeps downloads alive when the app is backgrounded.

**Favorites**
Persisted with DataStore Preferences, serialized as JSON. The logic lives in `FavoriteStore.kt`. A video belongs to exactly one folder — favoriting removes it from its previous folder before inserting into the target one. Imports deduplicate automatically, with conflicts resolved by video ID.

## Building

JDK 17, any recent stable Android Studio. Min SDK 26, target 35, compile 36.

    git clone https://github.com/grillpoet/bily.git
    cd bily
    ./gradlew assembleDebug

Output is at `app/build/outputs/apk/debug/app-debug.apk`. Release builds enable R8 and resource shrinking and require a signing config.

## Permissions

INTERNET for network requests. On Android 10 and above, writing to the app's private directory needs no storage permission; exporting to the gallery does require a media permission. Background downloads use FOREGROUND_SERVICE.

## Downloads and favorites

Downloaded videos live in the app's private directory and can be viewed from the in-app "My Downloads" page. Favorites are stored locally via DataStore as JSON and are not synced to any cloud. Export regularly if you want a backup — useful when switching devices or reinstalling.

## FAQ

If search or playback fails, check your network first. Bilibili API changes can break individual features; wait for an update. Only 720P and 360P are available because the API returns limited formats when not logged in, and the priority is keeping the core features stable.

## Contributing

Issues and PRs are welcome. For bug reports, include reproduction steps, device model, and Android version.

## License

MIT
