package com.alikhan.watch

import android.Manifest
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.graphics.Bitmap
import android.graphics.Canvas as NativeCanvas
import android.graphics.Paint
import android.graphics.Color as AColor
import android.os.Build
import android.os.Bundle
import android.os.ParcelUuid
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlinx.coroutines.*
import java.util.UUID
import kotlin.math.cos
import kotlin.math.sin

private val Gold = Color(0xFFD6B36A)
private val Bg = Color(0xFF090909)
private val Panel = Color(0xFF191919)
private val SERVICE_UUID = UUID.fromString("000018d0-0000-1000-8000-00805f9b34fb")
private val WRITE_UUID = UUID.fromString("00002d01-0000-1000-8000-00805f9b34fb")
private val NOTIFY_UUID = UUID.fromString("00002d00-0000-1000-8000-00805f9b34fb")

data class Face(val name:String, val style:Int, val accent:Int)
private val faces = listOf(
    Face("Royal Gold",0,0xFFD6B36A.toInt()), Face("Midnight",1,0xFF4EA5FF.toInt()),
    Face("Crimson",2,0xFFE45B62.toInt()), Face("Emerald",3,0xFF55C99A.toInt()),
    Face("Minimal White",4,0xFFEAEAEA.toInt()), Face("Cyber Blue",5,0xFF36D9F5.toInt())
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { WatchFacesApp(this) } }
}

@Composable
fun WatchFacesApp(activity: MainActivity) {
    var selected by remember { mutableStateOf(faces[0]) }
    var status by remember { mutableStateOf("Select a face, then connect your watch.") }
    var scanning by remember { mutableStateOf(false) }
    var devices by remember { mutableStateOf(listOf<BluetoothDevice>()) }
    var connected by remember { mutableStateOf<BluetoothGatt?>(null) }
    var busy by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    val context = activity
    val adapter = remember { (context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter }
    // Android 6–10 requires runtime Location permission AND Location Services ON for BLE scans.
    val requiredPerms = if (Build.VERSION.SDK_INT >= 31) {
        arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
    } else {
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    }
    fun hasPerms() = requiredPerms.all { context.checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }

    val scanner = remember(adapter) { adapter.bluetoothLeScanner }
    val scanCallback = remember {
        object: ScanCallback() {
            override fun onScanResult(type:Int, result:ScanResult) {
                val d=result.device
                if (devices.none { it.address == d.address }) devices = devices + d
            }
            override fun onBatchScanResults(results: MutableList<ScanResult>) {
                results.forEach { result ->
                    val d = result.device
                    if (devices.none { it.address == d.address }) devices = devices + d
                }
            }
            override fun onScanFailed(errorCode:Int) {
                scanning=false
                status="Scan failed: $errorCode. Bluetooth/Location setting check karein."
            }
        }
    }

    fun beginScan() {
        if (!hasPerms()) {
            status = "Nearby/Location permission allow karein."
            return
        }
        if (Build.VERSION.SDK_INT <= 30) {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val locationOn = try {
                (lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
            } catch (_: Exception) { false }
            if (!locationOn) {
                scanning = false
                status = "Phone ki Location setting ON karein, phir dobara SCAN dabayein."
                return
            }
        }
        val activeScanner = adapter.bluetoothLeScanner
        if (activeScanner == null) {
            scanning = false
            status = "Bluetooth scanner available nahi. Bluetooth OFF/ON karein."
            return
        }
        devices = emptyList()
        scanning = true
        status = "Scanning... Watch ko phone ke qareeb rakhein."
        try {
            activeScanner.startScan(null, ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build(), scanCallback)
        } catch (e: SecurityException) {
            scanning = false
            status = "Permission error: Settings > Apps > WATCH STUDIO > Location allow karein."
        } catch (e: Exception) {
            scanning = false
            status = "Scan start nahi hua: ${e.message ?: "unknown error"}"
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        if (requiredPerms.all { result[it] == true }) beginScan()
        else {
            scanning = false
            status = if (Build.VERSION.SDK_INT <= 30) "Location permission deny hui. App permission settings mein Location allow karein."
            else "Bluetooth permission deny hui. App permission settings mein Nearby devices allow karein."
        }
    }

    MaterialTheme(colorScheme = darkColorScheme(primary=Gold, background=Bg, surface=Panel)) {
        Column(Modifier.fillMaxSize().background(Bg).padding(16.dp)) {
            Text("WATCH STUDIO", color=Gold, fontSize=12.sp, fontWeight=FontWeight.Bold, letterSpacing=3.sp)
            Text("Premium Watch Faces", color=Color.White, fontSize=25.sp, fontWeight=FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment=Alignment.CenterVertically) {
                FacePreview(selected, Modifier.size(150.dp))
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(selected.name, color=Color.White, fontWeight=FontWeight.Bold)
                    Text("240 × 240 • RGB565", color=Color.Gray, fontSize=12.sp)
                    Text("Static face image transfer", color=Gold, fontSize=11.sp)
                }
            }
            Spacer(Modifier.height(18.dp))
            Text("CHOOSE A WATCH FACE", color=Gold, fontSize=12.sp, fontWeight=FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            LazyVerticalGrid(columns=GridCells.Fixed(2), modifier=Modifier.weight(1f), verticalArrangement=Arrangement.spacedBy(10.dp), horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                items(faces) { face ->
                    Column(Modifier.background(Panel, RoundedCornerShape(16.dp)).clickable { selected=face }.padding(10.dp), horizontalAlignment=Alignment.CenterHorizontally) {
                        FacePreview(face, Modifier.size(112.dp))
                        Text(face.name, color=Color.White, fontSize=13.sp, fontWeight=FontWeight.SemiBold)
                        if(selected==face) Text("SELECTED", color=Gold, fontSize=10.sp)
                    }
                }
            }
            Button(onClick={
                if (scanning) {
                    try { adapter.bluetoothLeScanner?.stopScan(scanCallback) } catch (_: Exception) { }
                    scanning = false
                    status = "Scanning stopped."
                } else if (!hasPerms()) {
                    permissionLauncher.launch(requiredPerms)
                } else {
                    beginScan()
                }
            }, modifier=Modifier.fillMaxWidth()) { Icon(Icons.Default.Bluetooth,null); Spacer(Modifier.width(8.dp)); Text(if(scanning) "STOP SCAN" else "SCAN FOR WATCH") }
            devices.forEach { d ->
                TextButton(onClick={
                    if(hasPerms()) {
                        status="Connecting to ${d.name ?: d.address}..."
                        connected?.close()
                        connected=d.connectGatt(context,false,object:BluetoothGattCallback() {
                            override fun onConnectionStateChange(g:BluetoothGatt,s:Int,newState:Int) {
                                activity.runOnUiThread { status=if(newState==BluetoothProfile.STATE_CONNECTED) "Connected. Discovering services..." else "Disconnected (status $s)" }
                                if(newState==BluetoothProfile.STATE_CONNECTED) g.discoverServices()
                            }
                            override fun onServicesDiscovered(g:BluetoothGatt,s:Int) {
                                activity.runOnUiThread { status=if(g.getService(SERVICE_UUID)?.getCharacteristic(WRITE_UUID)!=null) "Watch connected – ready to transfer." else "Connected, but expected 0x18D0/0x2D01 characteristic not found." }
                            }
                            override fun onCharacteristicWrite(g:BluetoothGatt,c:BluetoothGattCharacteristic,s:Int) { if(s!=BluetoothGatt.GATT_SUCCESS) activity.runOnUiThread { status="Bluetooth write error: $s" } }
                        }, if(Build.VERSION.SDK_INT>=23) BluetoothDevice.TRANSPORT_LE else BluetoothDevice.TRANSPORT_AUTO)
                    }
                }) { Text("Connect: ${d.name ?: "Watch"} (${d.address})", color=Gold) }
            }
            Button(enabled=connected!=null && !busy, onClick={
                val g=connected ?: return@Button
                val ch=g.getService(SERVICE_UUID)?.getCharacteristic(WRITE_UUID)
                if(ch==null) { status="Required write characteristic missing."; return@Button }
                busy=true; progress=0f; status="Preparing face..."
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        g.requestMtu(248)
                        delay(700)
                        val bitmap=renderFaceBitmap(selected)
                        val bytes=bitmapToRgb565(bitmap)
                        // Setup bytes are copied from the captured successful transfer; their semantics remain unverified.
                        val setup=byteArrayOf(0xAB.toByte(),0x06,0x2B,0x1C,0x20,0x9D.toByte())
                        writeNoResponse(g,ch,setup)
                        delay(250)
                        for(i in 0 until 7200) {
                            val packet=ByteArray(20)
                            packet[0]=0xAB.toByte(); packet[1]=0x2C
                            packet[2]=((i shr 8) and 0xFF).toByte(); packet[3]=(i and 0xFF).toByte()
                            System.arraycopy(bytes,i*16,packet,4,16)
                            writeNoResponse(g,ch,packet)
                            if(i%40==0) { progress=i/7200f; activity.runOnUiThread { status="Sending face: ${i*100/7200}%" } }
                            delay(18)
                        }
                        activity.runOnUiThread { progress=1f; busy=false; status="All image packets sent. Watch-side apply/confirmation is not yet verified." }
                    } catch(e:Exception) { activity.runOnUiThread { busy=false; status="Transfer stopped: ${e.message}" } }
                }
            }, modifier=Modifier.fillMaxWidth()) { Text(if(busy) "SENDING..." else "APPLY SELECTED FACE") }
            if(busy) LinearProgressIndicator(progress=progress, modifier=Modifier.fillMaxWidth())
            Text(status, color=Color.LightGray, fontSize=12.sp)
            Text("Note: this sends the captured static-image packet format. Animated hands/live complications are not supported by the verified protocol yet.", color=Color.Gray, fontSize=10.sp)
        }
    }
}

private fun writeNoResponse(g:BluetoothGatt, c:BluetoothGattCharacteristic, data:ByteArray) {
    c.writeType=BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
    if(Build.VERSION.SDK_INT>=33) {
        val result=g.writeCharacteristic(c,data,BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE)
        if(result!=BluetoothStatusCodes.SUCCESS) throw IllegalStateException("GATT write rejected: $result")
    } else {
        @Suppress("DEPRECATION")
        c.value=data
        @Suppress("DEPRECATION")
        if(!g.writeCharacteristic(c)) throw IllegalStateException("GATT write rejected")
    }
}
private fun renderFaceBitmap(face:Face):Bitmap {
    val bmp=Bitmap.createBitmap(240,240,Bitmap.Config.RGB_565)
    val c=NativeCanvas(bmp); val p=Paint(Paint.ANTI_ALIAS_FLAG)
    val bg=when(face.style){4->0xFFF4F1E8.toInt(); 1->0xFF071323.toInt(); 2->0xFF200B12.toInt(); 3->0xFF071B16.toInt();5->0xFF07121C.toInt();else->0xFF090909.toInt()}
    c.drawColor(bg)
    val accent=face.accent
    p.color=accent; p.style=Paint.Style.STROKE; p.strokeWidth=5f
    c.drawCircle(120f,120f,108f,p)
    p.strokeWidth=1.5f
    for(i in 0 until 60) {
        val a=Math.PI*i/30.0
        val r1=if(i%5==0)88f else 96f; val r2=103f
        p.strokeWidth=if(i%5==0)3f else 1f
        c.drawLine(120f+(kotlin.math.cos(a)*r1).toFloat(),120f+(kotlin.math.sin(a)*r1).toFloat(),120f+(kotlin.math.cos(a)*r2).toFloat(),120f+(kotlin.math.sin(a)*r2).toFloat(),p)
    }
    p.style=Paint.Style.FILL
    val hand=if(face.style==4) AColor.BLACK else AColor.WHITE
    p.color=hand; p.strokeWidth=7f; p.strokeCap=Paint.Cap.ROUND
    c.drawLine(120f,120f,85f,91f,p)
    p.color=accent; p.strokeWidth=5f
    c.drawLine(120f,120f,169f,101f,p)
    p.color=0xFFE85D58.toInt(); p.strokeWidth=2f
    c.drawLine(120f,120f,127f,43f,p)
    p.color=accent; c.drawCircle(120f,120f,6f,p)
    p.color=hand; p.textSize=12f; p.textAlign=Paint.Align.CENTER
    c.drawText(face.name.uppercase(),120f,190f,p)
    p.textSize=9f; c.drawText("12   3   6   9",120f,207f,p)
    return bmp
}
private fun bitmapToRgb565(bitmap:Bitmap):ByteArray {
    val pixels=IntArray(240*240); bitmap.getPixels(pixels,0,240,0,0,240,240)
    val out=ByteArray(pixels.size*2); var j=0
    for(px in pixels) {
        val r=(px shr 16) and 0xFF; val g=(px shr 8) and 0xFF; val b=px and 0xFF
        val v=((r shr 3) shl 11) or ((g shr 2) shl 5) or (b shr 3)
        out[j++]=((v shr 8) and 0xFF).toByte(); out[j++]=(v and 0xFF).toByte()
    }
    return out
}
@Composable
fun FacePreview(face:Face, modifier:Modifier=Modifier) {
    Canvas(modifier) {
        val center=androidx.compose.ui.geometry.Offset(size.width/2,size.height/2)
        val r=size.minDimension*.46f
        val bg=when(face.style){4->Color(0xFFF4F1E8);1->Color(0xFF071323);2->Color(0xFF200B12);3->Color(0xFF071B16);5->Color(0xFF07121C);else->Color(0xFF090909)}
        drawCircle(bg,r*1.05f,center); val accent=Color(face.accent)
        drawCircle(accent,r,center,style=Stroke(3.dp.toPx()))
        for(i in 0 until 60) {
            val a=Math.PI*i/30.0; val ri=if(i%5==0).78f else .86f
            drawLine(if(i%5==0) accent else Color.Gray,androidx.compose.ui.geometry.Offset(center.x+cos(a).toFloat()*r*ri,center.y+sin(a).toFloat()*r*ri),androidx.compose.ui.geometry.Offset(center.x+cos(a).toFloat()*r*.93f,center.y+sin(a).toFloat()*r*.93f),if(i%5==0)2.dp.toPx() else 1.dp.toPx(),StrokeCap.Round)
        }
        val hand=if(face.style==4) Color.Black else Color.White
        drawLine(hand,center,androidx.compose.ui.geometry.Offset(center.x-r*.32f,center.y-r*.28f),4.dp.toPx(),StrokeCap.Round)
        drawLine(accent,center,androidx.compose.ui.geometry.Offset(center.x+r*.48f,center.y-r*.17f),3.dp.toPx(),StrokeCap.Round)
        drawLine(Color(0xFFE85D58),center,androidx.compose.ui.geometry.Offset(center.x+r*.07f,center.y-r*.72f),1.dp.toPx(),StrokeCap.Round)
        drawCircle(accent,3.dp.toPx(),center)
    }
}
