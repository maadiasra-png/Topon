package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiAnalysisService
import com.example.data.auth.AuthResult
import com.example.data.auth.LoginRequest
import com.example.data.auth.PasswordResetConfirmRequest
import com.example.data.auth.RegisterRequest
import com.example.data.model.AuditLogItem
import com.example.data.model.CasePriority
import com.example.data.model.CitizenReport
import com.example.data.model.ConditionType
import com.example.data.model.FaceAnalysisResult
import com.example.data.model.MissingPersonCase
import com.example.data.model.NotificationItem
import com.example.data.model.NotificationPriority
import com.example.data.model.OfflineMapZone
import com.example.data.model.ReportStatus
import com.example.data.model.RescueChannelMessage
import com.example.data.model.SenderRole
import com.example.data.model.SightingReportItem
import com.example.data.model.User
import com.example.data.model.UserRole
import com.example.data.model.VerificationStatus
import com.example.data.model.CaseType
import com.example.data.model.AccidentType
import com.example.data.model.DisasterType
import com.example.data.model.EvidenceType
import com.example.data.model.EvidenceItem
import com.example.data.model.ChainOfCustodyEntry
import com.example.data.model.CasualtyAnalysisResult
import com.example.data.model.PrivateChatMessage
import com.example.data.model.SocialMediaLead
import com.example.data.model.ToponCase
import com.example.data.repository.ToponRepository
import com.example.ui.theme.ToponAccentTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ToponScreen(val title: String) {
    CASES_DASHBOARD("داشبورد و پرونده‌ها"),
    INCIDENT_REPORT("ثبت گزارش تصادف و سانحه"),
    WANTED_PERSONS("اشخاص تحت تعقیب"),
    PRIVATE_CHAT("گفتگوی امن و محرمانه"),
    EVIDENCE_VAULT("مخزن ادله و زنجیره اسناد"),
    SOCIAL_MONITOR("پایش رسانه‌های عمومی"),
    BIOMETRIC_SCANNER("اسکنر هوشمند"),
    OFFLINE_MAP("نقشه و رادار"),
    RESCUE_CHANNELS("شبکه امدادی"),
    STAFF_MANAGEMENT("پنل کارشناسی و بررسی"),
    FAMILY_PORTAL("مدیریت پرونده خانواده"),
    REWARD_NETWORK("پاداش و مشارکت"),
    AUDIT_LOGS("گزارش وقایع و امنیت"),
    SOURCE_CODE_EXPLORER("سورس‌کد و معماری سیستم")
}

enum class AuthSubScreen {
    LOGIN,
    REGISTER,
    FORGOT_PASSWORD,
    RESET_PASSWORD,
    MFA_VERIFY
}

class ToponViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ToponRepository(application)
    private val geminiService = GeminiAnalysisService()

    // Auth & User State
    val currentUser: StateFlow<User> = repository.currentUser

    private val _isUserLoggedIn = MutableStateFlow(false) // First screen must be Login
    val isUserLoggedIn = _isUserLoggedIn.asStateFlow()

    private val _authSubScreen = MutableStateFlow(AuthSubScreen.LOGIN)
    val authSubScreen = _authSubScreen.asStateFlow()

    private val _authLoading = MutableStateFlow(false)
    val authLoading = _authLoading.asStateFlow()

    private val _authErrorMessage = MutableStateFlow<String?>(null)
    val authErrorMessage = _authErrorMessage.asStateFlow()

    private val _authSuccessMessage = MutableStateFlow<String?>(null)
    val authSuccessMessage = _authSuccessMessage.asStateFlow()

    private val _mfaRequired = MutableStateFlow(false)
    val mfaRequired = _mfaRequired.asStateFlow()

    private val _pendingMfaUserId = MutableStateFlow<String?>(null)
    val pendingMfaUserId = _pendingMfaUserId.asStateFlow()

    private val _pendingMfaRole = MutableStateFlow<UserRole?>(null)
    val pendingMfaRole = _pendingMfaRole.asStateFlow()

    private val _rateLimitSeconds = MutableStateFlow(0)
    val rateLimitSeconds = _rateLimitSeconds.asStateFlow()

    // Navigation & UI State
    private val _currentScreen = MutableStateFlow(ToponScreen.CASES_DASHBOARD)
    val currentScreen = _currentScreen.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCondition = MutableStateFlow<ConditionType?>(null)
    val selectedCondition = _selectedCondition.asStateFlow()

    private val _selectedPriority = MutableStateFlow<CasePriority?>(null)
    val selectedPriority = _selectedPriority.asStateFlow()

    private val _selectedCase = MutableStateFlow<MissingPersonCase?>(null)
    val selectedCase = _selectedCase.asStateFlow()

    // Settings State
    private val _isDarkMode = MutableStateFlow(true) // Modern eye-safe dark mode
    val isDarkMode = _isDarkMode.asStateFlow()

    private val _accentTheme = MutableStateFlow(ToponAccentTheme.RESCUE_BLUE)
    val accentTheme = _accentTheme.asStateFlow()

    private val _isVoiceAccessibilityEnabled = MutableStateFlow(true)
    val isVoiceAccessibilityEnabled = _isVoiceAccessibilityEnabled.asStateFlow()

    private val _voiceAssistantStatus = MutableStateFlow<String?>("سامانه آماده پایش و دریافت فرمان صوتی است.")
    val voiceAssistantStatus = _voiceAssistantStatus.asStateFlow()

    // Scanner & Face Analysis State
    private val _scannedBitmap = MutableStateFlow<Bitmap?>(null)
    val scannedBitmap = _scannedBitmap.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning = _isScanning.asStateFlow()

    private val _faceAnalysisResult = MutableStateFlow<FaceAnalysisResult?>(null)
    val faceAnalysisResult = _faceAnalysisResult.asStateFlow()

    private val _hasAudioSample = MutableStateFlow(false)
    val hasAudioSample = _hasAudioSample.asStateFlow()

    private val _emergencyAlertDispatched = MutableStateFlow<String?>(null)
    val emergencyAlertDispatched = _emergencyAlertDispatched.asStateFlow()

    // Filtered Cases by Search, Condition & Priority
    val filteredCases: StateFlow<List<MissingPersonCase>> = combine(
        repository.cases,
        _searchQuery,
        _selectedCondition,
        _selectedPriority
    ) { allCases, query, condition, priority ->
        allCases.filter { caseItem ->
            val matchesQuery = query.isBlank() ||
                    caseItem.fullName.contains(query, ignoreCase = true) ||
                    caseItem.lastSeenLocation.contains(query, ignoreCase = true) ||
                    caseItem.caseNumber.contains(query, ignoreCase = true)

            val matchesCondition = condition == null || caseItem.conditionType == condition
            val matchesPriority = priority == null || caseItem.priority == priority

            matchesQuery && matchesCondition && matchesPriority
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All Cases
    val allCases: StateFlow<List<MissingPersonCase>> = repository.cases

    // High Priority Cases
    val criticalCases: StateFlow<List<MissingPersonCase>> = repository.cases.combine(MutableStateFlow(Unit)) { cases, _ ->
        cases.filter { it.priority == CasePriority.CRITICAL }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Reports from Room
    val reports: StateFlow<List<CitizenReport>> = repository.allReports
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // User's own reports (for Citizen role)
    val userReports: StateFlow<List<CitizenReport>> = combine(
        repository.allReports,
        repository.currentUser
    ) { allReports, user ->
        allReports.filter { it.reporterUserId == user.id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Audit logs from Room (for Staff / Admin)
    val auditLogs: StateFlow<List<AuditLogItem>> = repository.auditLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Sightings / Reward items derived from reports & cases
    val sightings: StateFlow<List<SightingReportItem>> = combine(
        repository.allReports,
        repository.cases
    ) { reportsList, casesList ->
        reportsList.map { r ->
            val caseItem = casesList.find { it.id == r.caseId }
            val rewardShare = ((caseItem?.rewardAmountToman ?: 20_000_000L) * (r.potentialMatchScore ?: 75) / 100.0 * 0.4).toLong()
            SightingReportItem(
                id = r.id,
                caseId = r.caseId,
                personName = r.reportedPersonName,
                locationName = r.locationName,
                citizenNotes = r.citizenNotes,
                similarityScore = r.potentialMatchScore ?: 75,
                estimatedRewardShareToman = rewardShare.coerceAtLeast(1_000_000L),
                timestamp = r.timestamp
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Family cases
    val familyCases: StateFlow<List<MissingPersonCase>> = combine(
        repository.cases,
        repository.currentUser
    ) { casesList, user ->
        casesList.filter { it.registeredByUserId == user.id || it.id == "case-01" }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Notifications from Room
    val notifications: StateFlow<List<NotificationItem>> = repository.notifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotificationsCount: StateFlow<Int> = repository.notifications.combine(MutableStateFlow(Unit)) { list, _ ->
        list.count { !it.isRead }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Chat messages
    val messages: StateFlow<List<RescueChannelMessage>> = repository.messages

    // Offline zones
    val offlineZones: StateFlow<List<OfflineMapZone>> = repository.offlineZones

    init {
        viewModelScope.launch {
            repository.cases.collect { cases ->
                if (_selectedCase.value == null && cases.isNotEmpty()) {
                    _selectedCase.value = cases.first()
                }
            }
        }
    }

    // Navigation with Route-level Protection
    fun navigateTo(screen: ToponScreen) {
        if (!_isUserLoggedIn.value) {
            _authErrorMessage.value = "دسترسی غیرمجاز: لطفاً ابتدا وارد حساب کاربری خود شوید."
            return
        }

        val role = currentUser.value.role
        val allowed = repository.authService.isRouteAllowed(role, screen.name)
        if (allowed) {
            _currentScreen.value = screen
        } else {
            viewModelScope.launch {
                repository.dispatchNotification(
                    title = "عدم دسترسی به بخش ${screen.title}",
                    message = "دسترسی به بخش «${screen.title}» نیازمند مجوز سازمانی معتبر است.",
                    priority = NotificationPriority.HIGH,
                    targetRole = role
                )
            }
        }
    }

    fun setAuthSubScreen(subScreen: AuthSubScreen) {
        _authSubScreen.value = subScreen
        _authErrorMessage.value = null
        _authSuccessMessage.value = null
    }

    fun clearAuthMessages() {
        _authErrorMessage.value = null
        _authSuccessMessage.value = null
    }

    // Auth & Roles
    fun switchRole(role: UserRole) {
        viewModelScope.launch {
            repository.switchUserRole(role)
            // Route user to appropriate screen
            when (role) {
                UserRole.AUTHORIZED_STAFF -> _currentScreen.value = ToponScreen.STAFF_MANAGEMENT
                UserRole.FAMILY -> _currentScreen.value = ToponScreen.FAMILY_PORTAL
                UserRole.ADMIN -> _currentScreen.value = ToponScreen.AUDIT_LOGS
                UserRole.CITIZEN -> _currentScreen.value = ToponScreen.CASES_DASHBOARD
            }
        }
    }

    fun login(identifier: String, password: String, rememberMe: Boolean = true) {
        viewModelScope.launch {
            _authLoading.value = true
            _authErrorMessage.value = null
            _authSuccessMessage.value = null

            val result = repository.authService.login(
                LoginRequest(
                    identifier = identifier,
                    passwordPlain = password,
                    rememberMe = rememberMe
                )
            )

            _authLoading.value = false

            when (result) {
                is AuthResult.Success -> {
                    repository.setCurrentUser(result.session.user)
                    _isUserLoggedIn.value = true
                    _mfaRequired.value = false
                    _authSubScreen.value = AuthSubScreen.LOGIN
                    _rateLimitSeconds.value = 0
                    // Navigate to role-specific dashboard
                    when (result.session.user.role) {
                        UserRole.CITIZEN -> _currentScreen.value = ToponScreen.CASES_DASHBOARD
                        UserRole.FAMILY -> _currentScreen.value = ToponScreen.FAMILY_PORTAL
                        UserRole.AUTHORIZED_STAFF -> _currentScreen.value = ToponScreen.STAFF_MANAGEMENT
                        UserRole.ADMIN -> _currentScreen.value = ToponScreen.AUDIT_LOGS
                    }
                }
                is AuthResult.MfaRequired -> {
                    _mfaRequired.value = true
                    _pendingMfaUserId.value = result.pendingUserId
                    _pendingMfaRole.value = result.userRole
                    _authErrorMessage.value = null
                }
                is AuthResult.Error -> {
                    _authErrorMessage.value = result.message
                    if (result.isRateLimited) {
                        startRateLimitCountdown(result.retryAfterSeconds)
                    }
                }
            }
        }
    }

    fun verifyMfa(code: String, rememberMe: Boolean = true) {
        val userId = _pendingMfaUserId.value ?: return
        viewModelScope.launch {
            _authLoading.value = true
            _authErrorMessage.value = null

            val result = repository.authService.verifyMfa(
                userId = userId,
                otpCode = code,
                rememberMe = rememberMe
            )

            _authLoading.value = false

            when (result) {
                is AuthResult.Success -> {
                    repository.setCurrentUser(result.session.user)
                    _isUserLoggedIn.value = true
                    _mfaRequired.value = false
                    _pendingMfaUserId.value = null
                    _pendingMfaRole.value = null

                    when (result.session.user.role) {
                        UserRole.AUTHORIZED_STAFF -> _currentScreen.value = ToponScreen.STAFF_MANAGEMENT
                        UserRole.ADMIN -> _currentScreen.value = ToponScreen.AUDIT_LOGS
                        UserRole.FAMILY -> _currentScreen.value = ToponScreen.FAMILY_PORTAL
                        UserRole.CITIZEN -> _currentScreen.value = ToponScreen.CASES_DASHBOARD
                    }
                }
                is AuthResult.Error -> {
                    _authErrorMessage.value = result.message
                }
                is AuthResult.MfaRequired -> {
                    _authErrorMessage.value = result.message
                }
            }
        }
    }

    fun cancelMfa() {
        _mfaRequired.value = false
        _pendingMfaUserId.value = null
        _pendingMfaRole.value = null
        _authErrorMessage.value = null
    }

    fun registerUser(
        fullName: String,
        username: String,
        email: String,
        phone: String,
        passwordPlain: String,
        role: UserRole,
        organization: String?,
        authorizationCode: String?
    ) {
        viewModelScope.launch {
            _authLoading.value = true
            _authErrorMessage.value = null
            _authSuccessMessage.value = null

            val result = repository.authService.register(
                RegisterRequest(
                    fullName = fullName,
                    username = username,
                    email = email,
                    phone = phone,
                    passwordPlain = passwordPlain,
                    role = role,
                    organization = organization,
                    authorizationCode = authorizationCode
                )
            )

            _authLoading.value = false

            when (result) {
                is AuthResult.Success -> {
                    repository.setCurrentUser(result.session.user)
                    _isUserLoggedIn.value = true
                    _authSubScreen.value = AuthSubScreen.LOGIN
                    when (result.session.user.role) {
                        UserRole.CITIZEN -> _currentScreen.value = ToponScreen.CASES_DASHBOARD
                        UserRole.FAMILY -> _currentScreen.value = ToponScreen.FAMILY_PORTAL
                        UserRole.AUTHORIZED_STAFF -> _currentScreen.value = ToponScreen.STAFF_MANAGEMENT
                        UserRole.ADMIN -> _currentScreen.value = ToponScreen.AUDIT_LOGS
                    }
                }
                is AuthResult.Error -> {
                    _authErrorMessage.value = result.message
                }
                is AuthResult.MfaRequired -> {
                    _mfaRequired.value = true
                    _pendingMfaUserId.value = result.pendingUserId
                    _pendingMfaRole.value = result.userRole
                }
            }
        }
    }

    fun requestPasswordReset(identifier: String) {
        viewModelScope.launch {
            _authLoading.value = true
            _authErrorMessage.value = null
            val result = repository.authService.requestPasswordReset(identifier)
            _authLoading.value = false
            result.onSuccess { otpCode ->
                _authSuccessMessage.value = "کد تأیید بازیابی به اطلاعات تماس ارسال شد (کد تستی دمو: $otpCode)."
                _authSubScreen.value = AuthSubScreen.RESET_PASSWORD
            }.onFailure {
                _authErrorMessage.value = it.message ?: "خطا در درخواست بازیابی رمز عبور."
            }
        }
    }

    fun confirmPasswordReset(identifier: String, resetCode: String, newPasswordPlain: String) {
        viewModelScope.launch {
            _authLoading.value = true
            _authErrorMessage.value = null
            val result = repository.authService.confirmPasswordReset(
                PasswordResetConfirmRequest(
                    identifier = identifier,
                    resetCode = resetCode,
                    newPasswordPlain = newPasswordPlain
                )
            )
            _authLoading.value = false
            result.onSuccess {
                _authSuccessMessage.value = "رمز عبور جدید با موفقیت تنظیم شد. اکنون می‌توانید وارد شوید."
                _authSubScreen.value = AuthSubScreen.LOGIN
            }.onFailure {
                _authErrorMessage.value = it.message ?: "خطا در تنظیم رمز عبور."
            }
        }
    }

    private fun startRateLimitCountdown(seconds: Int) {
        viewModelScope.launch {
            var rem = seconds
            _rateLimitSeconds.value = rem
            while (rem > 0) {
                delay(1000L)
                rem--
                _rateLimitSeconds.value = rem
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.authService.logout(null)
            _isUserLoggedIn.value = false
            _authSubScreen.value = AuthSubScreen.LOGIN
            _authErrorMessage.value = null
            _authSuccessMessage.value = "با موفقیت از حساب کاربری خارج شدید."
            _currentScreen.value = ToponScreen.CASES_DASHBOARD
        }
    }

    // Search & Filter
    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun selectConditionFilter(condition: ConditionType?) {
        _selectedCondition.value = condition
    }

    fun selectPriorityFilter(priority: CasePriority?) {
        _selectedPriority.value = priority
    }

    fun selectCase(caseItem: MissingPersonCase) {
        _selectedCase.value = caseItem
    }

    // Appearance & Accessibility
    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun setAccentTheme(theme: ToponAccentTheme) {
        _accentTheme.value = theme
    }

    fun toggleVoiceAccessibility() {
        _isVoiceAccessibilityEnabled.value = !_isVoiceAccessibilityEnabled.value
        _voiceAssistantStatus.value = if (_isVoiceAccessibilityEnabled.value) {
            "دسترسی صوتی و راهنمای گفتاری فعال گردید."
        } else {
            "دسترسی صوتی موقتاً غیرفعال شد."
        }
    }

    // Face / Image Scanning
    fun setScannedBitmap(bitmap: Bitmap?) {
        _scannedBitmap.value = bitmap
        _faceAnalysisResult.value = null
    }

    fun toggleAudioSample() {
        _hasAudioSample.value = !_hasAudioSample.value
    }

    fun startBiometricScan(targetCase: MissingPersonCase) {
        viewModelScope.launch {
            _isScanning.value = true
            _faceAnalysisResult.value = null
            _emergencyAlertDispatched.value = null

            // Realistic scanning simulation delay
            delay(1600)

            val analysis = repository.faceAnalysisService.analyzeFace(
                bitmap = _scannedBitmap.value,
                targetCase = targetCase,
                imageSource = "اسکن زنده دوربین توسط داوطلب میدانی"
            )

            _faceAnalysisResult.value = analysis
            _isScanning.value = false

            if (analysis.potentialMatchPercent >= 75) {
                _emergencyAlertDispatched.value = "سرنخ احتمالی: تطابق ${analysis.potentialMatchPercent}٪ با پرونده ${targetCase.fullName} شناسایی شد. جهت ارزیابی به کارشناس مجاز ارجاع گردید."
                _voiceAssistantStatus.value = "توجه: سرنخ احتمالی ${analysis.potentialMatchPercent} درصد ثبت شد. نتیجه برای بررسی کارشناس ارسال گردید."
            }
        }
    }

    fun dismissAlertBanner() {
        _emergencyAlertDispatched.value = null
    }

    // Submit Report
    fun submitCitizenReport(
        caseId: String,
        personName: String,
        locationName: String,
        photoUri: String?,
        notes: String,
        isEmergency: Boolean
    ) {
        viewModelScope.launch {
            val score = _faceAnalysisResult.value?.potentialMatchPercent ?: 82
            val confidence = _faceAnalysisResult.value?.confidencePercent ?: 88
            val reportId = repository.submitCitizenReport(
                caseId = caseId,
                reportedPersonName = personName,
                locationName = locationName,
                latitude = 35.7782,
                longitude = 51.4116,
                photoUri = photoUri,
                hasAudio = _hasAudioSample.value,
                notes = notes,
                isEmergency = isEmergency,
                potentialMatchScore = score,
                aiConfidence = confidence
            )
            _voiceAssistantStatus.value = "گزارش $reportId با موفقیت ثبت شد و در صف بررسی کارشناس قرار گرفت."
        }
    }

    // Staff Human Review Workflow
    fun reviewReport(
        reportId: String,
        newStatus: ReportStatus,
        notes: String
    ) {
        viewModelScope.launch {
            repository.reviewReport(reportId, newStatus, notes)
            _voiceAssistantStatus.value = "گزارش با موفقیت بررسی و در وضعیت ${newStatus.labelFa} ثبت گردید."
        }
    }

    // Family registers new case
    fun registerFamilyCase(
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
    ) {
        viewModelScope.launch {
            val caseId = repository.registerFamilyCase(
                fullName, age, conditionType, priority, lastSeenLocation,
                heightCm, buildType, clothing, medicalNeeds, rewardToman
            )
            _voiceAssistantStatus.value = "پرونده عزیز شما با موفقیت ثبت و جهت احراز هویت به مأموران ارجاع شد."
            _currentScreen.value = ToponScreen.CASES_DASHBOARD
        }
    }

    // Staff updates status / priority
    fun updateCaseStatus(caseId: String, newStatus: VerificationStatus) {
        viewModelScope.launch {
            repository.updateCaseStatus(caseId, newStatus)
        }
    }

    fun updateCasePriority(caseId: String, newPriority: CasePriority) {
        viewModelScope.launch {
            repository.updateCasePriority(caseId, newPriority)
        }
    }

    fun markNotificationRead(id: String) {
        viewModelScope.launch {
            repository.markNotificationRead(id)
        }
    }

    fun toggleDownloadOfflineZone(zoneId: String) {
        viewModelScope.launch {
            repository.toggleDownloadZone(zoneId)
        }
    }

    fun sendRescueMessage(caseId: String, text: String, role: SenderRole) {
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.sendChatMessage(caseId, text, role)
        }
    }

    // Unified Case Management (Items 40-55)
    val unifiedCases: StateFlow<List<ToponCase>> = repository.unifiedCases

    private val _selectedCaseTypeFilter = MutableStateFlow<CaseType?>(null)
    val selectedCaseTypeFilter = _selectedCaseTypeFilter.asStateFlow()

    private val _selectedPriorityFilter = MutableStateFlow<CasePriority?>(null)
    val selectedPriorityFilter = _selectedPriorityFilter.asStateFlow()

    val filteredUnifiedCases: StateFlow<List<ToponCase>> = combine(
        unifiedCases,
        _selectedCaseTypeFilter,
        _selectedPriorityFilter
    ) { casesList, typeFilter, priorityFilter ->
        casesList.filter { item ->
            (typeFilter == null || item.caseType == typeFilter) &&
            (priorityFilter == null || item.priority == priorityFilter)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedUnifiedCase = MutableStateFlow<ToponCase?>(null)
    val selectedUnifiedCase = _selectedUnifiedCase.asStateFlow()

    val evidenceListForSelectedCase: StateFlow<List<EvidenceItem>> = _selectedUnifiedCase.flatMapLatest { caseItem ->
        if (caseItem == null) MutableStateFlow(emptyList())
        else repository.getEvidenceForCase(caseItem.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chainOfCustodyForSelectedCase: StateFlow<List<ChainOfCustodyEntry>> = _selectedUnifiedCase.flatMapLatest { caseItem ->
        if (caseItem == null) MutableStateFlow(emptyList())
        else repository.getChainOfCustodyLogs(caseItem.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val privateChatMessagesForSelectedCase: StateFlow<List<PrivateChatMessage>> = _selectedUnifiedCase.flatMapLatest { caseItem ->
        if (caseItem == null) MutableStateFlow(emptyList())
        else repository.getPrivateChatMessages(caseItem.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val socialMediaLeads: StateFlow<List<SocialMediaLead>> = repository.getSocialMediaLeads()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _casualtyAnalysisResult = MutableStateFlow<CasualtyAnalysisResult?>(null)
    val casualtyAnalysisResult = _casualtyAnalysisResult.asStateFlow()

    private val _isAnalyzingCasualty = MutableStateFlow(false)
    val isAnalyzingCasualty = _isAnalyzingCasualty.asStateFlow()

    fun setCaseTypeFilter(type: CaseType?) {
        _selectedCaseTypeFilter.value = type
    }

    fun setPriorityFilter(priority: CasePriority?) {
        _selectedPriorityFilter.value = priority
    }

    fun selectUnifiedCase(case: ToponCase) {
        _selectedUnifiedCase.value = case
    }

    fun submitAccidentReport(
        accidentType: AccidentType,
        locationName: String,
        latitude: Double,
        longitude: Double,
        description: String,
        photoUri: String?,
        estimatedCasualties: Int,
        isEmergency: Boolean,
        onSuccess: (ToponCase) -> Unit
    ) {
        viewModelScope.launch {
            val created = repository.submitAccidentReport(
                accidentType = accidentType,
                locationName = locationName,
                latitude = latitude,
                longitude = longitude,
                description = description,
                photoUri = photoUri,
                estimatedCasualties = estimatedCasualties,
                isEmergency = isEmergency,
                reporterUser = currentUser.value
            )
            _selectedUnifiedCase.value = created
            onSuccess(created)
        }
    }

    fun submitDisasterReport(
        disasterType: DisasterType,
        locationName: String,
        latitude: Double,
        longitude: Double,
        description: String,
        photoUri: String?,
        onSuccess: (ToponCase) -> Unit
    ) {
        viewModelScope.launch {
            val created = repository.submitDisasterReport(
                disasterType = disasterType,
                locationName = locationName,
                latitude = latitude,
                longitude = longitude,
                description = description,
                photoUri = photoUri,
                reporterUser = currentUser.value
            )
            _selectedUnifiedCase.value = created
            onSuccess(created)
        }
    }

    fun analyzeCasualtyImage(bitmap: Bitmap?) {
        viewModelScope.launch {
            _isAnalyzingCasualty.value = true
            delay(1200)
            val result = repository.aiService.imageAnalysis.analyzeCasualtyImage(bitmap)
            _casualtyAnalysisResult.value = result
            _isAnalyzingCasualty.value = false
        }
    }

    fun clearCasualtyAnalysis() {
        _casualtyAnalysisResult.value = null
    }

    fun uploadEvidence(
        caseId: String,
        type: EvidenceType,
        title: String,
        mediaUri: String?,
        source: String
    ) {
        viewModelScope.launch {
            repository.uploadEvidence(
                caseId = caseId,
                type = type,
                title = title,
                mediaUri = mediaUri,
                source = source,
                uploaderUser = currentUser.value
            )
        }
    }

    fun sendPrivateChatMessage(
        caseId: String,
        content: String,
        attachmentUri: String? = null,
        isVoiceNote: Boolean = false
    ) {
        viewModelScope.launch {
            repository.sendPrivateChatMessage(
                caseId = caseId,
                content = content,
                attachmentUri = attachmentUri,
                isVoiceNote = isVoiceNote,
                sender = currentUser.value
            )
        }
    }

    fun verifyCaseStatus(
        caseId: String,
        newStatus: VerificationStatus,
        assignedStaffName: String? = null
    ) {
        viewModelScope.launch {
            repository.verifyCaseStatus(
                caseId = caseId,
                newStatus = newStatus,
                assignedStaffName = assignedStaffName ?: currentUser.value.fullName,
                actorUser = currentUser.value
            )
            _selectedUnifiedCase.value = _selectedUnifiedCase.value?.let {
                if (it.id == caseId) it.copy(status = newStatus) else it
            }
        }
    }
}
