# Storm Cloud Gaming — Host prototype

This folder contains the first real host-side streaming service. It captures the host desktop and sends video over WebRTC to an authenticated client. A WebRTC data channel accepts a small allow-list of keyboard and mouse events.

## Requirements

- A Windows or Linux desktop with a graphical session
- Python 3.10–3.12
- A virtual display/session on Linux if running headless
- A firewall rule allowing the signaling port and WebRTC UDP traffic

## Run on the host

```bash
python -m venv .venv
# Windows: .venv\\Scripts\\activate
# Linux/macOS: source .venv/bin/activate
pip install -r requirements.txt
```

Set a long random token (at least 24 characters), then start:

```bash
# Linux/macOS
export STORM_HOST_TOKEN="replace-with-a-long-random-secret"
python host.py

# PowerShell
$env:STORM_HOST_TOKEN="replace-with-a-long-random-secret"
python host.py
```

The host listens on port 8765 by default. `GET /health` reports readiness. The signaling WebSocket is `/signal` and requires an HTTP `Authorization: Bearer <token>` header.

Optional environment variables: `STORM_BIND`, `STORM_PORT`, `STORM_FPS`, `STORM_WIDTH`, `STORM_HEIGHT`.

## Important limitations of this prototype

- Video only; host audio capture/playback is not wired up yet.
- The current signaling flow waits for ICE gathering and is intended for a first LAN prototype. Reliable internet use needs TURN, TLS/WSS, and proper network configuration.
- Do not expose this service directly to the public internet. Put it behind a VPN or a properly configured TLS reverse proxy and use a unique strong token.
- Keyboard/mouse input is a generic prototype. A game-specific virtual gamepad mapping still needs to be added.
- This service streams the host desktop; it does not provide a cloud GPU. The host must have a game installed and be powerful enough to run it.
