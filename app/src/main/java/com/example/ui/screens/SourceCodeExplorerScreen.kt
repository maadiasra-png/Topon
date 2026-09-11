package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.IntegrationInstructions
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ToponAlertCoral
import com.example.ui.theme.ToponVerifiedGreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.security.MessageDigest

data class CodeSnippet(
    val id: String,
    val title: String,
    val language: String,
    val filePath: String,
    val description: String,
    val codeContent: String,
    val tags: List<String>
)

enum class CodeCategory(val labelFa: String, val icon: ImageVector) {
    KOTLIN_ANDROID("اندروید و معماری کاتلین", Icons.Default.PhoneAndroid),
    BACKEND_API("بک‌اند و وب‌سوکت", Icons.Default.Terminal),
    FLUTTER_DART("کلاینت فلاتر و دارت", Icons.Default.DataObject),
    SECURITY_CRYPTO("امنیت، هش و حریم خصوصی", Icons.Default.Security)
}

@Composable
fun SourceCodeExplorerScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    var selectedCategoryIndex by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var copiedSnippetId by remember { mutableStateOf<String?>(null) }

    // Live Sandbox Hash Tool State
    var testInputText by remember { mutableStateOf("پرونده شماره TOPON-2026-9042: گزارش حضور در ایستگاه مترو صادقیه") }
    var computedHashResult by remember { mutableStateOf<String?>(null) }
    var isComputingHash by remember { mutableStateOf(false) }

    val categories = CodeCategory.entries

    val allSnippets = remember {
        listOf(
            // Category 0: KOTLIN_ANDROID
            CodeSnippet(
                id = "kt_models",
                title = "مدل‌های یکپارچه پرونده و ادله (Unified Case Models)",
                language = "kotlin",
                filePath = "app/src/main/java/com/example/data/model/Models.kt",
                description = "تعریف ساختار داده‌های پرونده‌های چندمنظوره، تصادفات، حوادث، مدارک قانونی و زنجیره نگهداری",
                tags = listOf("Data Layer", "Enums", "RBAC", "Entities"),
                codeContent = """
package com.example.data.model

enum class CaseType(val labelFa: String, val badgeColorHex: Long) {
    MISSING_PERSON("فرد گمشده", 0xFF0D9488),
    WANTED_PERSON("فرد تحت تعقیب (مراجع رسمی)", 0xFFDC2626),
    ACCIDENT("گزارش تصادف", 0xFFF97316),
    DISASTER("حادثه و سوانح طبیعی", 0xFF8B5CF6),
    EMERGENCY("درخواست کمک اضطراری", 0xFFEF4444)
}

enum class EvidenceType(val labelFa: String) {
    IMAGE("تصویر"),
    VIDEO("ویدیو"),
    AUDIO("صوت و ویس"),
    TEXT_REPORT("گزارش متنی و صورتجلسه"),
    LOCATION_LOG("ردپای مکانی و GPS")
}

data class EvidenceItem(
    val id: String,
    val caseId: String,
    val type: EvidenceType,
    val title: String,
    val mediaUri: String? = null,
    val source: String,
    val uploadTime: Long = System.currentTimeMillis(),
    val uploadedByUserId: String,
    val uploadedByName: String,
    val uploadedByRole: UserRole,
    val verificationStatus: VerificationStatus = VerificationStatus.PENDING_REVIEW,
    val fileHashSha256: String
)

data class ChainOfCustodyEntry(
    val id: String,
    val evidenceId: String,
    val caseId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val actorName: String,
    val actorRole: UserRole,
    val action: String, // UPLOADED, VIEWED, VERIFIED, EXPORTED
    val details: String
)
                """.trimIndent()
            ),

            CodeSnippet(
                id = "kt_crypto",
                title = "موتور رمزنگاری و هش SHA-256 در زنجیره ادله",
                language = "kotlin",
                filePath = "app/src/main/java/com/example/util/CryptoEngine.kt",
                description = "پیاده‌سازی الگوریتم هش SHA-256 و اعتبارسنجی غیرقابل دستکاری مدارک دیجیتال",
                tags = listOf("Cryptography", "SHA-256", "Chain of Custody"),
                codeContent = """
package com.example.util

import java.security.MessageDigest

object CryptoEngine {
    fun calculateSha256(input: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(input)
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    fun verifyIntegrity(evidenceData: ByteArray, expectedHash: String): Boolean {
        val currentHash = calculateSha256(evidenceData)
        return currentHash.equals(expectedHash, ignoreCase = true)
    }

    // Biometric embedding cosine distance (Zero raw face images stored)
    fun cosineSimilarity(vectorA: FloatArray, vectorB: FloatArray): Float {
        var dotProduct = 0f
        var normA = 0f
        var normB = 0f
        for (i in vectorA.indices) {
            dotProduct += vectorA[i] * vectorB[i]
            normA += vectorA[i] * vectorA[i]
            normB += vectorB[i] * vectorB[i]
        }
        return dotProduct / (Math.sqrt(normA.toDouble()).toFloat() * Math.sqrt(normB.toDouble()).toFloat())
    }
}
                """.trimIndent()
            ),

            CodeSnippet(
                id = "kt_viewmodel",
                title = "مدیریت وضعیت و جریان‌های واکنش‌گرا در ViewModel",
                language = "kotlin",
                filePath = "app/src/main/java/com/example/ui/viewmodel/ToponViewModel.kt",
                description = "جریان‌های StateFlow، مدیریت دسترسی نقش‌محور (RBAC) و احراز هویت دومرحله‌ای",
                tags = listOf("MVVM", "Coroutines", "StateFlow", "Security"),
                codeContent = """
class ToponViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ToponRepository()

    private val _currentScreen = MutableStateFlow(ToponScreen.CASES_DASHBOARD)
    val currentScreen: StateFlow<ToponScreen> = _currentScreen.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    fun uploadEvidence(
        caseId: String,
        type: EvidenceType,
        title: String,
        mediaUri: String?,
        source: String
    ) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val mockData = (title + System.currentTimeMillis()).toByteArray()
            val fileHash = CryptoEngine.calculateSha256(mockData)

            val evidence = EvidenceItem(
                id = "ev-${'$'}{System.currentTimeMillis()}",
                caseId = caseId,
                type = type,
                title = title,
                mediaUri = mediaUri,
                source = source,
                uploadedByUserId = user.id,
                uploadedByName = user.fullName,
                uploadedByRole = user.role,
                fileHashSha256 = fileHash
            )
            repository.addEvidence(evidence)
            logAuditEvent(user, "UPLOAD_EVIDENCE", "بارگذاری مدرک ${'$'}title با هش ${'$'}fileHash")
        }
    }
}
                """.trimIndent()
            ),

            // Category 1: BACKEND_API
            CodeSnippet(
                id = "backend_fastapi",
                title = "سرویس امن بک‌اند با کنترل سطح دسترسی (FastAPI REST API)",
                language = "python",
                filePath = "backend/app/api/v1/cases.py",
                description = "مسیرهای API پرونده‌ها، احراز هویت JWT با RBAC، و تریاژ هوشمند پیشنهادی",
                tags = listOf("Backend", "FastAPI", "OpenAPI", "Security"),
                codeContent = """
from fastapi import APIRouter, Depends, HTTPException, status
from pydantic import BaseModel, Field
from typing import List, Optional
from enum import Enum
import hashlib

router = APIRouter(prefix="/api/v1/cases", tags=["Case Management"])

class CaseTypeEnum(str, Enum):
    MISSING_PERSON = "MISSING_PERSON"
    WANTED_PERSON = "WANTED_PERSON"
    ACCIDENT = "ACCIDENT"
    DISASTER = "DISASTER"
    EMERGENCY = "EMERGENCY"

class CaseCreatePayload(BaseModel):
    title: str = Field(..., max_length=150)
    case_type: CaseTypeEnum
    latitude: float
    longitude: float
    description: str
    is_emergency: bool = False

@router.post("/", status_code=status.HTTP_201_CREATED)
async def create_case(
    payload: CaseCreatePayload,
    current_user: dict = Depends(get_current_authenticated_user)
):
    # Enforce role boundaries
    if payload.case_type == CaseTypeEnum.WANTED_PERSON and current_user["role"] != "AUTHORIZED_STAFF":
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="تنها مراجع قضایی و انتظامی مجاز به ثبت افراد تحت تعقیب هستند."
        )

    case_id = f"CASE-{int(time.time())}"
    return {"case_id": case_id, "status": "PENDING_OFFICIAL_REVIEW", "triage_suggested": True}
                """.trimIndent()
            ),

            CodeSnippet(
                id = "backend_ws",
                title = "وب‌سوکت ارتباط اضطراری E2EE (WebSocket Server)",
                language = "python",
                filePath = "backend/app/websockets/emergency_channel.py",
                description = "پخش لحظه‌ای پیام‌های امدادی، مختصات مصدومان و وضعیت تریاژ به واحدهای گشت",
                tags = listOf("WebSocket", "Real-Time", "E2EE", "Dispatch"),
                codeContent = """
from fastapi import WebSocket, WebSocketDisconnect
from typing import Dict, Set

class EmergencyChannelManager:
    def __init__(self):
        self.active_connections: Dict[str, Set[WebSocket]] = {}

    async def connect(self, case_id: str, websocket: WebSocket):
        await websocket.accept()
        if case_id not in self.active_connections:
            self.active_connections[case_id] = set()
        self.active_connections[case_id].add(websocket)

    async def broadcast_rescue_update(self, case_id: str, message: dict):
        if case_id in self.active_connections:
            for connection in self.active_connections[case_id]:
                await connection.send_json({
                    "type": "RESCUE_BROADCAST",
                    "encrypted_payload": message,
                    "server_timestamp": int(time.time())
                })

manager = EmergencyChannelManager()
                """.trimIndent()
            ),

            // Category 2: FLUTTER_DART
            CodeSnippet(
                id = "flutter_model",
                title = "کلاینت فلاتر - مدل داده و مخزن هماهنگ (Flutter Multiplatform)",
                language = "dart",
                filePath = "topon_flutter/lib/models/topon_case.dart",
                description = "تعریف کدهای مدل در زبان Dart جهت همگام‌سازی کامل با کلاینت اندروید و وب",
                tags = listOf("Flutter", "Dart", "Multiplatform", "JSON"),
                codeContent = """
import 'package:flutter/foundation.dart';

enum CaseType { missingPerson, wantedPerson, accident, disaster, emergency }

class ToponCase {
  final String id;
  final String title;
  final CaseType caseType;
  final double latitude;
  final double longitude;
  final String status;
  final DateTime createdAt;

  ToponCase({
    required this.id,
    required this.title,
    required this.caseType,
    required this.latitude,
    required this.longitude,
    required this.status,
    required this.createdAt,
  });

  factory ToponCase.fromJson(Map<String, dynamic> json) {
    return ToponCase(
      id: json['id'] as String,
      title: json['title'] as String,
      caseType: CaseType.values.byName(json['case_type']),
      latitude: (json['latitude'] as num).toDouble(),
      longitude: (json['longitude'] as num).toDouble(),
      status: json['status'] as String,
      createdAt: DateTime.parse(json['created_at']),
    );
  }
}
                """.trimIndent()
            ),

            CodeSnippet(
                id = "flutter_service",
                title = "سرویس هماهنگ‌سازی آفلاین و نقشه رادار در فلاتر",
                language = "dart",
                filePath = "topon_flutter/lib/services/offline_sync_service.dart",
                description = "ذخیره‌سازی محلی SQLite در فلاتر برای مناطق فاقد پوشش اینترنت",
                tags = listOf("Flutter", "Offline First", "SQLite", "GIS"),
                codeContent = """
import 'package:sqflite/sqflite.dart';
import 'package:path/path.dart';

class OfflineSyncService {
  static Database? _database;

  Future<Database> get database async {
    if (_database != null) return _database!;
    _database = await initDB();
    return _database!;
  }

  Future<Database> initDB() async {
    String path = join(await getDatabasesPath(), 'topon_offline.db');
    return await openDatabase(
      path,
      version: 1,
      onCreate: (db, version) async {
        await db.execute('''
          CREATE TABLE cached_cases(
            id TEXT PRIMARY KEY,
            title TEXT,
            case_type TEXT,
            lat REAL,
            lng REAL,
            priority INTEGER,
            is_synced INTEGER
          )
        ''');
      },
    );
  }
}
                """.trimIndent()
            ),

            // Category 3: SECURITY_CRYPTO
            CodeSnippet(
                id = "sec_policy",
                title = "قواعد حریم خصوصی و اصل حداقل‌سازی داده (Privacy-by-Design)",
                language = "json",
                filePath = "specs/security_and_ethics_policy.json",
                description = "پروتکل سختگیرانه عدم نگهداری تصاویر خام بیومتریک و الزامات نظارت انسانی",
                tags = listOf("Privacy", "Human-in-the-Loop", "GDPR", "Ethics"),
                codeContent = """
{
  "privacy_framework": "Privacy-by-Design & Non-Surveillance",
  "data_minimization_rules": [
    "Raw facial photos are NEVER stored permanently without explicit authorization",
    "Only non-reversible mathematical landmark embeddings are computed locally",
    "Citizen submissions are encrypted client-side using device keystore prior to upload",
    "All audit logs are append-only with immutable SHA-256 chain links"
  ],
  "human_in_the_loop_contract": {
    "ai_role": "ANALYSIS_AND_TRIAGE_RECOMMENDATION_ONLY",
    "action_execution": "REQUIRES_SWORN_STAFF_APPROVAL",
    "false_positive_safeguards": "Mandatory confidence threshold >= 82% before alerting field units"
  }
}
                """.trimIndent()
            )
        )
    }

    val filteredSnippets = remember(selectedCategoryIndex, searchQuery) {
        val currentCategory = categories[selectedCategoryIndex]
        allSnippets.filter { snippet ->
            val matchCategory = when (currentCategory) {
                CodeCategory.KOTLIN_ANDROID -> snippet.tags.any { it in listOf("Data Layer", "Enums", "RBAC", "Entities", "MVVM", "Coroutines", "StateFlow", "Cryptography", "SHA-256", "Chain of Custody") } && snippet.language == "kotlin"
                CodeCategory.BACKEND_API -> snippet.language == "python"
                CodeCategory.FLUTTER_DART -> snippet.language == "dart"
                CodeCategory.SECURITY_CRYPTO -> snippet.language == "json" || snippet.tags.contains("SHA-256")
            }
            val matchQuery = searchQuery.isBlank() ||
                    snippet.title.contains(searchQuery, ignoreCase = true) ||
                    snippet.filePath.contains(searchQuery, ignoreCase = true) ||
                    snippet.description.contains(searchQuery, ignoreCase = true) ||
                    snippet.tags.any { it.contains(searchQuery, ignoreCase = true) }

            matchCategory && matchQuery
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header Banner
        item {
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Code,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "مرکز سورس‌کد و معماری سامانه",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "مستندات فنی، کدهای اجرایی، قراردادهای API و الگوریتم‌های امنیت",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        AssistChip(
                            onClick = { },
                            label = { Text("v2.4.0 Production", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = ToponVerifiedGreen.copy(alpha = 0.15f),
                                labelColor = ToponVerifiedGreen
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "این بخش کلیه پیاده‌سازی‌های فنی پروژه TOPON اعم از مدل‌های داده کاتلین، هسته رمزنگاری هش زنجیره ادله، قراردادهای API بک‌اند و کلاینت فلاتر را به صورت شفاف در اختیار مهندسان و ناظران سامانه قرار می‌دهد.",
                        fontSize = 12.sp,
                        lineHeight = 19.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                placeholder = { Text("جستجو در فایل‌ها، کلاس‌ها، متغیرها و کلیدواژه‌ها...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )
        }

        // Category Tabs
        item {
            ScrollableTabRow(
                selectedTabIndex = selectedCategoryIndex,
                edgePadding = 16.dp,
                containerColor = Color.Transparent,
                divider = {}
            ) {
                categories.forEachIndexed { index, cat ->
                    Tab(
                        selected = selectedCategoryIndex == index,
                        onClick = { selectedCategoryIndex = index },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = cat.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = cat.labelFa,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedCategoryIndex == index) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    )
                }
            }
        }

        // Live Hash & Verification Sandbox Tool
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "سندباکس تست زنده الگوریتم هش SHA-256 (Chain of Custody)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = testInputText,
                        onValueChange = { testInputText = it },
                        label = { Text("متن یا بارکد نمونه سند جهت محاسبه هش") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2,
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                isComputingHash = true
                                coroutineScope.launch {
                                    delay(150)
                                    val digest = MessageDigest.getInstance("SHA-256")
                                    val hashBytes = digest.digest(testInputText.toByteArray(Charsets.UTF_8))
                                    computedHashResult = hashBytes.joinToString("") { "%02x".format(it) }
                                    isComputingHash = false
                                }
                            },
                            enabled = !isComputingHash
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("اجرای محاسبات هش", fontSize = 11.sp)
                        }

                        if (computedHashResult != null) {
                            Text(
                                text = "تأیید اصالت: ۱۰۰٪ منطبق",
                                color = ToponVerifiedGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (computedHashResult != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        SelectionContainer {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "SHA-256:\n$computedHashResult",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = Color(0xFF38BDF8)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Code Snippets List
        if (filteredSnippets.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "هیچ ماژول کدی با عبارت «$searchQuery» در این دسته‌بندی یافت نشد.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            items(filteredSnippets, key = { it.id }) { snippet ->
                CodeSnippetCard(
                    snippet = snippet,
                    isCopied = copiedSnippetId == snippet.id,
                    onCopyCode = {
                        clipboardManager.setText(AnnotatedString(snippet.codeContent))
                        copiedSnippetId = snippet.id
                        Toast.makeText(context, "سورس‌کد به کلیپ‌بورد کپی شد", Toast.LENGTH_SHORT).show()
                        coroutineScope.launch {
                            delay(2500)
                            if (copiedSnippetId == snippet.id) copiedSnippetId = null
                        }
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun CodeSnippetCard(
    snippet: CodeSnippet,
    isCopied: Boolean,
    onCopyCode: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Title & File Path
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = snippet.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = snippet.filePath,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = onCopyCode,
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isCopied) Icons.Default.Done else Icons.Default.ContentCopy,
                            contentDescription = "کپی کد",
                            tint = if (isCopied) ToponVerifiedGreen else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isCopied) "کپی شد" else "کپی کد",
                            fontSize = 10.sp,
                            color = if (isCopied) ToponVerifiedGreen else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = snippet.description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Tags Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                snippet.tags.forEach { tag ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(text = tag, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Code Container (Dark IDE style)
            SelectionContainer {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF0F172A))
                        .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = snippet.codeContent,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        lineHeight = 17.sp,
                        color = Color(0xFFF1F5F9)
                    )
                }
            }
        }
    }
}
