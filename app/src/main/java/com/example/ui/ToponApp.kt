package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FamilyRestroom
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.UserRole
import com.example.ui.components.NotificationCenterDialog
import com.example.ui.components.SettingsDialog
import com.example.ui.screens.AuditLogScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.BiometricScannerScreen
import com.example.ui.screens.CasesDashboardScreen
import com.example.ui.screens.EvidenceVaultScreen
import com.example.ui.screens.FamilyPortalScreen
import com.example.ui.screens.IncidentReportingScreen
import com.example.ui.screens.OfflineRescueMapScreen
import com.example.ui.screens.PrivateChatScreen
import com.example.ui.screens.RescueChannelsScreen
import com.example.ui.screens.RewardNetworkScreen
import com.example.ui.screens.SocialMediaMonitorScreen
import com.example.ui.screens.SourceCodeExplorerScreen
import com.example.ui.screens.StaffReviewDashboard
import com.example.ui.screens.WantedPersonsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ToponAlertCoral
import com.example.ui.theme.ToponVerifiedGreen
import com.example.ui.viewmodel.ToponScreen
import com.example.ui.viewmodel.ToponViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToponApp(
    viewModel: ToponViewModel = viewModel()
) {
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val accentTheme by viewModel.accentTheme.collectAsStateWithLifecycle()
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val isVoiceEnabled by viewModel.isVoiceAccessibilityEnabled.collectAsStateWithLifecycle()
    val voiceStatus by viewModel.voiceAssistantStatus.collectAsStateWithLifecycle()
    val emergencyAlert by viewModel.emergencyAlertDispatched.collectAsStateWithLifecycle()

    // Auth & Role
    val isUserLoggedIn by viewModel.isUserLoggedIn.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val authSubScreen by viewModel.authSubScreen.collectAsStateWithLifecycle()
    val authLoading by viewModel.authLoading.collectAsStateWithLifecycle()
    val authErrorMessage by viewModel.authErrorMessage.collectAsStateWithLifecycle()
    val authSuccessMessage by viewModel.authSuccessMessage.collectAsStateWithLifecycle()
    val mfaRequired by viewModel.mfaRequired.collectAsStateWithLifecycle()
    val pendingMfaRole by viewModel.pendingMfaRole.collectAsStateWithLifecycle()
    val rateLimitSeconds by viewModel.rateLimitSeconds.collectAsStateWithLifecycle()

    // Data States
    val cases by viewModel.filteredCases.collectAsStateWithLifecycle()
    val allCases by viewModel.allCases.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCondition by viewModel.selectedCondition.collectAsStateWithLifecycle()
    val selectedCase by viewModel.selectedCase.collectAsStateWithLifecycle()

    // Biometrics & AI
    val scannedBitmap by viewModel.scannedBitmap.collectAsStateWithLifecycle()
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    val faceAnalysisResult by viewModel.faceAnalysisResult.collectAsStateWithLifecycle()
    val hasAudioSample by viewModel.hasAudioSample.collectAsStateWithLifecycle()

    // Reports & Staff Review
    val reports by viewModel.reports.collectAsStateWithLifecycle()
    val auditLogs by viewModel.auditLogs.collectAsStateWithLifecycle()

    // Map, Channels & Rewards
    val offlineZones by viewModel.offlineZones.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val sightings by viewModel.sightings.collectAsStateWithLifecycle()

    // Notifications
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val unreadNotificationsCount by viewModel.unreadNotificationsCount.collectAsStateWithLifecycle()

    var showSettingsDialog by remember { mutableStateOf(false) }
    var showNotificationDialog by remember { mutableStateOf(false) }
    var showRoleMenu by remember { mutableStateOf(false) }

    MyApplicationTheme(
        darkTheme = isDarkMode,
        accentTheme = accentTheme
    ) {
        // Set layout direction to RTL for native Persian presentation
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            if (!isUserLoggedIn) {
                // Show Auth Flow
                AuthScreen(
                    isDarkMode = isDarkMode,
                    authSubScreen = authSubScreen,
                    authLoading = authLoading,
                    authErrorMessage = authErrorMessage,
                    authSuccessMessage = authSuccessMessage,
                    mfaRequired = mfaRequired,
                    pendingMfaRole = pendingMfaRole,
                    rateLimitSeconds = rateLimitSeconds,
                    onToggleDarkMode = viewModel::toggleDarkMode,
                    onSetSubScreen = viewModel::setAuthSubScreen,
                    onClearMessages = viewModel::clearAuthMessages,
                    onLogin = viewModel::login,
                    onVerifyMfa = viewModel::verifyMfa,
                    onCancelMfa = viewModel::cancelMfa,
                    onRegister = viewModel::registerUser,
                    onRequestReset = viewModel::requestPasswordReset,
                    onConfirmReset = viewModel::confirmPasswordReset
                )
            } else {
                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(9.dp)
                                            .clip(CircleShape)
                                            .background(ToponVerifiedGreen)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "تاپان • TOPON",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "شبکه هوشمند امداد",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            },
                            navigationIcon = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val userRole = currentUser.role
                                    val roleColor = Color(userRole.badgeColorHex)
                                    Box(
                                        modifier = Modifier
                                            .padding(start = 10.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(roleColor.copy(alpha = 0.15f))
                                            .clickable { showRoleMenu = true }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(roleColor)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = userRole.labelFa,
                                                color = roleColor,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        DropdownMenu(
                                            expanded = showRoleMenu,
                                            onDismissRequest = { showRoleMenu = false }
                                        ) {
                                            UserRole.entries.forEach { role ->
                                                DropdownMenuItem(
                                                    text = {
                                                        Text(
                                                            text = "تغییر به نقش: ${role.labelFa}",
                                                            fontSize = 12.sp,
                                                            fontWeight = if (userRole == role) FontWeight.Bold else FontWeight.Normal
                                                        )
                                                    },
                                                    onClick = {
                                                        viewModel.switchRole(role)
                                                        showRoleMenu = false
                                                    }
                                                )
                                            }

                                            DropdownMenuItem(
                                                text = {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(Icons.Default.Logout, contentDescription = null, tint = ToponAlertCoral, modifier = Modifier.size(16.dp))
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text("خروج از حساب", color = ToponAlertCoral, fontSize = 12.sp)
                                                    }
                                                },
                                                onClick = {
                                                    viewModel.logout()
                                                    showRoleMenu = false
                                                }
                                            )
                                        }
                                    }

                                    if (isVoiceEnabled) {
                                        Box(
                                            modifier = Modifier
                                                .padding(start = 6.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primaryContainer)
                                                .padding(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.RecordVoiceOver,
                                                contentDescription = "دسترسی صوتی",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            },
                            actions = {
                                // Notification Center Bell with Badge
                                IconButton(onClick = { showNotificationDialog = true }) {
                                    if (unreadNotificationsCount > 0) {
                                        BadgedBox(
                                            badge = {
                                                Box(
                                                    modifier = Modifier
                                                        .size(16.dp)
                                                        .clip(CircleShape)
                                                        .background(ToponAlertCoral),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = unreadNotificationsCount.toString(),
                                                        color = Color.White,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Notifications,
                                                contentDescription = "اعلان‌ها",
                                                tint = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Notifications,
                                            contentDescription = "اعلان‌ها",
                                            tint = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                IconButton(onClick = { viewModel.navigateTo(ToponScreen.SOURCE_CODE_EXPLORER) }) {
                                    Icon(
                                        imageVector = Icons.Default.Code,
                                        contentDescription = "سورس‌کد و معماری",
                                        tint = if (currentScreen == ToponScreen.SOURCE_CODE_EXPLORER) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                IconButton(onClick = { showSettingsDialog = true }) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "تنظیمات",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    },
                    bottomBar = {
                        val activeRole = currentUser.role
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 8.dp
                        ) {
                            when (activeRole) {
                                UserRole.CITIZEN -> {
                                    NavigationBarItem(
                                        selected = currentScreen == ToponScreen.CASES_DASHBOARD,
                                        onClick = { viewModel.navigateTo(ToponScreen.CASES_DASHBOARD) },
                                        icon = { Icon(Icons.Default.ViewList, contentDescription = null) },
                                        label = { Text("پرونده‌ها", fontSize = 10.sp) },
                                        colors = NavigationBarItemDefaults.colors(selectedIconColor = MaterialTheme.colorScheme.primary, indicatorColor = MaterialTheme.colorScheme.primaryContainer)
                                    )
                                    NavigationBarItem(
                                        selected = currentScreen == ToponScreen.BIOMETRIC_SCANNER,
                                        onClick = { viewModel.navigateTo(ToponScreen.BIOMETRIC_SCANNER) },
                                        icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = null) },
                                        label = { Text("اسکنر چهره", fontSize = 10.sp) },
                                        colors = NavigationBarItemDefaults.colors(selectedIconColor = MaterialTheme.colorScheme.primary, indicatorColor = MaterialTheme.colorScheme.primaryContainer)
                                    )
                                    NavigationBarItem(
                                        selected = currentScreen == ToponScreen.OFFLINE_MAP,
                                        onClick = { viewModel.navigateTo(ToponScreen.OFFLINE_MAP) },
                                        icon = { Icon(Icons.Default.Map, contentDescription = null) },
                                        label = { Text("نقشه و رادار", fontSize = 10.sp) },
                                        colors = NavigationBarItemDefaults.colors(selectedIconColor = MaterialTheme.colorScheme.primary, indicatorColor = MaterialTheme.colorScheme.primaryContainer)
                                    )
                                    NavigationBarItem(
                                        selected = currentScreen == ToponScreen.RESCUE_CHANNELS,
                                        onClick = { viewModel.navigateTo(ToponScreen.RESCUE_CHANNELS) },
                                        icon = { Icon(Icons.Default.Forum, contentDescription = null) },
                                        label = { Text("گفتگوها", fontSize = 10.sp) },
                                        colors = NavigationBarItemDefaults.colors(selectedIconColor = MaterialTheme.colorScheme.primary, indicatorColor = MaterialTheme.colorScheme.primaryContainer)
                                    )
                                    NavigationBarItem(
                                        selected = currentScreen == ToponScreen.REWARD_NETWORK,
                                        onClick = { viewModel.navigateTo(ToponScreen.REWARD_NETWORK) },
                                        icon = { Icon(Icons.Default.VolunteerActivism, contentDescription = null) },
                                        label = { Text("صندوق پاداش", fontSize = 10.sp) },
                                        colors = NavigationBarItemDefaults.colors(selectedIconColor = MaterialTheme.colorScheme.primary, indicatorColor = MaterialTheme.colorScheme.primaryContainer)
                                    )
                                }

                                UserRole.FAMILY -> {
                                    NavigationBarItem(
                                        selected = currentScreen == ToponScreen.FAMILY_PORTAL,
                                        onClick = { viewModel.navigateTo(ToponScreen.FAMILY_PORTAL) },
                                        icon = { Icon(Icons.Default.FamilyRestroom, contentDescription = null) },
                                        label = { Text("ثبت و پیگیری", fontSize = 10.sp) },
                                        colors = NavigationBarItemDefaults.colors(selectedIconColor = MaterialTheme.colorScheme.primary, indicatorColor = MaterialTheme.colorScheme.primaryContainer)
                                    )
                                    NavigationBarItem(
                                        selected = currentScreen == ToponScreen.CASES_DASHBOARD,
                                        onClick = { viewModel.navigateTo(ToponScreen.CASES_DASHBOARD) },
                                        icon = { Icon(Icons.Default.ViewList, contentDescription = null) },
                                        label = { Text("پرونده‌ها", fontSize = 10.sp) },
                                        colors = NavigationBarItemDefaults.colors(selectedIconColor = MaterialTheme.colorScheme.primary, indicatorColor = MaterialTheme.colorScheme.primaryContainer)
                                    )
                                    NavigationBarItem(
                                        selected = currentScreen == ToponScreen.BIOMETRIC_SCANNER,
                                        onClick = { viewModel.navigateTo(ToponScreen.BIOMETRIC_SCANNER) },
                                        icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = null) },
                                        label = { Text("اسکنر", fontSize = 10.sp) },
                                        colors = NavigationBarItemDefaults.colors(selectedIconColor = MaterialTheme.colorScheme.primary, indicatorColor = MaterialTheme.colorScheme.primaryContainer)
                                    )
                                    NavigationBarItem(
                                        selected = currentScreen == ToponScreen.OFFLINE_MAP,
                                        onClick = { viewModel.navigateTo(ToponScreen.OFFLINE_MAP) },
                                        icon = { Icon(Icons.Default.Map, contentDescription = null) },
                                        label = { Text("رادار", fontSize = 10.sp) },
                                        colors = NavigationBarItemDefaults.colors(selectedIconColor = MaterialTheme.colorScheme.primary, indicatorColor = MaterialTheme.colorScheme.primaryContainer)
                                    )
                                    NavigationBarItem(
                                        selected = currentScreen == ToponScreen.RESCUE_CHANNELS,
                                        onClick = { viewModel.navigateTo(ToponScreen.RESCUE_CHANNELS) },
                                        icon = { Icon(Icons.Default.Forum, contentDescription = null) },
                                        label = { Text("گفتگوها", fontSize = 10.sp) },
                                        colors = NavigationBarItemDefaults.colors(selectedIconColor = MaterialTheme.colorScheme.primary, indicatorColor = MaterialTheme.colorScheme.primaryContainer)
                                    )
                                }

                                UserRole.AUTHORIZED_STAFF -> {
                                    NavigationBarItem(
                                        selected = currentScreen == ToponScreen.STAFF_MANAGEMENT,
                                        onClick = { viewModel.navigateTo(ToponScreen.STAFF_MANAGEMENT) },
                                        icon = { Icon(Icons.Default.Shield, contentDescription = null) },
                                        label = { Text("بررسی سرنخ‌ها", fontSize = 10.sp) },
                                        colors = NavigationBarItemDefaults.colors(selectedIconColor = MaterialTheme.colorScheme.primary, indicatorColor = MaterialTheme.colorScheme.primaryContainer)
                                    )
                                    NavigationBarItem(
                                        selected = currentScreen == ToponScreen.CASES_DASHBOARD,
                                        onClick = { viewModel.navigateTo(ToponScreen.CASES_DASHBOARD) },
                                        icon = { Icon(Icons.Default.ViewList, contentDescription = null) },
                                        label = { Text("پرونده‌ها", fontSize = 10.sp) },
                                        colors = NavigationBarItemDefaults.colors(selectedIconColor = MaterialTheme.colorScheme.primary, indicatorColor = MaterialTheme.colorScheme.primaryContainer)
                                    )
                                    NavigationBarItem(
                                        selected = currentScreen == ToponScreen.OFFLINE_MAP,
                                        onClick = { viewModel.navigateTo(ToponScreen.OFFLINE_MAP) },
                                        icon = { Icon(Icons.Default.Map, contentDescription = null) },
                                        label = { Text("نقشه امداد", fontSize = 10.sp) },
                                        colors = NavigationBarItemDefaults.colors(selectedIconColor = MaterialTheme.colorScheme.primary, indicatorColor = MaterialTheme.colorScheme.primaryContainer)
                                    )
                                    NavigationBarItem(
                                        selected = currentScreen == ToponScreen.RESCUE_CHANNELS,
                                        onClick = { viewModel.navigateTo(ToponScreen.RESCUE_CHANNELS) },
                                        icon = { Icon(Icons.Default.Forum, contentDescription = null) },
                                        label = { Text("اتاق عملیات", fontSize = 10.sp) },
                                        colors = NavigationBarItemDefaults.colors(selectedIconColor = MaterialTheme.colorScheme.primary, indicatorColor = MaterialTheme.colorScheme.primaryContainer)
                                    )
                                    NavigationBarItem(
                                        selected = currentScreen == ToponScreen.AUDIT_LOGS,
                                        onClick = { viewModel.navigateTo(ToponScreen.AUDIT_LOGS) },
                                        icon = { Icon(Icons.Default.Security, contentDescription = null) },
                                        label = { Text("ردپای امنیتی", fontSize = 10.sp) },
                                        colors = NavigationBarItemDefaults.colors(selectedIconColor = MaterialTheme.colorScheme.primary, indicatorColor = MaterialTheme.colorScheme.primaryContainer)
                                    )
                                }

                                UserRole.ADMIN -> {
                                    NavigationBarItem(
                                        selected = currentScreen == ToponScreen.STAFF_MANAGEMENT,
                                        onClick = { viewModel.navigateTo(ToponScreen.STAFF_MANAGEMENT) },
                                        icon = { Icon(Icons.Default.Shield, contentDescription = null) },
                                        label = { Text("مدیریت سرنخ‌ها", fontSize = 10.sp) },
                                        colors = NavigationBarItemDefaults.colors(selectedIconColor = MaterialTheme.colorScheme.primary, indicatorColor = MaterialTheme.colorScheme.primaryContainer)
                                    )
                                    NavigationBarItem(
                                        selected = currentScreen == ToponScreen.AUDIT_LOGS,
                                        onClick = { viewModel.navigateTo(ToponScreen.AUDIT_LOGS) },
                                        icon = { Icon(Icons.Default.Security, contentDescription = null) },
                                        label = { Text("گزارش وقایع", fontSize = 10.sp) },
                                        colors = NavigationBarItemDefaults.colors(selectedIconColor = MaterialTheme.colorScheme.primary, indicatorColor = MaterialTheme.colorScheme.primaryContainer)
                                    )
                                    NavigationBarItem(
                                        selected = currentScreen == ToponScreen.CASES_DASHBOARD,
                                        onClick = { viewModel.navigateTo(ToponScreen.CASES_DASHBOARD) },
                                        icon = { Icon(Icons.Default.ViewList, contentDescription = null) },
                                        label = { Text("پرونده‌ها", fontSize = 10.sp) },
                                        colors = NavigationBarItemDefaults.colors(selectedIconColor = MaterialTheme.colorScheme.primary, indicatorColor = MaterialTheme.colorScheme.primaryContainer)
                                    )
                                    NavigationBarItem(
                                        selected = currentScreen == ToponScreen.RESCUE_CHANNELS,
                                        onClick = { viewModel.navigateTo(ToponScreen.RESCUE_CHANNELS) },
                                        icon = { Icon(Icons.Default.Forum, contentDescription = null) },
                                        label = { Text("گفتگوها", fontSize = 10.sp) },
                                        colors = NavigationBarItemDefaults.colors(selectedIconColor = MaterialTheme.colorScheme.primary, indicatorColor = MaterialTheme.colorScheme.primaryContainer)
                                    )
                                    NavigationBarItem(
                                        selected = currentScreen == ToponScreen.REWARD_NETWORK,
                                        onClick = { viewModel.navigateTo(ToponScreen.REWARD_NETWORK) },
                                        icon = { Icon(Icons.Default.VolunteerActivism, contentDescription = null) },
                                        label = { Text("صندوق پاداش", fontSize = 10.sp) },
                                        colors = NavigationBarItemDefaults.colors(selectedIconColor = MaterialTheme.colorScheme.primary, indicatorColor = MaterialTheme.colorScheme.primaryContainer)
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Emergency Dispatch Top Banner
                            AnimatedVisibility(
                                visible = emergencyAlert != null,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(ToponAlertCoral)
                                        .padding(horizontal = 14.dp, vertical = 10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.NotificationsActive,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = emergencyAlert ?: "",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                lineHeight = 16.sp
                                            )
                                        }
                                        IconButton(
                                            onClick = { viewModel.dismissAlertBanner() },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "بستن",
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Voice Assistant Status Toast
                            if (isVoiceEnabled && voiceStatus != null) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .padding(horizontal = 14.dp, vertical = 4.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.RecordVoiceOver,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = voiceStatus ?: "",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 10.sp
                                        )
                                    }
                                }
                            }

                            // Quick Module Access Bar (Items 40-50)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surface)
                                    .horizontalScroll(rememberScrollState())
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                AssistChip(
                                    onClick = { viewModel.navigateTo(ToponScreen.INCIDENT_REPORT) },
                                    label = { Text("ثبت سانحه/تصادف", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    leadingIcon = {
                                        Icon(Icons.Default.Warning, contentDescription = null, tint = ToponAlertCoral, modifier = Modifier.size(16.dp))
                                    },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = if (currentScreen == ToponScreen.INCIDENT_REPORT) ToponAlertCoral.copy(alpha = 0.2f) else Color.Transparent
                                    )
                                )

                                AssistChip(
                                    onClick = { viewModel.navigateTo(ToponScreen.WANTED_PERSONS) },
                                    label = { Text("تحت تعقیب قضایی", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    leadingIcon = {
                                        Icon(Icons.Default.Gavel, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = if (currentScreen == ToponScreen.WANTED_PERSONS) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                                    )
                                )

                                AssistChip(
                                    onClick = { viewModel.navigateTo(ToponScreen.PRIVATE_CHAT) },
                                    label = { Text("گفتگوی امن E2EE", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    leadingIcon = {
                                        Icon(Icons.Default.Lock, contentDescription = null, tint = ToponVerifiedGreen, modifier = Modifier.size(16.dp))
                                    },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = if (currentScreen == ToponScreen.PRIVATE_CHAT) ToponVerifiedGreen.copy(alpha = 0.2f) else Color.Transparent
                                    )
                                )

                                AssistChip(
                                    onClick = { viewModel.navigateTo(ToponScreen.EVIDENCE_VAULT) },
                                    label = { Text("مخزن ادله و زنجیره", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    leadingIcon = {
                                        Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                                    },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = if (currentScreen == ToponScreen.EVIDENCE_VAULT) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent
                                    )
                                )

                                AssistChip(
                                    onClick = { viewModel.navigateTo(ToponScreen.SOCIAL_MONITOR) },
                                    label = { Text("پایشگر رسانه‌ها (OSINT)", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    leadingIcon = {
                                        Icon(Icons.Default.Language, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(16.dp))
                                    },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = if (currentScreen == ToponScreen.SOCIAL_MONITOR) MaterialTheme.colorScheme.tertiaryContainer else Color.Transparent
                                    )
                                )

                                AssistChip(
                                    onClick = { viewModel.navigateTo(ToponScreen.SOURCE_CODE_EXPLORER) },
                                    label = { Text("سورس‌کد و معماری", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                    leadingIcon = {
                                        Icon(Icons.Default.Code, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = if (currentScreen == ToponScreen.SOURCE_CODE_EXPLORER) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                                    )
                                )
                            }

                            // Screen Content Switching
                            when (currentScreen) {
                                ToponScreen.CASES_DASHBOARD -> {
                                    CasesDashboardScreen(
                                        cases = cases,
                                        searchQuery = searchQuery,
                                        selectedCondition = selectedCondition,
                                        onSearchChanged = viewModel::onSearchQueryChanged,
                                        onConditionSelected = viewModel::selectConditionFilter,
                                        onSelectCaseForScan = { caseItem ->
                                            viewModel.selectCase(caseItem)
                                            viewModel.navigateTo(ToponScreen.BIOMETRIC_SCANNER)
                                        },
                                        onNavigateToIncidents = {
                                            viewModel.navigateTo(ToponScreen.INCIDENT_REPORT)
                                        },
                                        onNavigateToWanted = {
                                            viewModel.navigateTo(ToponScreen.WANTED_PERSONS)
                                        },
                                        onNavigateToEvidence = {
                                            viewModel.navigateTo(ToponScreen.EVIDENCE_VAULT)
                                        },
                                        onNavigateToSourceCode = {
                                            viewModel.navigateTo(ToponScreen.SOURCE_CODE_EXPLORER)
                                        }
                                    )
                                }

                                ToponScreen.INCIDENT_REPORT -> {
                                    IncidentReportingScreen(
                                        viewModel = viewModel,
                                        onReportSubmitted = {
                                            viewModel.navigateTo(ToponScreen.CASES_DASHBOARD)
                                        }
                                    )
                                }

                                ToponScreen.WANTED_PERSONS -> {
                                    WantedPersonsScreen(
                                        viewModel = viewModel,
                                        onReportSighting = { _ ->
                                            viewModel.navigateTo(ToponScreen.BIOMETRIC_SCANNER)
                                        },
                                        onOpenEvidenceVault = { _ ->
                                            viewModel.navigateTo(ToponScreen.EVIDENCE_VAULT)
                                        }
                                    )
                                }

                                ToponScreen.PRIVATE_CHAT -> {
                                    PrivateChatScreen(
                                        viewModel = viewModel
                                    )
                                }

                                ToponScreen.EVIDENCE_VAULT -> {
                                    EvidenceVaultScreen(
                                        viewModel = viewModel
                                    )
                                }

                                ToponScreen.SOCIAL_MONITOR -> {
                                    SocialMediaMonitorScreen(
                                        viewModel = viewModel
                                    )
                                }

                                ToponScreen.BIOMETRIC_SCANNER -> {
                                    BiometricScannerScreen(
                                        cases = allCases,
                                        selectedCase = selectedCase,
                                        scannedBitmap = scannedBitmap,
                                        isScanning = isScanning,
                                        faceAnalysisResult = faceAnalysisResult,
                                        hasAudioSample = hasAudioSample,
                                        onCaseSelected = viewModel::selectCase,
                                        onBitmapCaptured = viewModel::setScannedBitmap,
                                        onToggleAudioSample = viewModel::toggleAudioSample,
                                        onStartScan = viewModel::startBiometricScan,
                                        onSubmitReport = viewModel::submitCitizenReport
                                    )
                                }

                                ToponScreen.OFFLINE_MAP -> {
                                    OfflineRescueMapScreen(
                                        cases = allCases,
                                        offlineZones = offlineZones,
                                        onToggleDownloadZone = viewModel::toggleDownloadOfflineZone,
                                        onCaseSelected = { c ->
                                            viewModel.selectCase(c)
                                            viewModel.navigateTo(ToponScreen.BIOMETRIC_SCANNER)
                                        }
                                    )
                                }

                                ToponScreen.RESCUE_CHANNELS -> {
                                    RescueChannelsScreen(
                                        cases = allCases,
                                        messages = messages,
                                        onSendMessage = viewModel::sendRescueMessage
                                    )
                                }

                                ToponScreen.REWARD_NETWORK -> {
                                    RewardNetworkScreen(
                                        sightings = sightings
                                    )
                                }

                                ToponScreen.STAFF_MANAGEMENT -> {
                                    StaffReviewDashboard(
                                        currentUser = currentUser,
                                        cases = allCases,
                                        reports = reports,
                                        onReviewReport = viewModel::reviewReport,
                                        onUpdateCaseStatus = viewModel::updateCaseStatus,
                                        onUpdateCasePriority = viewModel::updateCasePriority
                                    )
                                }

                                ToponScreen.FAMILY_PORTAL -> {
                                    FamilyPortalScreen(
                                        currentUser = currentUser,
                                        cases = allCases,
                                        reports = reports,
                                        onRegisterCase = viewModel::registerFamilyCase,
                                        onOpenChat = {
                                            viewModel.navigateTo(ToponScreen.RESCUE_CHANNELS)
                                        }
                                    )
                                }

                                ToponScreen.AUDIT_LOGS -> {
                                    AuditLogScreen(
                                        logs = auditLogs
                                    )
                                }

                                ToponScreen.SOURCE_CODE_EXPLORER -> {
                                    SourceCodeExplorerScreen()
                                }
                            }
                        }
                    }
                }

                // Notification Center Dialog
                if (showNotificationDialog) {
                    NotificationCenterDialog(
                        notifications = notifications,
                        onMarkRead = viewModel::markNotificationRead,
                        onDismiss = { showNotificationDialog = false }
                    )
                }

                // Settings & Theme Dialog
                if (showSettingsDialog) {
                    SettingsDialog(
                        isDarkMode = isDarkMode,
                        currentTheme = accentTheme,
                        isVoiceAccessibilityEnabled = isVoiceEnabled,
                        onToggleDarkMode = viewModel::toggleDarkMode,
                        onSelectAccentTheme = viewModel::setAccentTheme,
                        onToggleVoiceAccessibility = viewModel::toggleVoiceAccessibility,
                        onDismiss = { showSettingsDialog = false }
                    )
                }
            }
        }
    }
}
