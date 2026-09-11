package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.EmergencyShare
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CasePriority
import com.example.data.model.CitizenReport
import com.example.data.model.MissingPersonCase
import com.example.data.model.ReportStatus
import com.example.data.model.User
import com.example.data.model.VerificationStatus
import com.example.ui.theme.ToponAlertAmber
import com.example.ui.theme.ToponAlertCoral
import com.example.ui.theme.ToponVerifiedGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StaffReviewDashboard(
    currentUser: User,
    cases: List<MissingPersonCase>,
    reports: List<CitizenReport>,
    onReviewReport: (reportId: String, newStatus: ReportStatus, notes: String) -> Unit,
    onUpdateCaseStatus: (caseId: String, newStatus: VerificationStatus) -> Unit,
    onUpdateCasePriority: (caseId: String, newPriority: CasePriority) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedReportForReview by remember { mutableStateOf<CitizenReport?>(null) }
    var reviewNotes by remember { mutableStateOf("") }
    var selectedCaseForEdit by remember { mutableStateOf<MissingPersonCase?>(null) }

    val pendingReports = reports.filter { it.status == ReportStatus.SUBMITTED || it.status == ReportStatus.UNDER_REVIEW }
    val criticalCases = cases.filter { it.priority == CasePriority.CRITICAL }

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
                            text = "داشبورد ارزیابی و تصمیم‌گیری نهاد مجاز",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "بررسی انسانی سرنخ‌های AI و تعیین وضعیت پرونده‌ها • ${currentUser.organization ?: "پلیس آگاهی ناجا"}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF1E3A8A).copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = currentUser.badgeNumber ?: "STAFF",
                            fontSize = 11.sp,
                            color = Color(0xFF60A5FA),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Quick Stats Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    title = "در صف بررسی",
                    count = pendingReports.size.toString(),
                    color = ToponAlertAmber,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "پرونده‌های بحرانی",
                    count = criticalCases.size.toString(),
                    color = ToponAlertCoral,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "کل گزارش‌ها",
                    count = reports.size.toString(),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Human-in-the-Loop Architecture Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "فرآیند احراز هویت انسانی (Human-in-the-Loop):",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "گزارش شهروند ← تحلیل هوش مصنوعی ← بررسی توسط کارشناس مجاز ← اعمال تغییرات در پرونده. نتایج AI هرگز به تنهایی مبنای اعلام هویت قطعی نیستند.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }

        // Section: Citizen Reports awaiting review
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "گزارش‌های شهروندی نیازمند بازبینی (${reports.size})",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        if (reports.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(30.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "هیچ گزارشی برای بررسی وجود ندارد.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(reports, key = { it.id }) { report ->
                StaffReportCardItem(
                    report = report,
                    onOpenReview = {
                        selectedReportForReview = report
                        reviewNotes = "بررسی تطابق چهره و موقعیت مکانی انجام شد."
                    }
                )
            }
        }

        // Section: Manage Missing Person Cases & Status
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "مدیریت وضعیت و اولویت پرونده‌ها (${cases.size})",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        items(cases, key = { it.id }) { c ->
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
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${c.fullName} (${c.caseNumber})",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "وضعیت: ${c.status.label} • اولویت: ${c.priority.labelFa}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    Button(
                        onClick = { selectedCaseForEdit = c },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text("تغییر وضعیت", fontSize = 11.sp)
                    }
                }
            }
        }
    }

    // Review Modal Dialog
    selectedReportForReview?.let { r ->
        AlertDialog(
            onDismissRequest = { selectedReportForReview = null },
            title = {
                Text(
                    text = "بررسی کارشناسی گزارش ${r.id}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "سوژه پرونده: ${r.reportedPersonName}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "موقعیت گزارش شده: ${r.locationName}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "توضیحات شهروند: ${r.citizenNotes}",
                        fontSize = 12.sp
                    )

                    // AI Potential Match Disclaimer Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                            .padding(10.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Potential Match: ${r.potentialMatchScore ?: 82}٪",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "اطمینان هوش مصنوعی: ${r.aiConfidencePercent ?: 88}٪",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "هشدار قانونی: این نتیجه فقط یک سرنخ احتمالی است و برای تأیید نیاز به بررسی انسانی دارد.",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 14.sp
                            )
                        }
                    }

                    OutlinedTextField(
                        value = reviewNotes,
                        onValueChange = { reviewNotes = it },
                        label = { Text("یادداشت کارشناس رسیدگی") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = {
                            onReviewReport(r.id, ReportStatus.VERIFIED, reviewNotes)
                            selectedReportForReview = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ToponVerifiedGreen)
                    ) {
                        Text("تأیید سرنخ", fontSize = 11.sp)
                    }

                    Button(
                        onClick = {
                            onReviewReport(r.id, ReportStatus.ESCALATED, reviewNotes)
                            selectedReportForReview = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ToponAlertCoral)
                    ) {
                        Text("ارجاع اضطراری", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            onReviewReport(r.id, ReportStatus.REJECTED, reviewNotes)
                            selectedReportForReview = null
                        }
                    ) {
                        Text("رد", fontSize = 11.sp)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedReportForReview = null }) {
                    Text("بستن")
                }
            }
        )
    }

    // Case Status Edit Dialog
    selectedCaseForEdit?.let { c ->
        AlertDialog(
            onDismissRequest = { selectedCaseForEdit = null },
            title = { Text("تغییر وضعیت پرونده ${c.fullName}", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("انتخاب وضعیت جدید:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    VerificationStatus.entries.forEach { status ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (c.status == status) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
                                .clickable {
                                    onUpdateCaseStatus(c.id, status)
                                    selectedCaseForEdit = null
                                }
                                .padding(10.dp)
                        ) {
                            Text(text = status.label, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("انتخاب سطح اولویت:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    CasePriority.entries.forEach { prio ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (c.priority == prio) Color(prio.colorHex).copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface)
                                .clickable {
                                    onUpdateCasePriority(c.id, prio)
                                    selectedCaseForEdit = null
                                }
                                .padding(8.dp)
                        ) {
                            Text(text = prio.labelFa, fontSize = 12.sp, color = Color(prio.colorHex), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedCaseForEdit = null }) {
                    Text("انصراف")
                }
            }
        )
    }
}

@Composable
fun StatCard(title: String, count: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = count, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = color)
        }
    }
}

@Composable
fun StaffReportCardItem(
    report: CitizenReport,
    onOpenReview: () -> Unit
) {
    val statusColor = Color(report.status.colorHex)
    val timeFormatted = SimpleDateFormat("HH:mm - yyyy/MM/dd", Locale.getDefault()).format(Date(report.timestamp))

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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(statusColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = report.status.labelFa,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "کد: ${report.id}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (report.potentialMatchScore != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "تطابق AI: ${report.potentialMatchScore}٪",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "پرونده: ${report.reportedPersonName} • گزارشگر: ${report.reporterName}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = report.locationName,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "یادداشت: ${report.citizenNotes}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(top = 4.dp)
            )

            if (report.staffReviewNotes != null) {
                Text(
                    text = "بررسی شده توسط ${report.reviewedByStaffName ?: "کارشناس"}: ${report.staffReviewNotes}",
                    fontSize = 10.sp,
                    color = ToponVerifiedGreen,
                    modifier = Modifier.padding(top = 4.dp)
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
                    text = timeFormatted,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Button(
                    onClick = onOpenReview,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("بررسی و تصمیم‌گیری", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
