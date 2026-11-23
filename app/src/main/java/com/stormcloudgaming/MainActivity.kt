package com.stormcloudgaming

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import org.webrtc.SurfaceViewRenderer

private val Ink = Color(0xFF101014)
private val Panel = Color(0xFF202027)
private val Orange = Color(0xFFFF7A1A)
private val Muted = Color(0xFFB8B8C2)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContent { StormApp() }
    }
}

@Composable
fun StormApp() {
    val context = LocalContext.current
    var host by remember { mutableStateOf("192.168.1.2:8765") }
    var token by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Belum terhubung") }
    var quality by remember { mutableStateOf("720p") }
    var session by remember { mutableStateOf<WebRtcSession?>(null) }
    var showPlayer by remember { mutableStateOf(false) }
    val renderer = remember { SurfaceViewRenderer(context) }

    DisposableEffect(session) {
        session?.attachRenderer(renderer)
        onDispose { session?.close() }
    }

    MaterialTheme(colorScheme = darkColorScheme(background = Ink, surface = Panel, primary = Orange)) {
        Surface(Modifier.fillMaxSize(), color = Ink) {
            if (showPlayer) {
                Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("STORM STREAM", color = Orange, fontWeight = FontWeight.Black, fontSize = 20.sp, modifier = Modifier.weight(1f))
                        TextButton(onClick = { session = null; showPlayer = false; status = "Terputus" }) { Text("PUTUS") }
                    }
                    AndroidView(factory = { renderer }, modifier = Modifier.weight(1f).fillMaxWidth().background(Color.Black))
                    Text(status, color = Muted, fontSize = 11.sp)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        StreamKey("W") { session?.sendKey("w", true); session?.sendKey("w", false) }
                        StreamKey("A") { session?.sendKey("a", true); session?.sendKey("a", false) }
                        StreamKey("S") { session?.sendKey("s", true); session?.sendKey("s", false) }
                        StreamKey("D") { session?.sendKey("d", true); session?.sendKey("d", false) }
                        StreamKey("JUMP") { session?.sendKey("space", true); session?.sendKey("space", false) }
                        StreamKey("ATTACK") { session?.sendKey("x", true); session?.sendKey("x", false) }
                    }
                }
            } else {
                Row(Modifier.fillMaxSize().padding(22.dp), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("STORM", color = Orange, fontSize = 36.sp, fontWeight = FontWeight.Black)
                        Text("CLOUD GAMING", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Streaming desktop melalui WebRTC", color = Muted, fontSize = 13.sp)
                        Text("Kualitas target", color = Orange, fontWeight = FontWeight.Bold)
                        listOf("Adaptif", "720p", "1080p").forEach { item ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = quality == item, onClick = { quality = item })
                                Text(item, color = Color.White)
                            }
                        }
                        Spacer(Modifier.weight(1f))
                        Text("Catatan: game harus berjalan di PC host. APK tidak menyediakan GPU cloud gratis.", color = Muted, fontSize = 11.sp)
                    }
                    Column(Modifier.weight(1.1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("KONEKSI HOST", color = Orange, fontWeight = FontWeight.Bold)
                        OutlinedTextField(value = host, onValueChange = { host = it }, label = { Text("IP host:port atau ws://host:port") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = token, onValueChange = { token = it }, label = { Text("Token host (min. 24 karakter)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        Button(onClick = {
                            val next = WebRtcSession(context) { status = it }
                            session = next
                            next.connect(host.trim(), token)
                            showPlayer = true
                        }, modifier = Modifier.fillMaxWidth()) { Text("HUBUNGKAN & MULAI STREAM") }
                        Text("Status: $status", color = Muted, fontSize = 12.sp)
                        Text("Host Python tersedia di folder host/. Untuk percobaan awal, HP dan PC harus berada di jaringan LAN yang sama.", color = Muted, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun StreamKey(label: String, onClick: () -> Unit) {
    Button(onClick = onClick, shape = RoundedCornerShape(12.dp), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)) {
        Text(label, fontWeight = FontWeight.Bold, fontSize = 12.sp)
    }
}
