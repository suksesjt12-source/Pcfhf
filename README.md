# Storm Cloud Gaming

Android WebRTC game-streaming client plus a Python desktop host prototype.

## What is implemented

- Android Kotlin/Jetpack Compose client with a WebRTC peer connection.
- WebSocket signaling to a host using a bearer token.
- Remote video rendering through the WebRTC video track.
- WebRTC data channel for basic keyboard input events.
- Python host prototype that captures the desktop and publishes video.
- Host-side token check and a health endpoint.
- GitHub Actions workflow to build a debug APK.

## Current limitations

- This is an early LAN prototype, not a production cloud-gaming service.
- Host audio capture and Android audio playback are not implemented yet.
- The Android on-screen controls currently send only a small keyboard mapping; a full configurable gamepad/touch overlay is still required.
- The host uses a STUN server for initial connectivity. Internet play usually requires TURN, TLS/WSS, firewall configuration, and a secured deployment.
- No public GPU server directory or free GPU capacity is included. The PC/server must be owned or legitimately rented by the user and must have the game installed.
- The current Android UI uses a development WebSocket connection. Do not expose the host directly to the public internet.

## Start the host on a PC

See [host/README.md](host/README.md). In short:

```bash
cd host
python -m venv .venv
# Windows: .venv\\Scripts\\activate
# Linux: source .venv/bin/activate
pip install -r requirements.txt
```

Set a random secret of at least 24 characters in `STORM_HOST_TOKEN`, then run `python host.py`. Keep the PC and Android phone on the same Wi-Fi/LAN for the first test. Enter the PC's LAN IP and port (default `8765`) plus the same token in the Android app.

## Build the Android APK

1. Open the repository's **Actions** tab.
2. Select **Android APK Build**.
3. Wait for a successful run.
4. Download the `storm-cloud-gaming-debug` artifact and extract `app-debug.apk`.

The build runs automatically after pushes to `main`.

## Build versions

- Android: Kotlin, Jetpack Compose, WebRTC, OkHttp
- Android minimum: API 26
- JDK 17, Gradle 8.9, Android Gradle Plugin 8.7.3
- Python host: Python 3.10–3.12
