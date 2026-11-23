# Storm Cloud Gaming

Android starter project for a cloud-streaming client concept.

## Current status

- Native Android app shell built with Kotlin and Jetpack Compose.
- Naruto-inspired landscape dashboard and game selection UI.
- Server address field and honest connection-state messaging.
- GitHub Actions workflow to build and upload a debug APK.

## Important limitations

This repository does not yet contain a game-streaming engine or a public GPU server directory. A working session requires a legitimate compatible host that is already running the selected game and a streaming protocol client integrated into this app. The app does not claim that free GPU servers are available, and it does not fabricate server listings.

## Build

Push to `main` or run the **Android APK Build** workflow manually from the Actions tab. Download the `storm-cloud-gaming-debug` artifact from a successful run.

## Android requirements

- Android 8.0 (API 26) or newer
- JDK 17
- Gradle 8.9
- Android Gradle Plugin 8.7.3
