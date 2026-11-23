package com.stormcloudgaming

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Ink = Color(0xFF101014)
private val Panel = Color(0xFF202027)
private val Orange = Color(0xFFFF7A1A)
private val Muted = Color(0xFFB8B8C2)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { StormApp() }
    }
}

@Composable
fun StormApp() {
    var serverAddress by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("Belum terhubung") }
    var selected by remember { mutableStateOf("Naruto Storm 4") }
    var showControls by remember { mutableStateOf(false) }
    var quality by remember { mutableStateOf("Adaptif") }

    MaterialTheme(colorScheme = darkColorScheme(
        background = Ink, surface = Panel, primary = Orange,
        onBackground = Color.White, onSurface = Color.White
    )) {
        Surface(Modifier.fillMaxSize(), color = Ink) {
            if (showControls) {
                ControllerPreview(onBack = { showControls = false })
            } else {
                Row(Modifier.fillMaxSize().padding(22.dp), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    Column(Modifier.weight(1.15f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("STORM", color = Orange, fontSize = 34.sp, fontWeight = FontWeight.Black)
                        Text("CLOUD GAMING", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        Text("Mainkan game melalui server streaming yang kompatibel.", color = Muted, fontSize = 13.sp)
                        Text("PILIH GAME", color = Orange, fontWeight = FontWeight.Bold)
                        listOf("Naruto Storm 1", "Naruto Storm 2", "Naruto Storm 3 Full Burst", "Naruto Storm 4", "Road to Boruto").forEach { game ->
                            Card(
                                onClick = { selected = game },
                                colors = CardDefaults.cardColors(containerColor = if (selected == game) Orange.copy(alpha = .22f) else Panel),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(Modifier.fillMaxWidth().padding(11.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(game, Modifier.weight(1f), fontWeight = if (selected == game) FontWeight.Bold else FontWeight.Normal)
                                    if (selected == game) Text("DIPILIH", color = Orange, fontSize = 10.sp)
                                }
                            }
                        }
                        Spacer(Modifier.weight(1f))
                        OutlinedButton(onClick = { showControls = true }, modifier = Modifier.fillMaxWidth()) {
                            Text("PRATINJAU KONTROL TOUCH")
                        }
                    }
                    Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(18.dp)) {
                            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                                Text("SERVER CLOUD", color = Orange, fontWeight = FontWeight.Bold)
                                Text("Pencarian server gratis", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                Text("Belum ada direktori server GPU publik yang terverifikasi. Aplikasi tidak akan menampilkan server palsu atau menjanjikan GPU gratis tanpa batas.", color = Muted, fontSize = 12.sp)
                                OutlinedTextField(
                                    value = serverAddress,
                                    onValueChange = { serverAddress = it },
                                    label = { Text("Alamat host streaming") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Button(
                                    onClick = { status = if (serverAddress.isBlank()) "Masukkan alamat host terlebih dahulu." else "Alamat dicatat. Uji koneksi streaming belum tersedia pada versi ini." },
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text("PERIKSA KONEKSI") }
                                Text("Status: $status", color = Muted, fontSize = 11.sp)
                            }
                        }
                        Card(Modifier.fillMaxWidth().weight(1f), colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(18.dp)) {
                            Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("PENGATURAN STREAMING", color = Orange, fontWeight = FontWeight.Bold)
                                Text("Kualitas gambar", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                listOf("Adaptif", "720p", "1080p").forEach { option ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(selected = quality == option, onClick = { quality = option })
                                        Text(option, color = Color.White)
                                    }
                                }
                                Text("Mode landscape • Audio host diperlukan • FPS mengikuti kemampuan server", color = Muted, fontSize = 11.sp)
                                Spacer(Modifier.weight(1f))
                                Text("Status pengembangan: UI dan konfigurasi awal. Mesin streaming, input game, dan host GPU belum terintegrasi.", color = Muted, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ControllerPreview(onBack: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("TOUCH CONTROLLER", color = Orange, fontSize = 22.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
            TextButton(onClick = onBack) { Text("KEMBALI") }
        }
        Text("Pratinjau tata letak. Tombol belum mengirim input ke game karena streaming host belum terpasang.", color = Muted, fontSize = 12.sp)
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(176.dp).background(Panel, CircleShape), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    ControlKey("▲")
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ControlKey("◀")
                        Spacer(Modifier.width(24.dp))
                        ControlKey("▶")
                    }
                    ControlKey("▼")
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ControlKey("GUARD")
                    ControlKey("JUMP")
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ControlKey("CHAKRA")
                    ControlKey("ATTACK")
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ControlKey("SUPPORT")
                    ControlKey("DASH")
                }
            }
        }
    }
}

@Composable
private fun ControlKey(label: String) {
    Surface(
        modifier = Modifier.size(width = if (label.length > 2) 92.dp else 54.dp, height = 54.dp),
        shape = RoundedCornerShape(16.dp),
        color = Orange.copy(alpha = .88f),
        contentColor = Color.Black
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, fontWeight = FontWeight.Black, fontSize = if (label.length > 2) 10.sp else 17.sp)
        }
    }
}
