[English](README.md) | [简体中文](README.zh-CN.md)

# Bily

A third-party Bilibili client for Android, Kotlin + Jetpack Compose. Only does search, playback, download, and local favorites — no ads, no recommendations, no social. Data comes from Bilibili's public APIs.

## Features

**Search and browsing**
Keyword search for videos, plus creator (UP) pages with paginated uploads. Search goes through the WBI-signed web API; creator pages use the APP-signed cursor API.

**Playback**
Media3 ExoPlayer as the engine, with a custom player UI. Gesture seeking, double-tap play/pause, 0.5x–2x speed. Fullscreen follows the video's actual aspect ratio and rotates between portrait and landscape; portrait videos take a separate fullscreen path. Multi-part videos are supported, and playback auto-advances to the next part.

**Download**
720P and 360P. Queued through Android's DownloadManager, saved to the public Downloads directory. Filenames include the part number and BV ID to avoid collisions.

**Favorites**
Stored in DataStore Preferences as JSON, organized into folders. A video belongs to exactly one folder — favoriting removes it from the previous one first. Import supports merge and replace modes, with global bvid deduplication.

**Misc**
Dark mode, Coil cover caching (RGB_565 halves memory), infinite grid that loads as you approach the bottom.

## Networking

Bilibili's APIs have a few layers of anti-scraping. All handled here:

**WBI signing**
Pull img_key and sub_key from the `nav` endpoint, derive a mixin_key through a fixed 64-slot permutation table, sort params, concatenate, and MD5 to get w_rid. Used by search and playurl.

**APP signing**
Hardcoded appkey/appsec, MD5 over sorted params. Used for APP-only endpoints like creator uploads.

**Buvid fingerprint**
Hit the `spi` endpoint to get buvid3/buvid4, write them into cookies, and pair them with a generated _uuid and b_lsid so requests look like a real browser.

**Risk control**
Detects codes -352/-402/-412/-799, and the case where `data` contains only `v_voucher`, and throws a dedicated RateLimited exception. The UI shows a "too many requests" message.

**Throttling and retries**
Each API category has its own throttle (playurl 200ms, search 500ms, creator 500ms) so they don't block each other. RetryInterceptor does exponential backoff on 412/429, up to two retries, with jitter.

**TLS fingerprint**
OkHttp is configured with Chrome's cipher suites and TLS 1.3/1.2 to slip past TLS fingerprinting.

## Architecture

MVVM with manual DI. `AppContainer` is initialized in Application and holds OkHttp, cookie jar, APIs, repositories, and Downloader. ViewModels get their dependencies through `AppViewModelFactory`. Navigation is a hand-written `Navigator` backed by a `mutableStateListOf<Screen>` stack, capped at 20 entries, with adjacent duplicates ignored.

    app/src/main/java/com/grillpoet/bily/
    ├── core/       Constants and extensions
    ├── core/net/   CookieJar, interceptors, Call.await
    ├── data/       Repositories, Downloader, FavoriteStore, models
    ├── data/api/   API layer, DTOs, WBI/APP signing, Buvid
    ├── di/         AppContainer
    ├── nav/        Navigator and Screen
    ├── ui/         AppRoot, theme, icons
    ├── ui/screen/  Main, search, creator, video player
    ├── ui/vm/      ViewModels
    └── ui/widget/  Player, cards, dialogs, infinite scroll

## Building

JDK 17, any recent stable Android Studio. minSdk 26, targetSdk 35, compileSdk 36.

    git clone https://github.com/grillpoet/bily.git
    cd bily
    ./gradlew assembleDebug

APK at `app/build/outputs/apk/debug/app-debug.apk`. Release builds enable R8 and resource shrinking and need a signing config.

## Permissions

INTERNET for all network calls. POST_NOTIFICATIONS on Android 13+ for DownloadManager completion notifications.

## FAQ

If search or playback fails, check your network first. Bilibili API changes can break individual features; wait for an update. Only 720P and 360P are available because the API returns limited formats when not logged in. Favorites are local-only and not synced — export regularly if you want a backup.

## License

MIT
