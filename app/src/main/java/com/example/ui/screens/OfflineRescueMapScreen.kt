package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Sos
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MissingPersonCase
import com.example.data.model.OfflineMapZone
import com.example.ui.theme.ToponAlertAmber
import com.example.ui.theme.ToponAlertCoral
import com.example.ui.theme.ToponVerifiedGreen

@Composable
fun OfflineRescueMapScreen(
    cases: List<MissingPersonCase>,
    offlineZones: List<OfflineMapZone>,
    onToggleDownloadZone: (String) -> Unit,
    onCaseSelected: (MissingPersonCase) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val infiniteTransition = rememberInfiniteTransition(label = "radar_pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 20f,
        targetValue = 90f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_radius"
    )

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Top Header
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "نقشه امدادی و رادار جستجو",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "پشتیبانی کامل از مسیریابی آفلاین در مناطق کوهستانی و دورافتاده",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ToponVerifiedGreen.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.WifiOff,
                                contentDescription = null,
                                tint = ToponVerifiedGreen,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "آفلاین آماده", color = ToponVerifiedGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Interactive Vector Map Canvas
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(310.dp)
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF091426)),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Custom Tactical Map Graphic Canvas
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height

                        // Topographic Grid Lines
                        val gridSpacing = 45.dp.toPx()
                        var x = 0f
                        while (x <= w) {
                            drawLine(
                                color = Color(0x1A38BDF8),
                                start = Offset(x, 0f),
                                end = Offset(x, h),
                                strokeWidth = 1f
                            )
                            x += gridSpacing
                        }
                        var y = 0f
                        while (y <= h) {
                            drawLine(
                                color = Color(0x1A38BDF8),
                                start = Offset(0f, y),
                                end = Offset(w, y),
                                strokeWidth = 1f
                            )
                            y += gridSpacing
                        }

                        // Mountain contour curves (Tochal zone representation)
                        drawCircle(
                            color = Color(0x140284C7),
                            radius = w * 0.45f,
                            center = Offset(w * 0.7f, h * 0.35f),
                            style = Stroke(width = 1.5f)
                        )
                        drawCircle(
                            color = Color(0x140284C7),
                            radius = w * 0.28f,
                            center = Offset(w * 0.7f, h * 0.35f),
                            style = Stroke(width = 1.5f)
                        )

                        // Center User Volunteer Position
                        val userCenter = Offset(w * 0.42f, h * 0.52f)

                        // Animated Pulsing Search Radar Ring
                        drawCircle(
                            color = Color(0x3306B6D4),
                            radius = pulseRadius * 2.2f,
                            center = userCenter,
                            style = Stroke(width = 2f)
                        )
                        drawCircle(
                            color = Color(0xFF06B6D4),
                            radius = 6.dp.toPx(),
                            center = userCenter
                        )
                        drawCircle(
                            color = Color.White,
                            radius = 2.dp.toPx(),
                            center = userCenter
                        )

                        // Incident Pins (Park Mellat & Tochal & Tajrish)
                        val pin1 = Offset(w * 0.35f, h * 0.38f) // Park Mellat
                        drawCircle(
                            color = Color(0x33EF4444),
                            radius = 28.dp.toPx(),
                            center = pin1,
                            style = Stroke(width = 1.5f)
                        )
                        drawCircle(
                            color = Color(0xFFEF4444),
                            radius = 7.dp.toPx(),
                            center = pin1
                        )

                        val pin2 = Offset(w * 0.68f, h * 0.28f) // Tochal
                        drawCircle(
                            color = Color(0x33F59E0B),
                            radius = 35.dp.toPx(),
                            center = pin2,
                            style = Stroke(width = 1.5f)
                        )
                        drawCircle(
                            color = Color(0xFFF59E0B),
                            radius = 7.dp.toPx(),
                            center = pin2
                        )
                    }

                    // Map Overlay Badges
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = 0.65f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "موقعیت کنونی: ۳۵.۷۷۸۲° N, ۵۱.۴۱۱۶° E",
                                color = Color(0xFFE2E8F0),
                                fontSize = 10.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = 0.65f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "شعاع پایش رادار: ۵ کیلومتر",
                                color = Color(0xFF38BDF8),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Quick Map Action Pins list at bottom
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        cases.take(2).forEach { c ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.Black.copy(alpha = 0.8f))
                                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(10.dp))
                                    .clickable { onCaseSelected(c) }
                                    .padding(8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = ToponAlertCoral,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Column {
                                        Text(text = c.fullName, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        Text(text = "فاصله: ~۱.۲ کیلومتر", color = Color(0xFF94A3B8), fontSize = 9.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Emergency SOS Quick Dispatch Buttons
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "سیستم هشدار لحظه‌ای و تماس اضطراری مراکز امدادی",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SosQuickButton(
                        label = "اورژانس ۱۱۵",
                        subtext = "امداد پزشکی",
                        color = Color(0xFFDC2626),
                        modifier = Modifier.weight(1f),
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:115"))
                            context.startActivity(intent)
                        }
                    )
                    SosQuickButton(
                        label = "پلیس ۱۱۰",
                        subtext = "گشت انتظامی",
                        color = Color(0xFF1E3A8A),
                        modifier = Modifier.weight(1f),
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:110"))
                            context.startActivity(intent)
                        }
                    )
                    SosQuickButton(
                        label = "هلال احمر ۱۱۲",
                        subtext = "نجات و کوهستان",
                        color = Color(0xFFD97706),
                        modifier = Modifier.weight(1f),
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:112"))
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }

        // Offline Map Packages List
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "بسته‌های نقشه آفلاین مناطق دورافتاده",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "ذخیره روی حافظه محلی",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        items(offlineZones, key = { it.id }) { zone ->
            OfflineZoneCardItem(
                zone = zone,
                onToggleDownload = { onToggleDownloadZone(zone.id) }
            )
        }
    }
}

@Composable
fun SosQuickButton(
    label: String,
    subtext: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(60.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(text = subtext, color = Color.White.copy(alpha = 0.8f), fontSize = 10.sp)
        }
    }
}

@Composable
fun OfflineZoneCardItem(
    zone: OfflineMapZone,
    onToggleDownload: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = zone.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${zone.region} • حجم: ${zone.sizeMb} مگابایت",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            if (zone.isDownloaded) {
                OutlinedButton(
                    onClick = onToggleDownload,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ToponVerifiedGreen)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "دانلود شده", fontSize = 11.sp)
                }
            } else {
                Button(
                    onClick = onToggleDownload,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "دانلود آفلاین", fontSize = 11.sp)
                }
            }
        }
    }
}
