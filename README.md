# Storm Cloud Gaming

Android starter project for a cloud-streaming client concept.

## Implemented in this starter

- Native Android application built with Kotlin and Jetpack Compose.
- Landscape dashboard with a Naruto-inspired orange/black theme.
- Selectable catalog entries for Naruto Storm titles.
- Host-address field and explicit connection-state messaging.
- Streaming-quality preference selector (adaptive, 720p, 1080p).
- Touch-controller layout preview with directional pad and gameplay buttons.
- GitHub Actions workflow that builds and uploads a debug APK artifact.

## Not implemented yet

- Actual video/audio streaming or game-input transport.
- Automatic discovery of verified public GPU hosts.
- A cloud GPU server or game licenses.
- Functional touch input forwarding to a running game.

The controller screen is currently a visual preview only. A real gaming session requires a legitimate compatible host running a game and a streaming client/protocol integrated into this Android app. The app does not fabricate server listings or promise unlimited free GPU access.

## Build an APK

1. Open the repository's **Actions** tab.
2. Select **Android APK Build**.
3. Wait for the workflow to finish successfully.
4. Open the run and download the `storm-cloud-gaming-debug` artifact.
5. Extract the ZIP to obtain `app-debug.apk`.

The workflow also runs automatically when changes are pushed to `main`.

## Requirements

- Android 8.0 (API 26) or newer
- JDK 17
- Gradle 8.9
- Android Gradle Plugin 8.7.3
- Android SDK Platform 35
