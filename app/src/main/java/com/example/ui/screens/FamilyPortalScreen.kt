package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CasePriority
import com.example.data.model.CitizenReport
import com.example.data.model.ConditionType
import com.example.data.model.MissingPersonCase
import com.example.data.model.ReportStatus
import com.example.data.model.User
import com.example.ui.theme.ToponVerifiedGreen

@Composable
fun FamilyPortalScreen(
    currentUser: User,
    cases: List<MissingPersonCase>,
    reports: List<CitizenReport>,
    onRegisterCase: (
        fullName: String,
        age: Int,
        conditionType: ConditionType,
        priority: CasePriority,
        lastSeenLocation: String,
        heightCm: Int,
        buildType: String,
        clothing: String,
        medicalNeeds: String,
        rewardToman: Long
    ) -> Unit,
    onOpenChat: (caseId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showRegisterDialog by remember { mutableStateOf(false) }

    // Filter cases associated with family or primary case
    val familyCases = cases.filter { it.registeredByUserId == currentUser.id || it.id == "case-01" }
    val verifiedLeads = reports.filter { it.status == ReportStatus.VERIFIED }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Header
        item {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "درگاه اختصاصی خانواده فرد گمشده",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "پیگیری رسمی روند جستجو، ثبت سرنخ‌ها و ارتباط مستقیم با گشت‌های امداد",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF7C3AED).copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "خانواده مددجو",
                            fontSize = 11.sp,
                            color = Color(0xFFC084FC),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = { showRegisterDialog = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ثبت پرونده جدید فرد گمشده", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Family Case Card Tracker
        item {
            Text(
                text = "پرونده‌های فعال خانواده (${familyCases.size})",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }

        items(familyCases, key = { it.id }) { caseItem ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = caseItem.fullName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "شماره پرونده: ${caseItem.caseNumber} • سن: ${caseItem.age} سال",
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
                            Text(
                                text = caseItem.status.label,
                                fontSize = 11.sp,
                                color = ToponVerifiedGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Investigation Progress Pipeline
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ProgressStep("ثبت اولیه", true)
                        ProgressStep("تأیید مدارک پلیس", true)
                        ProgressStep("پایش میدانی", true)
                        ProgressStep("یافت سوژه", false)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "آخرین موقعیت شناخته شده: ${caseItem.lastSeenLocation}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "صندوق پاداش خانواده: ${(caseItem.rewardAmountToman / 1_000_000)} میلیون تومان",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "تعداد داوطلبان فعال در شعاع: ${caseItem.nearbyVolunteersCount} نفر",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Button(
                            onClick = { onOpenChat(caseItem.id) },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Icon(Icons.Default.Message, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("گفتگوی امن با امدادگران", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Section: Verified Human Leads
        item {
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "سرنخ‌های تأیید شده توسط کارشناس (${verifiedLeads.size})",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }

        if (verifiedLeads.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "سرنخ‌های شهروندی پس از بررسی دقیق کارشناسان پلیس و امداد در این بخش نمایش داده می‌شوند.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(verifiedLeads, key = { it.id }) { report ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "سرنخ تأیید شده: ${report.locationName}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ToponVerifiedGreen, modifier = Modifier.size(16.dp))
                        }
                        Text(
                            text = report.citizenNotes,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }

    // Register Case Dialog
    if (showRegisterDialog) {
        RegisterCaseDialog(
            onDismiss = { showRegisterDialog = false },
            onSubmit = { name, age, cond, prio, loc, h, b, c, m, r ->
                onRegisterCase(name, age, cond, prio, loc, h, b, c, m, r)
                showRegisterDialog = false
            }
        )
    }
}

@Composable
fun ProgressStep(title: String, isDone: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(if (isDone) ToponVerifiedGreen else Color.Gray.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            if (isDone) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
            }
        }
        Text(
            text = title,
            fontSize = 9.sp,
            color = if (isDone) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

@Composable
fun RegisterCaseDialog(
    onDismiss: () -> Unit,
    onSubmit: (
        fullName: String,
        age: Int,
        conditionType: ConditionType,
        priority: CasePriority,
        lastSeenLocation: String,
        heightCm: Int,
        buildType: String,
        clothing: String,
        medicalNeeds: String,
        rewardToman: Long
    ) -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var ageStr by remember { mutableStateOf("") }
    var lastSeen by remember { mutableStateOf("") }
    var clothing by remember { mutableStateOf("") }
    var medicalNeeds by remember { mutableStateOf("") }
    var rewardStr by remember { mutableStateOf("50000000") }
    var selectedCondition by remember { mutableStateOf(ConditionType.ALZHEIMER) }
    var selectedPriority by remember { mutableStateOf(CasePriority.CRITICAL) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ثبت پرونده جدید فرد گمشده", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(
                modifier = Modifier.padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("نام و نام خانوادگی فرد گمشده") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = ageStr,
                    onValueChange = { ageStr = it },
                    label = { Text("سن (سال)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = lastSeen,
                    onValueChange = { lastSeen = it },
                    label = { Text("آخرین مکان و زمان مشاهده") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = clothing,
                    onValueChange = { clothing = it },
                    label = { Text("مشخصات پوشش و لباس") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = medicalNeeds,
                    onValueChange = { medicalNeeds = it },
                    label = { Text("نیازهای پزشکی / دارویی فوری") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val age = ageStr.toIntOrNull() ?: 50
                    val reward = rewardStr.toLongOrNull() ?: 50_000_000L
                    onSubmit(
                        fullName.ifBlank { "مددجوی گمشده" },
                        age,
                        selectedCondition,
                        selectedPriority,
                        lastSeen.ifBlank { "تهران" },
                        170,
                        "متوسط",
                        clothing.ifBlank { "لباس معمولی" },
                        medicalNeeds.ifBlank { "بدون نیاز دارویی خاص" },
                        reward
                    )
                }
            ) {
                Text("ثبت و ارسال به ستاد")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف")
            }
        }
    )
}
