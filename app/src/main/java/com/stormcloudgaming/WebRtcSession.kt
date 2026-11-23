package com.stormcloudgaming

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import org.webrtc.DataChannel
import org.webrtc.DefaultVideoDecoderFactory
import org.webrtc.DefaultVideoEncoderFactory
import org.webrtc.EglBase
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.SurfaceViewRenderer
import org.webrtc.VideoTrack
import java.util.concurrent.TimeUnit

class WebRtcSession(
    context: Context,
    private val onStatus: (String) -> Unit
) {
    private val egl = EglBase.create()
    private val factory: PeerConnectionFactory
    private val http = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()
    private var socket: WebSocket? = null
    private var peer: PeerConnection? = null
    private var inputChannel: DataChannel? = null
    private var renderer: SurfaceViewRenderer? = null
    private var remoteVideo: VideoTrack? = null
    private var offerReady = false
    private var answerSent = false

    init {
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(context.applicationContext).createInitializationOptions()
        )
        val encoder = DefaultVideoEncoderFactory(egl.eglBaseContext, true, true)
        val decoder = DefaultVideoDecoderFactory(egl.eglBaseContext)
        factory = PeerConnectionFactory.builder()
            .setVideoEncoderFactory(encoder)
            .setVideoDecoderFactory(decoder)
            .createPeerConnectionFactory()
    }

    fun attachRenderer(view: SurfaceViewRenderer) {
        renderer?.let { old -> remoteVideo?.removeSink(old); old.release() }
        renderer = view
        view.init(egl.eglBaseContext, null)
        view.setEnableHardwareScaler(true)
        view.setMirror(false)
        remoteVideo?.addSink(view)
    }

    fun connect(address: String, token: String) {
        if (address.isBlank() || token.length < 24) {
            onStatus("Masukkan alamat host dan token minimal 24 karakter.")
            return
        }
        val normalized = if (address.startsWith("ws://") || address.startsWith("wss://")) address else "ws://$address"
        val url = normalized.trimEnd('/') + if (normalized.endsWith("/signal")) "" else "/signal"
        onStatus("Menghubungkan ke host…")
        val request = Request.Builder().url(url).header("Authorization", "Bearer $token").build()
        socket = http.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                onStatus("Signaling tersambung. Menyiapkan WebRTC…")
                createPeer()
            }
            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val message = JSONObject(text)
                    when (message.optString("type")) {
                        "answer" -> {
                            val desc = SessionDescription(SessionDescription.Type.ANSWER, message.getString("sdp"))
                            peer?.setRemoteDescription(object : SdpObserver {
                                override fun onSetSuccess() { onStatus("Jawaban host diterima. Menunggu video…") }
                                override fun onSetFailure(error: String) { onStatus("Gagal memasang jawaban: $error") }
                                override fun onCreateSuccess(sdp: SessionDescription?) {}
                                override fun onCreateFailure(error: String?) {}
                            }, desc)
                        }
                        "error" -> onStatus(message.optString("message", "Kesalahan host"))
                    }
                } catch (e: Exception) { onStatus("Pesan signaling tidak valid: ${e.message}") }
            }
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                onStatus("Koneksi gagal: ${t.message ?: "periksa alamat, token, dan firewall"}")
            }
            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                onStatus("Koneksi signaling ditutup: $reason")
            }
        })
    }

    private fun createPeer() {
        val config = PeerConnection.RTCConfiguration(listOf(
            PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
        )).apply {
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
            continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
        }
        peer = factory.createPeerConnection(config, object : PeerConnection.Observer {
            override fun onSignalingChange(state: PeerConnection.SignalingState) {}
            override fun onIceConnectionChange(state: PeerConnection.IceConnectionState) {
                onStatus("WebRTC: $state")
            }
            override fun onIceConnectionReceivingChange(receiving: Boolean) {}
            override fun onIceGatheringChange(state: PeerConnection.IceGatheringState) {
                if (state == PeerConnection.IceGatheringState.COMPLETE) sendOffer()
            }
            override fun onIceCandidate(candidate: IceCandidate) {}
            override fun onIceCandidatesRemoved(candidates: Array<IceCandidate>) {}
            override fun onAddStream(stream: org.webrtc.MediaStream) {}
            override fun onRemoveStream(stream: org.webrtc.MediaStream) {}
            override fun onDataChannel(channel: DataChannel) { inputChannel = channel }
            override fun onRenegotiationNeeded() {}
            override fun onAddTrack(receiver: org.webrtc.RtpReceiver, streams: Array<out org.webrtc.MediaStream>) {
                val track = receiver.track()
                if (track is VideoTrack) {
                    remoteVideo = track
                    renderer?.let { track.addSink(it) }
                    onStatus("Video host diterima.")
                }
            }
        })
        inputChannel = peer?.createDataChannel("game-input", DataChannel.Init())
        peer?.createOffer(object : SdpObserver {
            override fun onCreateSuccess(sdp: SessionDescription) {
                peer?.setLocalDescription(object : SdpObserver {
                    override fun onSetSuccess() { offerReady = true; sendOffer() }
                    override fun onSetFailure(error: String) { onStatus("Gagal menyiapkan offer: $error") }
                    override fun onCreateSuccess(sdp: SessionDescription?) {}
                    override fun onCreateFailure(error: String?) {}
                }, sdp)
            }
            override fun onCreateFailure(error: String) { onStatus("Gagal membuat offer: $error") }
            override fun onSetSuccess() {}
            override fun onSetFailure(error: String) {}
        }, MediaConstraints())
    }

    private fun sendOffer() {
        if (!offerReady || answerSent || peer?.iceGatheringState() != PeerConnection.IceGatheringState.COMPLETE) return
        val sdp = peer?.localDescription?.description ?: return
        answerSent = true
        socket?.send(JSONObject().put("type", "offer").put("sdp", sdp).toString())
        onStatus("Offer terkirim. Menunggu jawaban host…")
    }

    fun sendKey(key: String, down: Boolean) {
        val message = JSONObject().put("action", if (down) "down" else "up").put("key", key)
        inputChannel?.send(DataChannel.Buffer(java.nio.ByteBuffer.wrap(message.toString().toByteArray()), false))
    }

    fun close() {
        try { inputChannel?.close() } catch (_: Exception) {}
        try { socket?.close(1000, "client closed") } catch (_: Exception) {}
        try { peer?.close() } catch (_: Exception) {}
        remoteVideo?.let { track -> renderer?.let { track.removeSink(it) } }
        renderer?.release()
        renderer = null
        remoteVideo = null
        peer = null
        inputChannel = null
        factory.dispose()
        egl.release()
        http.dispatcher.executorService.shutdown()
    }
}
