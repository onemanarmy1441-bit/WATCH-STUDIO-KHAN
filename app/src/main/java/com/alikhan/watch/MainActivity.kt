package com.alikhan.watch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.sin

private val Bg = Color(0xFF090909)
private val Panel = Color(0xFF171717)
private val Gold = Color(0xFFD6B36A)
private val Muted = Color(0xFF9B9B9B)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AliKhanWatchApp() }
    }
}

@Composable
fun AliKhanWatchApp() {
    var tab by remember { mutableStateOf(0) }
    var selected by remember { mutableStateOf("Luxury") }
    val categories = listOf("All", "Luxury", "Gaming", "Anime", "Sports", "Minimal", "Futuristic")
    MaterialTheme(colorScheme = darkColorScheme(
        primary = Gold, background = Bg, surface = Panel,
        onPrimary = Bg, onBackground = Color.White, onSurface = Color.White
    )) {
        Scaffold(
            containerColor = Bg,
            bottomBar = {
                NavigationBar(containerColor = Panel, contentColor = Gold) {
                    NavigationBarItem(selected = tab == 0, onClick = { tab = 0 },
                        icon = { Icon(Icons.Default.Home, null) }, label = { Text("Home") })
                    NavigationBarItem(selected = tab == 1, onClick = { tab = 1 },
                        icon = { Icon(Icons.Default.Edit, null) }, label = { Text("Studio") })
                    NavigationBarItem(selected = tab == 2, onClick = { tab = 2 },
                        icon = { Icon(Icons.Default.Collections, null) }, label = { Text("Library") })
                    NavigationBarItem(selected = tab == 3, onClick = { tab = 3 },
                        icon = { Icon(Icons.Default.Settings, null) }, label = { Text("Settings") })
                }
            }
        ) { padding ->
            Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 18.dp)) {
                Spacer(Modifier.height(18.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("ALI KHAN", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 3.sp)
                        Text("WATCH", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                    }
                    Surface(color = Panel, shape = CircleShape) {
                        Icon(Icons.Default.Watch, null, tint = Gold, modifier = Modifier.padding(12.dp).size(24.dp))
                    }
                }
                Spacer(Modifier.height(22.dp))
                when (tab) {
                    0 -> HomeScreen(categories, selected) { selected = it }
                    1 -> StudioScreen()
                    2 -> LibraryScreen()
                    else -> SettingsScreen()
                }
            }
        }
    }
}

@Composable
fun HomeScreen(categories: List<String>, selected: String, onSelect: (String) -> Unit) {
    Text("Your next look starts here.", color = Muted, fontSize = 14.sp)
    Spacer(Modifier.height(18.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Featured face", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.weight(1f))
        Text("PREVIEW", color = Gold, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
    }
    Spacer(Modifier.height(12.dp))
    Box(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(24.dp)).padding(18.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            WatchPreview()
            Spacer(Modifier.height(12.dp))
            Text("GOLDEN CHRONO", color = Gold, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Text("Analog + digital concept", color = Muted, fontSize = 12.sp)
        }
    }
    Spacer(Modifier.height(22.dp))
    Text("Explore categories", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(12.dp))
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(categories) { item ->
            FilterChip(selected = selected == item, onClick = { onSelect(item) }, label = { Text(item) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Gold, selectedLabelColor = Bg))
        }
    }
    Spacer(Modifier.height(16.dp))
    Text("Watch faces are previews at this stage. Device installation will be added after the watch protocol is verified.",
        color = Muted, fontSize = 12.sp, lineHeight = 17.sp)
}

@Composable
fun WatchPreview() {
    Canvas(Modifier.size(218.dp)) {
        val c = Offset(size.width / 2, size.height / 2)
        val r = size.minDimension * .45f
        drawCircle(Color(0xFF252525), r)
        drawCircle(Gold, r, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 5f))
        drawCircle(Color(0xFF101010), r * .91f)
        for (i in 0 until 60) {
            val a = Math.PI * i / 30.0
            val inner = r * if (i % 5 == 0) .78f else .86f
            val outer = r * .91f
            drawLine(if (i % 5 == 0) Gold else Color(0xFF777777),
                Offset(c.x + cos(a).toFloat() * inner, c.y + sin(a).toFloat() * inner),
                Offset(c.x + cos(a).toFloat() * outer, c.y + sin(a).toFloat() * outer),
                strokeWidth = if (i % 5 == 0) 3f else 1f, cap = StrokeCap.Round)
        }
        drawLine(Color.White, c, Offset(c.x - 35f, c.y - 42f), 7f, StrokeCap.Round)
        drawLine(Gold, c, Offset(c.x + 62f, c.y - 25f), 5f, StrokeCap.Round)
        drawLine(Color(0xFFE05A4F), c, Offset(c.x + 8f, c.y - 78f), 2f, StrokeCap.Round)
        drawCircle(Gold, 6f, c)
    }
}

@Composable
fun StudioScreen() {
    Text("WATCH FACE STUDIO", color = Gold, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
    Spacer(Modifier.height(8.dp))
    Text("Create a face concept and preview it on a round dial.", color = Muted, fontSize = 13.sp)
    Spacer(Modifier.height(18.dp))
    Box(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(22.dp)).padding(18.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            WatchPreview()
            Spacer(Modifier.height(16.dp))
            Text("Analog + Digital", color = Color.White, fontWeight = FontWeight.Bold)
            Text("More editor controls are coming in the next build.", color = Muted, fontSize = 12.sp)
        }
    }
    Spacer(Modifier.height(16.dp))
    OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.AddPhotoAlternate, null); Spacer(Modifier.width(8.dp)); Text("Photo background — coming soon") }
    OutlinedButton(onClick = {}, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Default.Palette, null); Spacer(Modifier.width(8.dp)); Text("Hands, colors & complications — coming soon") }
}

@Composable
fun LibraryScreen() {
    Text("YOUR LIBRARY", color = Gold, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
    Spacer(Modifier.height(10.dp))
    Text("Your saved designs will appear here.", color = Muted)
    Spacer(Modifier.height(18.dp))
    WatchPreview()
    Text("No saved faces yet", color = Color.White, fontWeight = FontWeight.SemiBold)
}

@Composable
fun SettingsScreen() {
    Text("DEVICE & APP SETTINGS", color = Gold, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
    Spacer(Modifier.height(14.dp))
    SettingRow("Target watch", "Round smartwatch · Lefun Health")
    SettingRow("Firmware noted", "SV8.1_HV2.1")
    SettingRow("Bluetooth transfer", "Not connected / protocol not verified")
    SettingRow("App version", "0.1 · Prototype")
    Spacer(Modifier.height(16.dp))
    Text("This prototype does not yet connect to or modify your watch.", color = Muted, fontSize = 12.sp)
}
@Composable
fun SettingRow(title: String, value: String) {
    Column(Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        Text(title, color = Color.White, fontWeight = FontWeight.SemiBold)
        Text(value, color = Muted, fontSize = 12.sp)
    }
    HorizontalDivider(color = Panel)
}
