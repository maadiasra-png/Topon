package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccidentType
import com.example.data.model.CasualtyAnalysisResult
import com.example.data.model.DisasterType
import com.example.data.model.ToponCase
import com.example.ui.theme.ToponAlertAmber
import com.example.ui.theme.ToponAlertCoral
import com.example.ui.theme.ToponVerifiedGreen
import com.example.ui.viewmodel.ToponViewModel

@Composable
fun IncidentReportingScreen(
    viewModel: ToponViewModel,
    onReportSubmitted: (ToponCase) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Accident, 1: Disaster
    var showSuccessDialog by remember { mutableStateOf<ToponCase?>(null) }

    val casualtyResult by viewModel.casualtyAnalysisResult.collectAsState()
    val isAnalyzing by viewModel.isAnalyzingCasualty.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Screen Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(ToponAlertCoral.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = ToponAlertCoral,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "سامانه ثبت سوانح و حوادث میدانی",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "گزارش‌های شهروندی مستقیماً به مرکز هماهنگی امداد و مأموران مجاز متصل می‌شوند",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("گزارش تصادف رانندگی", fontWeight = FontWeight.SemiBold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("گزارش بحران و سوانح", fontWeight = FontWeight.SemiBold) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedTab == 0) {
            AccidentReportForm(
                isAnalyzing = isAnalyzing,
                casualtyResult = casualtyResult,
                onAnalyzeImage = { viewModel.analyzeCasualtyImage(null) },
                onSubmit = { type, location, lat, lon, desc, photoUri, casualties, isEmergency ->
                    viewModel.submitAccidentReport(
                        accidentType = type,
                        locationName = location,
                        latitude = lat,
                        longitude = lon,
                        description = desc,
                        photoUri = photoUri,
                        estimatedCasualties = casualties,
                        isEmergency = isEmergency
                    ) { createdCase ->
                        showSuccessDialog = createdCase
                        onReportSubmitted(createdCase)
                    }
                }
            )
        } else {
            DisasterReportForm(
                onSubmit = { type, location, lat, lon, desc, photoUri ->
                    viewModel.submitDisasterReport(
                        disasterType = type,
                        locationName = location,
                        latitude = lat,
                        longitude = lon,
                        description = desc,
                        photoUri = photoUri
                    ) { createdCase ->
                        showSuccessDialog = createdCase
                        onReportSubmitted(createdCase)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Privacy by Design & Chain of Custody notice
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            )
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = ToponVerifiedGreen,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "حفاظت از داده‌ها: کلیه تصاویر با هش معتبر SHA-256 و پروتکل غیرقابل انکار زنجیره نگهداری (Chain of Custody) ثبت می‌گردند.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
private fun AccidentReportForm(
    isAnalyzing: Boolean,
    casualtyResult: CasualtyAnalysisResult?,
    onAnalyzeImage: () -> Unit,
    onSubmit: (AccidentType, String, Double, Double, String, String?, Int, Boolean) -> Unit
) {
    var selectedAccidentType by remember { mutableStateOf<AccidentType>(AccidentType.MULTI_CAR) }
    var locationName by remember { mutableStateOf("تهران، اتوبان شهید همت شرق، نرسیده به چمران") }
    var description by remember { mutableStateOf("") }
    var casualtiesCount by remember { mutableIntStateOf(1) }
    var isEmergency by remember { mutableStateOf(true) }
    var hasPhotoAttached by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Accident Type Selection
        Text(
            text = "نوع حادثه رانندگی:",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                AccidentType.MULTI_CAR,
                AccidentType.PEDESTRIAN,
                AccidentType.ROLLOVER,
                AccidentType.VEHICLE_COLLISION
            ).forEach { type ->
                FilterChip(
                    selected = selectedAccidentType == type,
                    onClick = { selectedAccidentType = type },
                    label = { Text(type.labelFa, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }

        // Location Field
        OutlinedTextField(
            value = locationName,
            onValueChange = { locationName = it },
            label = { Text("موقعیت مکانی دقیق حادثه") },
            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors()
        )

        // Casualties Counter
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("تعداد مصدومان احتمالی", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text("تخمین اولیه جهت اعزام متناسب آمبولانس", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { if (casualtiesCount > 0) casualtiesCount-- },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "کاهش", modifier = Modifier.size(16.dp))
                    }
                    Text(
                        text = casualtiesCount.toString(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(horizontal = 14.dp)
                    )
                    IconButton(
                        onClick = { casualtiesCount++ },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "افزایش", modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // Emergency Priority Toggle
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
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
                    Text("وضعیت اضطراری فوری (کد بحران)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Text("نیاز به اعزام فوری اکیپ نجات و پلیس راهور", fontSize = 11.sp, color = ToponAlertCoral)
                }
                Switch(
                    checked = isEmergency,
                    onCheckedChange = { isEmergency = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = ToponAlertCoral)
                )
            }
        }

        // Description Field
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("توضیحات و شرایط محیطی (مسیر بسته، نشت سوخت و ...)") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            shape = RoundedCornerShape(12.dp)
        )

        // Photo Attachment & AI Casualty Analysis
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("مستندات تصویری صحنه", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                    OutlinedButton(
                        onClick = {
                            hasPhotoAttached = !hasPhotoAttached
                            if (hasPhotoAttached) onAnalyzeImage()
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(if (hasPhotoAttached) "حذف تصویر" else "ثبت / پیوست تصویر", fontSize = 11.sp)
                    }
                }

                if (hasPhotoAttached) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(ToponVerifiedGreen.copy(alpha = 0.15f))
                            .border(1.dp, ToponVerifiedGreen, RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ToponVerifiedGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("تصویر صحنه پیوست شد. هش یکپارچگی ثبت گردید.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // AI Casualty Analysis Button & Card
                    OutlinedButton(
                        onClick = onAnalyzeImage,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isAnalyzing
                    ) {
                        if (isAnalyzing) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("در حال پردازش هوشمند تصویر توسط لایه AI تاپان...", fontSize = 11.sp)
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("تحلیل هوشمند تصویر (تعداد مصدومان و خطرات محیطی)", fontSize = 12.sp)
                        }
                    }

                    if (casualtyResult != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "نتیجه ارزیابی هوش مصنوعی (پیشنهاد به اپراتور انسانی):",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("تعداد افراد شناسایی‌شده در تصویر: ${casualtyResult.peopleCount} نفر", fontSize = 11.sp)
                                Text("امتیاز وضوح و کیفیت تصویر: ${casualtyResult.imageQualityScore}٪ (${casualtyResult.clarityAssessment})", fontSize = 11.sp)
                                Text("ارزیابی نور محیط: ${casualtyResult.lightingAssessment}", fontSize = 11.sp)
                                Text("موانع و شرایط دید: ${casualtyResult.obstructionNotes}", fontSize = 11.sp)
                                Text("وضعیت اولیه پیشنهادی: ${casualtyResult.preliminaryStatus}", fontSize = 11.sp, color = ToponAlertCoral, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "تذکر اخلاقی: ${casualtyResult.ethicalDisclaimer}",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Submit Button
        Button(
            onClick = {
                onSubmit(
                    selectedAccidentType,
                    locationName,
                    35.7512,
                    51.3980,
                    description.ifBlank { "گزارش تصادف در $locationName با $casualtiesCount مصدوم احتمالی" },
                    if (hasPhotoAttached) "content://accident_evidence_simulated" else null,
                    casualtiesCount,
                    isEmergency
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isEmergency) ToponAlertCoral else MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isEmergency) "ارسال فوری گزارش حادثه اضطراری" else "ثبت و ارسال گزارش تصادف",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun DisasterReportForm(
    onSubmit: (DisasterType, String, Double, Double, String, String?) -> Unit
) {
    var selectedDisasterType by remember { mutableStateOf(DisasterType.FIRE) }
    var locationName by remember { mutableStateOf("تهران، جاده قدیم قم، کهریزک، شهرک صنعتی") }
    var description by remember { mutableStateOf("") }
    var hasPhotoAttached by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            text = "نوع سانحه یا بحران:",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                DisasterType.FIRE,
                DisasterType.EARTHQUAKE,
                DisasterType.FLOOD,
                DisasterType.COLLAPSE
            ).forEach { type ->
                FilterChip(
                    selected = selectedDisasterType == type,
                    onClick = { selectedDisasterType = type },
                    label = { Text(type.labelFa, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ToponAlertCoral.copy(alpha = 0.2f),
                        selectedLabelColor = ToponAlertCoral
                    )
                )
            }
        }

        OutlinedTextField(
            value = locationName,
            onValueChange = { locationName = it },
            label = { Text("موقعیت جغرافیایی و محدوده سانحه") },
            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = ToponAlertCoral) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("جزئیات سانحه، وسعت حریق/تخریب و نیازهای اولیه امدادرسانی") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 4,
            shape = RoundedCornerShape(12.dp)
        )

        // Photo / Drone Evidence
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, tint = ToponAlertCoral)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("پیوست تصویر یا شواهد میدانی", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                OutlinedButton(
                    onClick = { hasPhotoAttached = !hasPhotoAttached },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(if (hasPhotoAttached) "حذف" else "انتخاب تصویر", fontSize = 11.sp)
                }
            }
        }

        // Submit Button
        Button(
            onClick = {
                onSubmit(
                    selectedDisasterType,
                    locationName,
                    35.5200,
                    51.3600,
                    description.ifBlank { "گزارش سانحه ${selectedDisasterType.labelFa} در $locationName" },
                    if (hasPhotoAttached) "content://disaster_evidence_simulated" else null
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ToponAlertCoral)
        ) {
            Icon(Icons.Default.Warning, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("ارسال هشدار بحران به مراجع امدادی", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}
