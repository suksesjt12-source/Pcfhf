package com.stormcloudgaming

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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

    MaterialTheme(colorScheme = darkColorScheme(
        background = Ink, surface = Panel, primary = Orange,
        onBackground = Color.White, onSurface = Color.White
    )) {
        Surface(Modifier.fillMaxSize(), color = Ink) {
            Row(Modifier.fillMaxSize().padding(22.dp), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                Column(Modifier.weight(1.15f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
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
                            Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(game, Modifier.weight(1f), fontWeight = if (selected == game) FontWeight.Bold else FontWeight.Normal)
                                if (selected == game) Text("DIPILIH", color = Orange, fontSize = 10.sp)
                            }
                        }
                    }
                }
                Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(18.dp)) {
                        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("SERVER CLOUD", color = Orange, fontWeight = FontWeight.Bold)
                            Text("Pencarian server gratis", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text("Belum ada direktori server publik terverifikasi yang terhubung. Aplikasi tidak akan membuat server palsu atau menjanjikan GPU gratis tanpa batas.", color = Muted, fontSize = 12.sp)
                            OutlinedTextField(
                                value = serverAddress,
                                onValueChange = { serverAddress = it },
                                label = { Text("Alamat server (opsional)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Button(
                                onClick = { status = if (serverAddress.isBlank()) "Belum ada server. Masukkan alamat server yang kompatibel." else "Alamat tersimpan untuk konfigurasi; protokol streaming belum diintegrasikan." },
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("PERIKSA KONEKSI") }
                            Text("Status: $status", color = Muted, fontSize = 12.sp)
                        }
                    }
                    Card(Modifier.fillMaxWidth().weight(1f), colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(18.dp)) {
                        Column(Modifier.fillMaxSize().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("PENGATURAN STREAMING", color = Orange, fontWeight = FontWeight.Bold)
                            Text("Kualitas adaptif", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text("Target kualitas akan menyesuaikan kemampuan server dan jaringan setelah protokol streaming ditambahkan.", color = Muted, fontSize = 12.sp)
                            Text("• Resolusi: otomatis", color = Color.White)
                            Text("• FPS: mengikuti host", color = Color.White)
                            Text("• Mode layar: landscape", color = Color.White)
                            Spacer(Modifier.weight(1f))
                            Text("Tahap awal: antarmuka dan fondasi Android. Streaming game nyata memerlukan host GPU dan integrasi protokol yang kompatibel.", color = Muted, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}
