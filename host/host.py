import asyncio
import json
import os
import secrets
import signal
from typing import Optional

import mss
import numpy as np
from aiohttp import web
from aiortc import RTCPeerConnection, RTCSessionDescription, VideoStreamTrack
from av import VideoFrame
from pynput.keyboard import Controller as KeyboardController, Key
from pynput.mouse import Button, Controller as MouseController

TOKEN = os.environ.get("STORM_HOST_TOKEN", "")
HOST = os.environ.get("STORM_BIND", "0.0.0.0")
PORT = int(os.environ.get("STORM_PORT", "8765"))
MAX_FPS = max(5, min(60, int(os.environ.get("STORM_FPS", "30"))))
WIDTH = max(640, min(1920, int(os.environ.get("STORM_WIDTH", "1280"))))
HEIGHT = max(360, min(1080, int(os.environ.get("STORM_HEIGHT", "720"))))

pcs: set[RTCPeerConnection] = set()
keyboard = KeyboardController()
mouse = MouseController()
KEY_MAP = {
    "up": Key.up, "down": Key.down, "left": Key.left, "right": Key.right,
    "enter": Key.enter, "escape": Key.esc, "space": Key.space,
    "shift": Key.shift, "ctrl": Key.ctrl, "alt": Key.alt,
    "z": "z", "x": "x", "c": "c", "a": "a", "s": "s", "d": "d",
    "w": "w", "q": "q", "e": "e", "r": "r", "f": "f",
}

class DesktopVideoTrack(VideoStreamTrack):
    def __init__(self):
        super().__init__()
        self.sct = mss.mss()
        self.monitor = self.sct.monitors[1]

    async def recv(self):
        pts, time_base = await self.next_timestamp()
        shot = self.sct.grab(self.monitor)
        frame = np.asarray(shot)[:, :, :3]
        # Scale to a predictable stream size; keep aspect ratio of the source.
        import cv2
        frame = cv2.resize(frame, (WIDTH, HEIGHT), interpolation=cv2.INTER_AREA)
        video = VideoFrame.from_ndarray(frame, format="bgr24")
        video.pts = pts
        video.time_base = time_base
        return video

    def stop(self):
        try:
            self.sct.close()
        finally:
            super().stop()

def authorized(request: web.Request) -> bool:
    if not TOKEN:
        return False
    return request.headers.get("Authorization", "") == f"Bearer {TOKEN}"

async def health(request):
    return web.json_response({
        "service": "Storm Cloud Gaming Host",
        "status": "ready",
        "video": f"{WIDTH}x{HEIGHT}@{MAX_FPS}",
        "authentication": "enabled" if TOKEN else "not-configured",
        "note": "Use HTTPS/WSS and a strong token before exposing this host to the internet."
    })

async def signal_ws(request):
    if not authorized(request):
        raise web.HTTPUnauthorized(text="Missing or invalid host token")
    ws = web.WebSocketResponse(heartbeat=20, max_msg_size=1024 * 1024)
    await ws.prepare(request)
    pc: Optional[RTCPeerConnection] = None
    try:
        async for msg in ws:
            if msg.type != web.WSMsgType.TEXT:
                continue
            payload = json.loads(msg.data)
            if payload.get("type") != "offer" or not isinstance(payload.get("sdp"), str):
                await ws.send_json({"type": "error", "message": "Expected WebRTC offer"})
                continue
            pc = RTCPeerConnection()
            pcs.add(pc)
            pc.addTrack(DesktopVideoTrack())

            @pc.on("datachannel")
            def on_datachannel(channel):
                @channel.on("message")
                def on_message(message):
                    if not isinstance(message, str) or len(message) > 256:
                        return
                    try:
                        event = json.loads(message)
                        action = event.get("action")
                        key_name = event.get("key")
                        if action in ("down", "up") and key_name in KEY_MAP:
                            key = KEY_MAP[key_name]
                            (keyboard.press if action == "down" else keyboard.release)(key)
                        elif action == "mouse_move":
                            x = max(0, min(1, float(event.get("x", 0.5))))
                            y = max(0, min(1, float(event.get("y", 0.5))))
                            mouse.position = (int(x * WIDTH), int(y * HEIGHT))
                        elif action == "mouse_button" and event.get("button") in ("left", "right"):
                            button = Button.left if event["button"] == "left" else Button.right
                            (mouse.press if event.get("down") else mouse.release)(button)
                    except (ValueError, TypeError, KeyError):
                        return

            @pc.on("connectionstatechange")
            async def on_connectionstatechange():
                if pc.connectionState in ("failed", "closed", "disconnected"):
                    await pc.close()
                    pcs.discard(pc)

            await pc.setRemoteDescription(RTCSessionDescription(sdp=payload["sdp"], type="offer"))
            answer = await pc.createAnswer()
            await pc.setLocalDescription(answer)
            # Wait for ICE gathering so this simple first version can return one complete SDP.
            for _ in range(200):
                if pc.iceGatheringState == "complete":
                    break
                await asyncio.sleep(0.05)
            await ws.send_json({"type": "answer", "sdp": pc.localDescription.sdp})
    except (json.JSONDecodeError, ConnectionError):
        pass
    finally:
        if pc:
            await pc.close()
            pcs.discard(pc)
        await ws.close()
    return ws

async def on_shutdown(app):
    await asyncio.gather(*(pc.close() for pc in list(pcs)), return_exceptions=True)
    pcs.clear()

def main():
    if not TOKEN or len(TOKEN) < 24:
        raise SystemExit("Set STORM_HOST_TOKEN to a random secret of at least 24 characters.")
    app = web.Application()
    app.router.add_get("/health", health)
    app.router.add_get("/signal", signal_ws)
    app.on_shutdown.append(on_shutdown)
    web.run_app(app, host=HOST, port=PORT)

if __name__ == "__main__":
    main()
