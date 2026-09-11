package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocation
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicNone
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.FaceAnalysisResult
import com.example.data.model.MissingPersonCase
import com.example.ui.components.PrivacyEncryptionBadge
import com.example.ui.theme.ToponAlertAmber
import com.example.ui.theme.ToponAlertCoral
import com.example.ui.theme.ToponCyanScanner
import com.example.ui.theme.ToponVerifiedGreen
import java.text.NumberFormat
import java.util.Locale

@Composable
fun BiometricScannerScreen(
    cases: List<MissingPersonCase>,
    selectedCase: MissingPersonCase?,
    scannedBitmap: Bitmap?,
    isScanning: Boolean,
    faceAnalysisResult: FaceAnalysisResult?,
    hasAudioSample: Boolean,
    onCaseSelected: (MissingPersonCase) -> Unit,
    onBitmapCaptured: (Bitmap?) -> Unit,
    onToggleAudioSample: () -> Unit,
    onStartScan: (MissingPersonCase) -> Unit,
    onSubmitReport: (caseId: String, name: String, location: String, photoUri: String?, notes: String, isEmergency: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var showCasePicker by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current

    // Camera Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            onBitmapCaptured(bitmap)
            selectedImageUri = null
        }
    }

    // Gallery Launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                onBitmapCaptured(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "scan_laser")
    val laserPosition by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_y"
    )

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Top Header
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "اسکنر هوشمند چهره و صدا",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "استخراج سرنخ‌های احتمالی جهت بررسی کارشناسان مجاز",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    PrivacyEncryptionBadge()
                }
            }
        }

        // Active Target Case Card
        item {
            selectedCase?.let { target ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                if (target.photoRes != null) {
                                    Image(
                                        painter = painterResource(id = target.photoRes),
                                        contentDescription = target.fullName,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "سوژه تطبیق: ${target.fullName}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "پرونده: ${target.caseNumber} • ${target.age} ساله",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = { showCasePicker = true },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("تغییر سوژه", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Viewfinder / Camera Surface with Photo Preview, Replace & Delete
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(290.dp)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF040814))
                    .border(2.dp, if (isScanning) ToponCyanScanner else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
            ) {
                // Background: Captured Bitmap or Target Case
                if (scannedBitmap != null) {
                    Image(
                        bitmap = scannedBitmap.asImageBitmap(),
                        contentDescription = "تصویر بارگذاری شده",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (selectedCase?.photoRes != null) {
                    Image(
                        painter = painterResource(id = selectedCase.photoRes),
                        contentDescription = "تصویر پرونده",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.7f))
                    )
                }

                // Dark gradient overlay
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.5f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.75f)
                                )
                            )
                        )
                )

                // Laser Scanning Line Animation
                if (isScanning) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val y = size.height * laserPosition
                        drawLine(
                            color = Color(0xFF06B6D4),
                            start = Offset(0f, y),
                            end = Offset(size.width, y),
                            strokeWidth = 4.dp.toPx()
                        )
                        drawLine(
                            color = Color(0x6606B6D4),
                            start = Offset(0f, y - 6.dp.toPx()),
                            end = Offset(size.width, y + 6.dp.toPx()),
                            strokeWidth = 12.dp.toPx()
                        )
                    }
                }

                // Reticle / Face guide
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .align(Alignment.Center)
                        .border(
                            width = 1.5.dp,
                            color = if (isScanning) ToponCyanScanner else Color.White.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(32.dp)
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(1.dp)
                            .background(ToponCyanScanner.copy(alpha = 0.7f))
                            .align(Alignment.Center)
                    )
                    Text(
                        text = if (isScanning) "تحلیل الگوهای چهره..." else "چهره را در کادر تنظیم کنید",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 9.sp,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp)
                    )
                }

                // Delete Photo button if photo is loaded
                if (scannedBitmap != null) {
                    IconButton(
                        onClick = { onBitmapCaptured(null) },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f))
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "حذف عکس", tint = ToponAlertCoral, modifier = Modifier.size(18.dp))
                    }
                }

                // Viewfinder HUD Top
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopStart)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.6f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isScanning) "وضعیت: استخراج ویژگی‌ها" else if (scannedBitmap != null) "تصویر آماده پردازش" else "حالت آماده‌باش",
                            color = if (isScanning) ToponCyanScanner else Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (hasAudioSample) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(ToponVerifiedGreen.copy(alpha = 0.8f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(text = "نمونه صوتی ضمیمه شد", color = Color.White, fontSize = 10.sp)
                        }
                    }
                }

                // Viewfinder HUD Bottom: Live Biometric Metrics
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    HudIndicator(title = "فاصله مردمک", value = selectedCase?.physicalAttributes?.eyeDistanceRatio?.take(13) ?: "۳۱.۴mm")
                    HudIndicator(title = "شاخص قامت", value = "${selectedCase?.physicalAttributes?.heightCm ?: 170} cm")
                    HudIndicator(title = "بسامد صدا", value = if (hasAudioSample) "تطابق فرکانس" else "پایش غیرفعال")
                }
            }
        }

        // Photo Upload Options: Camera, Gallery & Voice Sample
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { cameraLauncher.launch() },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("دوربین", color = MaterialTheme.colorScheme.onSurface, fontSize = 11.sp)
                }

                Button(
                    onClick = {
                        galleryLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("گالری", color = MaterialTheme.colorScheme.onSurface, fontSize = 11.sp)
                }

                Button(
                    onClick = onToggleAudioSample,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (hasAudioSample) ToponVerifiedGreen.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Icon(
                        if (hasAudioSample) Icons.Default.Mic else Icons.Default.MicNone,
                        contentDescription = null,
                        tint = if (hasAudioSample) ToponVerifiedGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (hasAudioSample) "صوت فعال" else "تحلیل صوت",
                        color = if (hasAudioSample) ToponVerifiedGreen else MaterialTheme.colorScheme.onSurface,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Start Analysis Button
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Button(
                    onClick = {
                        selectedCase?.let { onStartScan(it) }
                    },
                    enabled = !isScanning && selectedCase != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (isScanning) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "در حال تحلیل سرنخ و ویژگی‌های بیومتریک...",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Icon(Icons.Default.Security, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "تحلیل هوش مصنوعی و استخراج سرنخ",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Scan Results Section with Forensic Disclaimer
        faceAnalysisResult?.let { result ->
            item {
                ScanResultReportCard(
                    result = result,
                    targetCase = selectedCase,
                    onSubmitReportClick = { showReportDialog = true }
                )
            }
        }
    }

    // Target Case Picker Dialog
    if (showCasePicker) {
        AlertDialog(
            onDismissRequest = { showCasePicker = false },
            title = { Text("انتخاب سوژه تطبیق چهره", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    cases.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (selectedCase?.id == item.id) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
                                .clickable {
                                    onCaseSelected(item)
                                    showCasePicker = false
                                }
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${item.fullName} (${item.conditionType.label})",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCasePicker = false }) {
                    Text("بستن")
                }
            }
        )
    }

    // Report Submission Dialog (Citizen Report with Human Review Lifecycle)
    if (showReportDialog && selectedCase != null) {
        var reporterLocation by remember { mutableStateOf(selectedCase.lastSeenLocation) }
        var reporterNotes by remember { mutableStateOf("فرد با مشخصات پرونده مطابقت دارد و در محل آرامش دارد.") }
        var isEmergency by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = {
                Text(
                    text = "ثبت رسمی سرنخ شهروندی در سامانه تاپان",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "با ثبت این گزارش، سرنخ به همراه تطابق احتمالی (${faceAnalysisResult?.potentialMatchPercent ?: 82}٪) جهت بررسی انسانی به کارشناسان نهاد مجاز ارجاع می‌گردد.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = reporterLocation,
                        onValueChange = { reporterLocation = it },
                        label = { Text("موقعیت دقیق مشاهده") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = reporterNotes,
                        onValueChange = { reporterNotes = it },
                        label = { Text("توضیحات تکمیلی") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { isEmergency = !isEmergency }
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = isEmergency, onCheckedChange = { isEmergency = it })
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "وضعیت اضطراری (نیاز به مداخله فوری پزشکی یا گشت)",
                            fontSize = 11.sp,
                            color = if (isEmergency) ToponAlertCoral else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSubmitReport(
                            selectedCase.id,
                            selectedCase.fullName,
                            reporterLocation,
                            selectedImageUri?.toString(),
                            reporterNotes,
                            isEmergency
                        )
                        showReportDialog = false
                    }
                ) {
                    Text("ثبت نهایی و ارسال به صف بررسی")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) {
                    Text("انصراف")
                }
            }
        )
    }
}

@Composable
fun HudIndicator(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = title, color = Color(0xFF94A3B8), fontSize = 10.sp)
        Text(text = value, color = ToponCyanScanner, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ScanResultReportCard(
    result: FaceAnalysisResult,
    targetCase: MissingPersonCase?,
    onSubmitReportClick: () -> Unit
) {
    val numberFormat = NumberFormat.getNumberInstance(Locale("fa", "IR"))
    val rewardEstimate = targetCase?.let {
        numberFormat.format((it.rewardAmountToman * 0.50).toLong())
    } ?: "۲۵,۰۰۰,۰۰۰"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Score Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Potential Match: ${result.potentialMatchPercent}٪",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "شناسه امنیتی: ${result.privacyEncryptedHash}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(ToponVerifiedGreen.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "اطمینان AI: ${result.confidencePercent}٪",
                        color = ToponVerifiedGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Forensic Disclaimer Banner (Mandatory)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(ToponAlertAmber.copy(alpha = 0.12f))
                    .border(1.dp, ToponAlertAmber.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    .padding(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = ToponAlertAmber, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = result.disclaimer,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Image Quality Assessment Details
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "ارزیابی کیفیت تصویر ورودی:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(text = "• وضعیت نور: ${result.quality.lightingCondition}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "• زاویه چهره: ${result.quality.angleAssessment}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "• وضوح و ابعاد: ${result.quality.clarityResolution}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sub-scores metric bars
            BiometricProgressBar(label = "شاخص فاصله دو چشم (Landmarks)", percent = result.potentialMatchPercent)
            Spacer(modifier = Modifier.height(6.dp))
            BiometricProgressBar(label = "شاخص تناسب قامت و ساختار فیزیکی", percent = (result.potentialMatchPercent - 4).coerceAtLeast(60))

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "تحلیل کارشناسی هوش مصنوعی:",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = result.forensicDetails,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 17.sp,
                modifier = Modifier.padding(top = 2.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                onClick = onSubmitReportClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.AddLocation, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "ارسال سرنخ به صف بررسی کارشناس مجاز",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun BiometricProgressBar(label: String, percent: Int) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = "$percent٪", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(3.dp))
        LinearProgressIndicator(
            progress = { percent / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = if (percent >= 80) ToponVerifiedGreen else MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}
