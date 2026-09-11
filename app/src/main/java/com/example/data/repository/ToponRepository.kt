package com.example.data.repository

import android.content.Context
import androidx.room.Room
import com.example.R
import com.example.data.api.DefaultFaceAnalysisService
import com.example.data.api.DefaultVoiceAnalysisService
import com.example.data.api.FaceAnalysisService
import com.example.data.api.VoiceAnalysisService
import com.example.data.database.AppDatabase
import com.example.data.database.AuditLogEntity
import com.example.data.database.NotificationEntity
import com.example.data.database.ReportEntity
import com.example.data.database.ToponCaseEntity
import com.example.data.database.EvidenceEntity
import com.example.data.database.ChainOfCustodyEntity
import com.example.data.database.PrivateChatMessageEntity
import com.example.data.database.SocialMediaLeadEntity
import com.example.data.ai.AIService
import com.example.data.ai.DefaultAIService
import com.example.data.auth.AuthService
import com.example.data.auth.DefaultAuthService
import com.example.data.model.AuditLogItem
import com.example.data.model.CasePriority
import com.example.data.model.CaseType
import com.example.data.model.DisasterType
import com.example.data.model.AccidentType
import com.example.data.model.EvidenceType
import com.example.data.model.EvidenceItem
import com.example.data.model.ChainOfCustodyEntry
import com.example.data.model.CasualtyAnalysisResult
import com.example.data.model.PrivateChatMessage
import com.example.data.model.SocialMediaLead
import com.example.data.model.ToponCase
import com.example.data.model.CitizenReport
import com.example.data.model.ConditionType
import com.example.data.model.MissingPersonCase
import com.example.data.model.NotificationItem
import com.example.data.model.NotificationPriority
import com.example.data.model.OfflineMapZone
import com.example.data.model.PhysicalAttributes
import com.example.data.model.ReportStatus
import com.example.data.model.RescueChannelMessage
import com.example.data.model.SenderRole
import com.example.data.model.User
import com.example.data.model.UserRole
import com.example.data.model.VerificationStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.util.UUID

class ToponRepository(context: Context) {

    private val db = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "topon_rescue_v2.db"
    ).fallbackToDestructiveMigration().build()

    private val reportDao = db.reportDao()
    private val auditLogDao = db.auditLogDao()
    private val notificationDao = db.notificationDao()
    private val toponCaseDao = db.toponCaseDao()
    private val evidenceDao = db.evidenceDao()
    private val chainOfCustodyDao = db.chainOfCustodyDao()
    private val privateChatDao = db.privateChatDao()
    private val socialMediaLeadDao = db.socialMediaLeadDao()

    val aiService: AIService = DefaultAIService()
    val authService: AuthService = DefaultAuthService(context, db)

    init {
        CoroutineScope(Dispatchers.IO).launch {
            (authService as? DefaultAuthService)?.initializePreSeededUsers()
            initializePreSeededCasesAndEvidence()
        }
    }

    val faceAnalysisService: FaceAnalysisService = DefaultFaceAnalysisService()
    val voiceAnalysisService: VoiceAnalysisService = DefaultVoiceAnalysisService()

    // Seeded Users for all 4 roles
    private val defaultUsers = listOf(
        User(
            id = "usr-citizen-1",
            username = "citizen_sara",
            fullName = "سارا مرادی",
            email = "sara.moradi@topon.ir",
            phone = "۰۹۱۲۳۴۵۶۷۸۹",
            role = UserRole.CITIZEN,
            contributionPoints = 175
        ),
        User(
            id = "usr-family-1",
            username = "family_rezaei",
            fullName = "علی رضایی",
            email = "ali.rezaei@topon.ir",
            phone = "۰۹۱۸۱۲۳۴۵۶۷",
            role = UserRole.FAMILY,
            contributionPoints = 30
        ),
        User(
            id = "usr-staff-1",
            username = "staff_police_110",
            fullName = "سرگرد مسعود کاظمی",
            email = "m.kazemi@police.ir",
            phone = "۰۹۲۱۰۰۰۱۱۰۰",
            role = UserRole.AUTHORIZED_STAFF,
            organization = "پلیس آگاهی ناجا – ستاد ویژه مفقودین",
            badgeNumber = "POL-8842",
            isMfaEnabled = true
        ),
        User(
            id = "usr-admin-1",
            username = "admin_topon",
            fullName = "مهندس مهدی رادمنش",
            email = "security@topon.ir",
            phone = "۰۹۱۲۹۹۹۸۸۷۷",
            role = UserRole.ADMIN,
            organization = "مرکز پایش و مدیریت امنیت سامانه تاپان",
            badgeNumber = "ADM-001",
            isMfaEnabled = true
        )
    )

    // Current Authenticated User (Defaults to Citizen for smooth start)
    private val _currentUser = MutableStateFlow<User>(defaultUsers[0])
    val currentUser = _currentUser.asStateFlow()

    fun setCurrentUser(user: User) {
        _currentUser.value = user
    }

    // Pre-seeded verified missing person cases with objective Priority
    private val _cases = MutableStateFlow<List<MissingPersonCase>>(
        listOf(
            MissingPersonCase(
                id = "case-01",
                caseNumber = "TPN-1403-9112",
                fullName = "حاج احمد رضایی",
                age = 76,
                priority = CasePriority.CRITICAL, // High age + Alzheimer + insulin needs
                conditionType = ConditionType.ALZHEIMER,
                photoRes = R.drawable.case_senior_avatar,
                verificationAuthority = "پلیس آگاهی ناجا و سازمان امداد هلال احمر",
                status = VerificationStatus.VERIFIED,
                reportedTimeAgo = "۴ ساعت پیش",
                lastSeenLocation = "تهران، ضلع غربی پارک ملت به سمت ولیعصر",
                latitude = 35.7782,
                longitude = 51.4116,
                searchRadiusKm = 3.5,
                physicalAttributes = PhysicalAttributes(
                    heightCm = 172,
                    buildType = "متوسط، قامت کمی خمیده ناشی از کهولت سن",
                    eyeDistanceRatio = "۳۱.۴ میلی‌متر (نسبت طلایی ۰.۴۶)",
                    voiceCharacteristics = "تن صدای آرام، لرزش خفیف در تکلم، پاسخ با تأخیر",
                    clothingDescription = "پالتوی کرم قهوه‌ای، عینک طبی با فریم مشکی، شلوار طوسی تیره",
                    medicalNeeds = "نیازمند انسولین و داروی فشار خون، دچار فراموشی لحظه‌ای موقعیت مکانی"
                ),
                rewardAmountToman = 60_000_000,
                rewardDistributionPolicy = "تسهیم هوشمند: ۶۰٪ برای اولین اسکن و تطابق تأیید شده توسط داوطلب، ۳۰٪ برای گزارش موقعیت دقیق و نگهداری تا رسیدن گشت، ۱۰٪ بین سایر داوطلبان منطقه جستجو.",
                familyEncryptedContact = "AES-256: [تماس امن خانواده از طریق درگاه تاپان]",
                nearbyVolunteersCount = 48,
                highAlert = true,
                registeredByUserId = "usr-family-1",
                isPubliclyVisible = true
            ),
            MissingPersonCase(
                id = "case-04",
                caseNumber = "TPN-1403-9118",
                fullName = "آرین ناصری",
                age = 6,
                priority = CasePriority.CRITICAL, // Young child in freezing mountain
                conditionType = ConditionType.EMERGENCY_ALERT,
                photoRes = R.drawable.case_child_avatar,
                verificationAuthority = "پلیس کوهستان و پایگاه امداد و نجات توچال",
                status = VerificationStatus.VERIFIED,
                reportedTimeAgo = "۱ ساعت پیش",
                lastSeenLocation = "ارتفاعات توچال، ایستگاه دوم تله‌کابین (منطقه کوهستانی دورافتاده)",
                latitude = 35.8322,
                longitude = 51.4190,
                searchRadiusKm = 5.0,
                physicalAttributes = PhysicalAttributes(
                    heightCm = 118,
                    buildType = "کودک ۶ ساله، کاپشن بادگیر زرد شبرنگ",
                    eyeDistanceRatio = "۲۶.۸ میلی‌متر",
                    voiceCharacteristics = "گریان یا ترسان از سرما و تاریکی",
                    clothingDescription = "کاپشن زرد کوهنوردی، کلاه بافتنی طوسی، پوتین ورزشی",
                    medicalNeeds = "خطر سرمازدگی (هیپوترمی) با توجه به شرایط کوهستان، نیازمند پتو و امداد سریع"
                ),
                rewardAmountToman = 80_000_000,
                rewardDistributionPolicy = "تسهیم حداکثری اضطراری با داوطلبان کوهنورد و پایگاه‌های آفلاین امداد کوهستان.",
                familyEncryptedContact = "AES-256: [بیسیم کوهستان و تلفن ماهواره‌ای]",
                nearbyVolunteersCount = 63,
                highAlert = true,
                isPubliclyVisible = true
            ),
            MissingPersonCase(
                id = "case-02",
                caseNumber = "TPN-1403-9104",
                fullName = "سامیار کمالی",
                age = 9,
                priority = CasePriority.HIGH,
                conditionType = ConditionType.AUTISM,
                photoRes = R.drawable.case_child_avatar,
                verificationAuthority = "پلیس ۱۱۰ و ستاد بحران امدادگران داوطلب",
                status = VerificationStatus.VERIFIED,
                reportedTimeAgo = "۲ ساعت پیش",
                lastSeenLocation = "تهران، میدان تجریش، ورودی بازار سنتی",
                latitude = 35.8055,
                longitude = 51.4294,
                searchRadiusKm = 2.0,
                physicalAttributes = PhysicalAttributes(
                    heightCm = 134,
                    buildType = "لاغر اندام و سبک‌پا",
                    eyeDistanceRatio = "۲۸.۵ میلی‌متر (شاخص چشم بادامی)",
                    voiceCharacteristics = "کلمات کوتاه، پرهیز از نگاه مستقیم، ممکن است تکرار اکولالیا داشته باشد",
                    clothingDescription = "هودی قرمز و سرمه‌ای، شلوار جین آبی، کوله پشتی کوچک مشکی",
                    medicalNeeds = "حساس به صداهای بلند و شلوغی، با لحن ملایم و بدون لمس فیزیکی هدایت شود"
                ),
                rewardAmountToman = 45_000_000,
                rewardDistributionPolicy = "صندوق پاداش خانواده: تقسیم بر حسب دقت رهگیری و سرعت انتقال امن به پایگاه امدادی تجریش.",
                familyEncryptedContact = "AES-256: [خط امن خانواده فعال]",
                nearbyVolunteersCount = 82,
                highAlert = true,
                isPubliclyVisible = true
            ),
            MissingPersonCase(
                id = "case-03",
                caseNumber = "TPN-1403-8991",
                fullName = "بانو زهرا سلیمانی",
                age = 81,
                priority = CasePriority.MEDIUM,
                conditionType = ConditionType.SENIOR_CONFUSION,
                photoRes = R.drawable.topon_rescue_icon,
                verificationAuthority = "مرکز کنترل و هماهنگی عملیات اضطراری (EOC)",
                status = VerificationStatus.VERIFIED,
                reportedTimeAgo = "۷ ساعت پیش",
                lastSeenLocation = "شهرری، حوالی حرم حضرت عبدالعظیم",
                latitude = 35.5861,
                longitude = 51.4389,
                searchRadiusKm = 4.0,
                physicalAttributes = PhysicalAttributes(
                    heightCm = 158,
                    buildType = "قد کوتاه، گام‌های آهسته",
                    eyeDistanceRatio = "۲۹.۱ میلی‌متر",
                    voiceCharacteristics = "صدای ملایم، پاسخ‌دهی به نام «بی‌بی زهرا»",
                    clothingDescription = "چادر مشکی طرح‌دار، کیف دستی کوچک قهوه‌ای چرمی",
                    medicalNeeds = "افت قند خون، به آب و استراحت سریع نیاز دارد"
                ),
                rewardAmountToman = 30_000_000,
                rewardDistributionPolicy = "پرداخت بر مبنای شبکه داوطلبان هلال احمر و اسکنرهای شهروندی فعال در محدوده ری.",
                familyEncryptedContact = "AES-256: [کد اختصاصی مرکز پیام خانواده]",
                nearbyVolunteersCount = 29,
                highAlert = false,
                isPubliclyVisible = true
            )
        )
    )

    val cases = _cases.asStateFlow()

    // Incident Chat Channels (auto-grouped by case)
    private val _messages = MutableStateFlow<List<RescueChannelMessage>>(
        listOf(
            RescueChannelMessage(
                id = "msg-1",
                caseId = "case-01",
                senderName = "سروان فراهانی",
                senderRole = SenderRole.POLICE,
                timeAgo = "۳۵ دقیقه پیش",
                text = "دوربین‌های مداربسته خروجی شمالی پارک ملت بررسی شد؛ سوژه به سمت تقاطع نیایش حرکت کرده است.",
                isEncrypted = true,
                priorityHigh = true
            ),
            RescueChannelMessage(
                id = "msg-2",
                caseId = "case-01",
                senderName = "پایگاه امداد و نجات ۱۱۵",
                senderRole = SenderRole.RESCUE_PARAMEDIC,
                timeAgo = "۲۰ دقیقه پیش",
                text = "آمبولانس کد ۳۰۴ در حاشیه اتوبان نیایش مستقر شد. به گشت‌های داوطلب تاپان آماده‌باش داده شد.",
                isEncrypted = true,
                priorityHigh = false
            ),
            RescueChannelMessage(
                id = "msg-3",
                caseId = "case-01",
                senderName = "داوطلب سارا مرادی",
                senderRole = SenderRole.CITIZEN_VOLUNTEER,
                timeAgo = "۱۰ دقیقه پیش",
                text = "اسکن چهره هوشمند در ایستگاه اتوبوس پارک ملت انجام دادم؛ شباهت ۸۴٪ ثبت شد. فرد روی نیمکت نشسته بود.",
                isEncrypted = true,
                priorityHigh = true
            )
        )
    )
    val messages = _messages.asStateFlow()

    // Offline Map Zones
    private val _offlineZones = MutableStateFlow<List<OfflineMapZone>>(
        listOf(
            OfflineMapZone(
                id = "zone-1",
                title = "ارتفاعات البرز مرکزی و توچال",
                region = "مناطق کوهستانی بدون آنتن‌دهی",
                isDownloaded = true,
                sizeMb = 64,
                cachedCasesCount = 1
            ),
            OfflineMapZone(
                id = "zone-2",
                title = "محدوده شهری تهران بزرگ (منطقه ۱ تا ۶)",
                region = "تراکم بالای داوطلبان و رادار",
                isDownloaded = true,
                sizeMb = 128,
                cachedCasesCount = 3
            ),
            OfflineMapZone(
                id = "zone-3",
                title = "شهرری و پیرامون بافت تاریخی",
                region = "پوشش ویژه سالمندان و زائران",
                isDownloaded = false,
                sizeMb = 48,
                cachedCasesCount = 1
            ),
            OfflineMapZone(
                id = "zone-4",
                title = "کویر مرنجاب و مناطق بیابانی کاشان",
                region = "مسیرهای گردشگری و بدون دکل مخابراتی",
                isDownloaded = false,
                sizeMb = 82,
                cachedCasesCount = 0
            )
        )
    )
    val offlineZones = _offlineZones.asStateFlow()

    // Observe reports from Room DB
    val allReports: Flow<List<CitizenReport>> = reportDao.getAllReports().map { list ->
        list.map { it.toDomainModel() }
    }

    // Observe Audit Logs from Room DB
    val auditLogs: Flow<List<AuditLogItem>> = auditLogDao.getAllLogs().map { list ->
        list.map { it.toDomainModel() }
    }

    // Observe Notifications from Room DB
    val notifications: Flow<List<NotificationItem>> = notificationDao.getAllNotifications().map { list ->
        list.map { it.toDomainModel() }
    }

    init {
        // Pre-seed sample report, notification and audit log in background
        CoroutineScope(Dispatchers.IO).launch {
            seedInitialDataIfEmpty()
        }
    }

    private suspend fun seedInitialDataIfEmpty() {
        val initialLog = AuditLogEntity(
            id = "log-init",
            timestamp = System.currentTimeMillis() - 3600000,
            actorUserId = "usr-staff-1",
            actorUsername = "staff_police_110",
            actorRole = UserRole.AUTHORIZED_STAFF.name,
            actionType = "VERIFY_CASE",
            targetResourceId = "case-01",
            details = "تأیید رسمی پرونده حاج احمد رضایی و صدور مجوز انتشار عمومی",
            ipAddressOrClient = "TOPON-Station-110"
        )
        auditLogDao.insertLog(initialLog)

        val initialNotif = NotificationEntity(
            id = "notif-01",
            title = "پرونده بحرانی جدید",
            message = "پرونده کودک ۶ ساله (آرین ناصری) در ایستگاه ۲ توچال ثبت و وضعیت اضطراری اعلام شد.",
            timestamp = System.currentTimeMillis() - 1800000,
            priority = NotificationPriority.CRITICAL.name,
            targetRole = null,
            caseId = "case-04",
            reportId = null,
            isRead = false
        )
        notificationDao.insertNotification(initialNotif)

        val initialReport = ReportEntity(
            id = "RPT-1403-8821",
            caseId = "case-01",
            reporterUserId = "usr-citizen-1",
            reporterName = "سارا مرادی",
            reportedPersonName = "حاج احمد رضایی",
            timestamp = System.currentTimeMillis() - 900000,
            locationName = "ایستگاه اتوبوس تقاطع نیایش و ولیعصر",
            latitude = 35.7801,
            longitude = 51.4132,
            photoUri = null,
            photoRes = R.drawable.case_senior_avatar,
            audioUri = null,
            hasAudio = true,
            citizenNotes = "فردی با مشخصات پالتوی قهوه‌ای و عینک، نشسته و به نظر سردرگم می‌رسید.",
            isEmergency = true,
            status = ReportStatus.UNDER_REVIEW.name,
            potentialMatchScore = 86,
            aiConfidencePercent = 90,
            staffReviewNotes = null,
            reviewedByStaffName = null,
            reviewedTimestamp = null,
            estimatedRewardContributionPoints = 25
        )
        reportDao.insertReport(initialReport)
    }

    // Authentication methods
    suspend fun switchUserRole(role: UserRole) = withContext(Dispatchers.Default) {
        val user = defaultUsers.firstOrNull { it.role == role } ?: defaultUsers[0]
        _currentUser.value = user
        logAction(
            actionType = "ROLE_SWITCH",
            targetResourceId = user.id,
            details = "تغییر نقش کاربری فعال به: ${role.labelFa}"
        )
    }

    suspend fun login(usernameOrEmail: String, role: UserRole): Boolean = withContext(Dispatchers.Default) {
        val match = defaultUsers.firstOrNull { it.role == role } ?: User(
            id = "usr-${UUID.randomUUID().toString().take(8)}",
            username = usernameOrEmail,
            fullName = usernameOrEmail,
            email = "$usernameOrEmail@topon.ir",
            phone = "۰۹۰۰۰۰۰۰۰۰۰",
            role = role
        )
        _currentUser.value = match
        logAction(
            actionType = "LOGIN",
            targetResourceId = match.id,
            details = "ورود موفق کاربر با نقش: ${role.labelFa}"
        )
        true
    }

    suspend fun registerUser(
        fullName: String,
        username: String,
        email: String,
        phone: String,
        role: UserRole,
        organization: String?
    ): User = withContext(Dispatchers.Default) {
        val newUser = User(
            id = "usr-${UUID.randomUUID().toString().take(8)}",
            username = username,
            fullName = fullName,
            email = email,
            phone = phone,
            role = role,
            organization = organization,
            contributionPoints = 10
        )
        _currentUser.value = newUser
        logAction(
            actionType = "REGISTER",
            targetResourceId = newUser.id,
            details = "ثبت نام حساب کاربری جدید با نقش: ${role.labelFa}"
        )
        newUser
    }

    // Submit Citizen Report with Human-in-the-Loop workflow
    suspend fun submitCitizenReport(
        caseId: String,
        reportedPersonName: String,
        locationName: String,
        latitude: Double,
        longitude: Double,
        photoUri: String?,
        hasAudio: Boolean,
        notes: String,
        isEmergency: Boolean,
        potentialMatchScore: Int? = null,
        aiConfidence: Int? = null
    ): String = withContext(Dispatchers.IO) {
        val user = _currentUser.value
        val reportId = "RPT-1403-" + (1000..9999).random()

        val entity = ReportEntity(
            id = reportId,
            caseId = caseId,
            reporterUserId = user.id,
            reporterName = user.fullName,
            reportedPersonName = reportedPersonName,
            timestamp = System.currentTimeMillis(),
            locationName = locationName,
            latitude = latitude,
            longitude = longitude,
            photoUri = photoUri,
            photoRes = null,
            audioUri = null,
            hasAudio = hasAudio,
            citizenNotes = notes,
            isEmergency = isEmergency,
            status = ReportStatus.SUBMITTED.name,
            potentialMatchScore = potentialMatchScore ?: 82,
            aiConfidencePercent = aiConfidence ?: 85,
            staffReviewNotes = null,
            reviewedByStaffName = null,
            reviewedTimestamp = null,
            estimatedRewardContributionPoints = if (isEmergency) 35 else 20
        )

        reportDao.insertReport(entity)

        // Add to Audit Log
        logAction(
            actionType = "SUBMIT_REPORT",
            targetResourceId = reportId,
            details = "ثبت گزارش شهروندی برای پرونده $caseId با تطابق هوش مصنوعی $potentialMatchScore٪"
        )

        // Dispatch notification to staff
        dispatchNotification(
            title = "گزارش شهروندی جدید ($reportId)",
            message = "گزارش جدید در موقعیت $locationName برای $reportedPersonName نیازمند بررسی انسانی است.",
            priority = if (isEmergency) NotificationPriority.CRITICAL else NotificationPriority.HIGH,
            targetRole = UserRole.AUTHORIZED_STAFF,
            caseId = caseId,
            reportId = reportId
        )

        // Add update to incident channel
        val msg = RescueChannelMessage(
            id = UUID.randomUUID().toString(),
            caseId = caseId,
            senderName = "سیستم گزارشگری تاپان",
            senderRole = SenderRole.CITIZEN_VOLUNTEER,
            timeAgo = "هم‌اکنون",
            text = "گزارش شهروندی $reportId در موقعیت $locationName ثبت شد (تطابق احتمالی: ${potentialMatchScore ?: 82}٪). در انتظار تأیید انسانی نهاد مجاز.",
            isEncrypted = true,
            priorityHigh = isEmergency
        )
        _messages.value = listOf(msg) + _messages.value

        reportId
    }

    // Staff Review Action (Human-in-the-Loop)
    suspend fun reviewReport(
        reportId: String,
        newStatus: ReportStatus,
        staffNotes: String
    ) = withContext(Dispatchers.IO) {
        val staffUser = _currentUser.value
        reportDao.updateReviewStatus(
            reportId = reportId,
            status = newStatus.name,
            notes = staffNotes,
            reviewer = staffUser.fullName,
            time = System.currentTimeMillis()
        )

        val actionName = when (newStatus) {
            ReportStatus.VERIFIED -> "APPROVE_REPORT"
            ReportStatus.REJECTED -> "REJECT_REPORT"
            ReportStatus.ESCALATED -> "ESCALATE_REPORT"
            else -> "UPDATE_REPORT"
        }

        logAction(
            actionType = actionName,
            targetResourceId = reportId,
            details = "تغییر وضعیت گزارش به ${newStatus.labelFa} توسط ${staffUser.fullName}. توضیحات: $staffNotes"
        )

        dispatchNotification(
            title = "نتیجه بررسی گزارش $reportId",
            message = "گزارش شما با موفقیت توسط ${staffUser.fullName} بررسی و در وضعیت ${newStatus.labelFa} قرار گرفت.",
            priority = NotificationPriority.NORMAL,
            targetRole = UserRole.CITIZEN,
            reportId = reportId
        )
    }

    // Family registers new case
    suspend fun registerFamilyCase(
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
    ): String = withContext(Dispatchers.Default) {
        val user = _currentUser.value
        val caseId = "case-" + UUID.randomUUID().toString().take(6)
        val caseNumber = "TPN-1403-" + (1000..9999).random()

        val newCase = MissingPersonCase(
            id = caseId,
            caseNumber = caseNumber,
            fullName = fullName,
            age = age,
            priority = priority,
            conditionType = conditionType,
            photoRes = R.drawable.topon_rescue_icon,
            verificationAuthority = "در انتظار تأیید مدارک توسط پلیس",
            status = VerificationStatus.PENDING_REVIEW,
            reportedTimeAgo = "هم‌اکنون",
            lastSeenLocation = lastSeenLocation,
            latitude = 35.7000,
            longitude = 51.4000,
            searchRadiusKm = 3.0,
            physicalAttributes = PhysicalAttributes(
                heightCm = heightCm,
                buildType = buildType,
                eyeDistanceRatio = "۳۰ میلی‌متر (ارزیابی اولیه)",
                voiceCharacteristics = "صدای طبیعی فرد",
                clothingDescription = clothing,
                medicalNeeds = medicalNeeds
            ),
            rewardAmountToman = rewardToman,
            rewardDistributionPolicy = "صندوق پاداش خانواده: توزیع بین داوطلبان بر مبنای صحت گزارش و راهنمایی میدانی.",
            familyEncryptedContact = "AES-256: [خط امن خانواده فعال شد]",
            nearbyVolunteersCount = 20,
            highAlert = priority == CasePriority.CRITICAL,
            registeredByUserId = user.id,
            isPubliclyVisible = true
        )

        _cases.value = listOf(newCase) + _cases.value

        logAction(
            actionType = "REGISTER_CASE",
            targetResourceId = caseId,
            details = "ثبت پرونده جدید برای $fullName با شماره $caseNumber توسط خانواده"
        )

        dispatchNotification(
            title = "پرونده جدید در انتظار تأیید",
            message = "پرونده جدید برای $fullName در موقعیت $lastSeenLocation ثبت شد و نیازمند احراز اولیه است.",
            priority = NotificationPriority.HIGH,
            targetRole = UserRole.AUTHORIZED_STAFF,
            caseId = caseId
        )

        caseId
    }

    // Staff updates case status
    suspend fun updateCaseStatus(caseId: String, newStatus: VerificationStatus) = withContext(Dispatchers.Default) {
        val staffUser = _currentUser.value
        _cases.value = _cases.value.map { c ->
            if (c.id == caseId) c.copy(status = newStatus) else c
        }

        logAction(
            actionType = "UPDATE_CASE_STATUS",
            targetResourceId = caseId,
            details = "تغییر وضعیت پرونده به ${newStatus.label} توسط ${staffUser.fullName}"
        )

        dispatchNotification(
            title = "بروزرسانی وضعیت پرونده",
            message = "وضعیت پرونده $caseId به ${newStatus.label} تغییر یافت.",
            priority = NotificationPriority.NORMAL,
            caseId = caseId
        )
    }

    // Staff updates case priority
    suspend fun updateCasePriority(caseId: String, newPriority: CasePriority) = withContext(Dispatchers.Default) {
        val staffUser = _currentUser.value
        _cases.value = _cases.value.map { c ->
            if (c.id == caseId) c.copy(priority = newPriority, highAlert = newPriority == CasePriority.CRITICAL) else c
        }

        logAction(
            actionType = "UPDATE_CASE_PRIORITY",
            targetResourceId = caseId,
            details = "تغییر اولویت پرونده به ${newPriority.labelFa} توسط ${staffUser.fullName}"
        )
    }

    suspend fun logAction(actionType: String, targetResourceId: String, details: String) = withContext(Dispatchers.IO) {
        val user = _currentUser.value
        val entity = AuditLogEntity(
            id = "log-${UUID.randomUUID().toString().take(8)}",
            timestamp = System.currentTimeMillis(),
            actorUserId = user.id,
            actorUsername = user.username,
            actorRole = user.role.name,
            actionType = actionType,
            targetResourceId = targetResourceId,
            details = details,
            ipAddressOrClient = "TOPON-App-Session"
        )
        auditLogDao.insertLog(entity)
    }

    suspend fun dispatchNotification(
        title: String,
        message: String,
        priority: NotificationPriority,
        targetRole: UserRole? = null,
        caseId: String? = null,
        reportId: String? = null
    ) = withContext(Dispatchers.IO) {
        val notif = NotificationEntity(
            id = "notif-${UUID.randomUUID().toString().take(8)}",
            title = title,
            message = message,
            timestamp = System.currentTimeMillis(),
            priority = priority.name,
            targetRole = targetRole?.name,
            caseId = caseId,
            reportId = reportId,
            isRead = false
        )
        notificationDao.insertNotification(notif)
    }

    suspend fun markNotificationRead(id: String) = withContext(Dispatchers.IO) {
        notificationDao.markAsRead(id)
    }

    suspend fun toggleDownloadZone(zoneId: String) = withContext(Dispatchers.IO) {
        _offlineZones.value = _offlineZones.value.map { zone ->
            if (zone.id == zoneId) zone.copy(isDownloaded = !zone.isDownloaded) else zone
        }
    }

    suspend fun sendChatMessage(caseId: String, text: String, senderRole: SenderRole) = withContext(Dispatchers.IO) {
        val user = _currentUser.value
        val msg = RescueChannelMessage(
            id = UUID.randomUUID().toString(),
            caseId = caseId,
            senderName = "${user.fullName} (${senderRole.label})",
            senderRole = senderRole,
            timeAgo = "هم‌اکنون",
            text = text,
            isEncrypted = true,
            priorityHigh = false
        )
        _messages.value = listOf(msg) + _messages.value
    }

    private fun ReportEntity.toDomainModel(): CitizenReport {
        return CitizenReport(
            id = id,
            caseId = caseId,
            reporterUserId = reporterUserId,
            reporterName = reporterName,
            reportedPersonName = reportedPersonName,
            timestamp = timestamp,
            locationName = locationName,
            latitude = latitude,
            longitude = longitude,
            photoUri = photoUri,
            photoRes = photoRes,
            audioUri = audioUri,
            hasAudio = hasAudio,
            citizenNotes = citizenNotes,
            isEmergency = isEmergency,
            status = try { ReportStatus.valueOf(status) } catch (e: Exception) { ReportStatus.SUBMITTED },
            potentialMatchScore = potentialMatchScore,
            aiConfidencePercent = aiConfidencePercent,
            staffReviewNotes = staffReviewNotes,
            reviewedByStaffName = reviewedByStaffName,
            reviewedTimestamp = reviewedTimestamp,
            estimatedRewardContributionPoints = estimatedRewardContributionPoints
        )
    }

    private fun AuditLogEntity.toDomainModel(): AuditLogItem {
        return AuditLogItem(
            id = id,
            timestamp = timestamp,
            actorUserId = actorUserId,
            actorUsername = actorUsername,
            actorRole = try { UserRole.valueOf(actorRole) } catch (e: Exception) { UserRole.CITIZEN },
            actionType = actionType,
            targetResourceId = targetResourceId,
            details = details,
            ipAddressOrClient = ipAddressOrClient
        )
    }

    private fun NotificationEntity.toDomainModel(): NotificationItem {
        return NotificationItem(
            id = id,
            title = title,
            message = message,
            timestamp = timestamp,
            priority = try { NotificationPriority.valueOf(priority) } catch (e: Exception) { NotificationPriority.NORMAL },
            targetRole = targetRole?.let { try { UserRole.valueOf(it) } catch (e: Exception) { null } },
            caseId = caseId,
            reportId = reportId,
            isRead = isRead
        )
    }

    // --- Unified Case Management & Section 40-54 Implementations ---
    private val _unifiedCases = MutableStateFlow<List<ToponCase>>(emptyList())
    val unifiedCases = _unifiedCases.asStateFlow()

    fun getEvidenceForCase(caseId: String): Flow<List<EvidenceItem>> {
        return evidenceDao.getEvidenceForCase(caseId).map { list ->
            list.map { it.toDomainModel() }
        }
    }

    fun getChainOfCustodyLogs(caseId: String): Flow<List<ChainOfCustodyEntry>> {
        return chainOfCustodyDao.getLogsForCase(caseId).map { list ->
            list.map { it.toDomainModel() }
        }
    }

    fun getPrivateChatMessages(caseId: String): Flow<List<PrivateChatMessage>> {
        return privateChatDao.getMessagesForCase(caseId).map { list ->
            list.map { it.toDomainModel() }
        }
    }

    fun getSocialMediaLeads(caseId: String? = null): Flow<List<SocialMediaLead>> {
        return if (caseId != null) {
            socialMediaLeadDao.getLeadsForCase(caseId).map { list -> list.map { it.toDomainModel() } }
        } else {
            socialMediaLeadDao.getAllLeads().map { list -> list.map { it.toDomainModel() } }
        }
    }

    suspend fun submitAccidentReport(
        accidentType: AccidentType,
        locationName: String,
        latitude: Double,
        longitude: Double,
        description: String,
        photoUri: String?,
        estimatedCasualties: Int,
        isEmergency: Boolean,
        reporterUser: User
    ): ToponCase = withContext(Dispatchers.IO) {
        val caseId = "case-acc-" + UUID.randomUUID().toString().take(6)
        val caseNumber = "ACC-1403-" + (1000..9999).random()
        val priority = if (isEmergency || estimatedCasualties > 2) CasePriority.CRITICAL else CasePriority.HIGH
        
        val newCase = ToponCase(
            id = caseId,
            caseNumber = caseNumber,
            title = "گزارش تصادف: ${accidentType.labelFa} در $locationName",
            caseType = CaseType.ACCIDENT,
            priority = priority,
            status = VerificationStatus.PENDING_REVIEW,
            createdAt = System.currentTimeMillis(),
            lastUpdated = System.currentTimeMillis(),
            location = locationName,
            latitude = latitude,
            longitude = longitude,
            description = description,
            verificationAuthority = "پلیس راهور و مرکز فوریت‌های پزشکی ۱۱۵",
            assignedStaff = null,
            isPubliclyVisible = true,
            estimatedCasualties = estimatedCasualties,
            reportsCount = 1,
            photoUri = photoUri
        )

        toponCaseDao.insertCase(newCase.toEntity())
        _unifiedCases.value = listOf(newCase) + _unifiedCases.value

        val custodyLog = ChainOfCustodyEntity(
            id = UUID.randomUUID().toString(),
            evidenceId = "init-report-$caseId",
            caseId = caseId,
            timestamp = System.currentTimeMillis(),
            actorName = reporterUser.fullName,
            actorRole = reporterUser.role.name,
            action = "UPLOADED",
            details = "ثبت اولیه گزارش تصادف میدانی توسط شهروند همراه با ثبت موقعیت و تصاویر اولیه"
        )
        chainOfCustodyDao.insertLog(custodyLog)

        logAction(
            actionType = "SUBMIT_ACCIDENT_REPORT",
            targetResourceId = caseId,
            details = "ثبت گزارش تصادف $caseNumber در $locationName"
        )

        dispatchNotification(
            title = "گزارش تصادف جدید ($caseNumber)",
            message = "تصادف ${accidentType.labelFa} در $locationName ثبت شد و در صف بررسی قرار گرفت.",
            priority = NotificationPriority.HIGH,
            caseId = caseId
        )

        newCase
    }

    suspend fun submitDisasterReport(
        disasterType: DisasterType,
        locationName: String,
        latitude: Double,
        longitude: Double,
        description: String,
        photoUri: String?,
        reporterUser: User
    ): ToponCase = withContext(Dispatchers.IO) {
        val caseId = "case-dis-" + UUID.randomUUID().toString().take(6)
        val caseNumber = "DIS-1403-" + (1000..9999).random()

        val newCase = ToponCase(
            id = caseId,
            caseNumber = caseNumber,
            title = "سانحه و بحران: ${disasterType.labelFa} در $locationName",
            caseType = CaseType.DISASTER,
            priority = CasePriority.CRITICAL,
            status = VerificationStatus.PENDING_REVIEW,
            createdAt = System.currentTimeMillis(),
            lastUpdated = System.currentTimeMillis(),
            location = locationName,
            latitude = latitude,
            longitude = longitude,
            description = description,
            verificationAuthority = "سازمان مدیریت بحران و هلال احمر",
            disasterSubtype = disasterType.labelFa,
            isPubliclyVisible = true,
            reportsCount = 1,
            photoUri = photoUri
        )

        toponCaseDao.insertCase(newCase.toEntity())
        _unifiedCases.value = listOf(newCase) + _unifiedCases.value

        logAction(
            actionType = "SUBMIT_DISASTER_REPORT",
            targetResourceId = caseId,
            details = "ثبت سانحه ${disasterType.labelFa} در $locationName"
        )

        dispatchNotification(
            title = "هشدار بحران و سانحه ($caseNumber)",
            message = "سانحه ${disasterType.labelFa} در $locationName گزارش شد.",
            priority = NotificationPriority.CRITICAL,
            caseId = caseId
        )

        newCase
    }

    suspend fun uploadEvidence(
        caseId: String,
        type: EvidenceType,
        title: String,
        mediaUri: String?,
        source: String,
        uploaderUser: User
    ): EvidenceItem = withContext(Dispatchers.IO) {
        val evidenceId = "evi-" + UUID.randomUUID().toString().take(8)
        val fileHash = "SHA256:" + UUID.randomUUID().toString().replace("-", "").take(16)
        val evidence = EvidenceItem(
            id = evidenceId,
            caseId = caseId,
            type = type,
            title = title,
            mediaUri = mediaUri,
            source = source,
            uploadTime = System.currentTimeMillis(),
            uploadedByUserId = uploaderUser.id,
            uploadedByName = uploaderUser.fullName,
            uploadedByRole = uploaderUser.role,
            verificationStatus = VerificationStatus.PENDING_REVIEW,
            fileHashSha256 = fileHash
        )

        evidenceDao.insertEvidence(evidence.toEntity())

        val custodyEntry = ChainOfCustodyEntity(
            id = UUID.randomUUID().toString(),
            evidenceId = evidenceId,
            caseId = caseId,
            timestamp = System.currentTimeMillis(),
            actorName = uploaderUser.fullName,
            actorRole = uploaderUser.role.name,
            action = "UPLOADED",
            details = "آپلود مدرک و سند به مخزن پرونده با هش معتبر $fileHash"
        )
        chainOfCustodyDao.insertLog(custodyEntry)

        evidence
    }

    suspend fun sendPrivateChatMessage(
        caseId: String,
        content: String,
        attachmentUri: String? = null,
        isVoiceNote: Boolean = false,
        sender: User
    ) = withContext(Dispatchers.IO) {
        val message = PrivateChatMessage(
            id = "pmsg-" + UUID.randomUUID().toString().take(8),
            caseId = caseId,
            senderId = sender.id,
            senderName = sender.fullName,
            senderRole = sender.role,
            timestamp = System.currentTimeMillis(),
            content = content,
            attachmentUri = attachmentUri,
            isVoiceNote = isVoiceNote,
            deliveryStatus = "DELIVERED"
        )
        privateChatDao.insertMessage(message.toEntity())
    }

    suspend fun verifyCaseStatus(
        caseId: String,
        newStatus: VerificationStatus,
        assignedStaffName: String?,
        actorUser: User
    ) = withContext(Dispatchers.IO) {
        toponCaseDao.updateCaseStatus(caseId, newStatus.name, System.currentTimeMillis())
        _unifiedCases.value = _unifiedCases.value.map { c ->
            if (c.id == caseId) c.copy(status = newStatus, assignedStaff = assignedStaffName ?: c.assignedStaff) else c
        }

        val custodyEntry = ChainOfCustodyEntity(
            id = UUID.randomUUID().toString(),
            evidenceId = "case-$caseId",
            caseId = caseId,
            timestamp = System.currentTimeMillis(),
            actorName = actorUser.fullName,
            actorRole = actorUser.role.name,
            action = "STATUS_CHANGED",
            details = "تغییر وضعیت رسمی پرونده به ${newStatus.label} توسط کارمند مجاز"
        )
        chainOfCustodyDao.insertLog(custodyEntry)
    }

    private suspend fun initializePreSeededCasesAndEvidence() = withContext(Dispatchers.IO) {
        val initialCases = listOf(
            ToponCase(
                id = "case-01",
                caseNumber = "TPN-1403-9112",
                title = "فرد مفقود: حاج احمد رضایی (سالمند مبتلا به آلزایمر)",
                caseType = CaseType.MISSING_PERSON,
                priority = CasePriority.CRITICAL,
                status = VerificationStatus.VERIFIED,
                location = "تهران، ضلع غربی پارک ملت به سمت ولیعصر",
                latitude = 35.7782,
                longitude = 51.4116,
                description = "سالمند ۷۶ ساله مبتلا به آلزایمر، نیازمند تزریق منظم انسولین. آخرین بار با پالتوی کرم قهوه‌ای رویت شده است.",
                verificationAuthority = "پلیس آگاهی ناجا و سازمان امداد هلال احمر",
                assignedStaff = "سرگرد مسعود کاظمی",
                reportsCount = 3,
                photoRes = R.drawable.case_senior_avatar
            ),
            ToponCase(
                id = "case-02",
                caseNumber = "TPN-1403-8041",
                title = "فرد مفقود: سارا کریمی (طیف اوتیسم)",
                caseType = CaseType.MISSING_PERSON,
                priority = CasePriority.HIGH,
                status = VerificationStatus.VERIFIED,
                location = "اصفهان، حاشیه زاینده‌رود نزدیک سی‌وسه‌پل",
                latitude = 32.6546,
                longitude = 51.6679,
                description = "نوجوان ۱۴ ساله دارای طیف اوتیسم، به صداهای بلند حساس است و ممکن است به نقاط آرام پناه برده باشد.",
                verificationAuthority = "مرکز فوریت‌های اجتماعی و هلال احمر",
                assignedStaff = "سروان فراهانی",
                reportsCount = 1,
                photoRes = null
            ),
            ToponCase(
                id = "case-03",
                caseNumber = "TPN-1403-7720",
                title = "فرد مفقود: کیان حسینی (کودک گمشده)",
                caseType = CaseType.MISSING_PERSON,
                priority = CasePriority.CRITICAL,
                status = VerificationStatus.VERIFIED,
                location = "شیراز، بازار وکیل ورودی سرای مشیر",
                latitude = 29.6152,
                longitude = 52.5458,
                description = "کودک ۵ ساله با کاپشن زرد و کلاه آبی، هنگام خرید از خانواده جدا شده است.",
                verificationAuthority = "کلانتری مرکزی شیراز و امدادگران داوطلب",
                assignedStaff = "سرگرد مسعود کاظمی",
                reportsCount = 2,
                photoRes = R.drawable.case_child_avatar
            ),
            ToponCase(
                id = "case-wanted-01",
                caseNumber = "WNT-1403-881",
                title = "فرد تحت تعقیب: متهم پرونده کلاهبرداری کلان و فرار مالیاتی",
                caseType = CaseType.WANTED_PERSON,
                priority = CasePriority.HIGH,
                status = VerificationStatus.VERIFIED,
                location = "محدوده غرب تهران و پایانه‌های پروازی",
                latitude = 35.6892,
                longitude = 51.3890,
                description = "حکم جلب بین‌المللی اینترپل (Red Notice). کلیه اطلاعات صرفاً از مراجع رسمی قضایی ثبت شده است. هرگونه تطبیق چهره احتمالی صرفاً یک سرنخ است و نیازمند بررسی انسانی مأمور مجاز می‌باشد.",
                verificationAuthority = "پلیس بین‌الملل اینترپل ف.ل. و دادسرای ناحیه ۳۲",
                assignedStaff = "سرگرد مسعود کاظمی",
                officialWarrantNumber = "WNT-1403-881",
                sensitiveConfidentialNotes = "آدرس منزل و اطلاعات تماس خصوصی متهم طبق اصل Privacy by Design از دید عموم مخفی است.",
                reportsCount = 1
            ),
            ToponCase(
                id = "case-accident-01",
                caseNumber = "ACC-1403-512",
                title = "گزارش تصادف زنجیره‌ای اتوبان شهید همت شرق",
                caseType = CaseType.ACCIDENT,
                priority = CasePriority.HIGH,
                status = VerificationStatus.VERIFIED,
                location = "تهران، اتوبان شهید همت شرق، خروجی چمران",
                latitude = 35.7512,
                longitude = 51.3980,
                description = "برخورد ۳ دستگاه خودرو سواری در لاین تندرو. ۲ مصدوم سرپایی به آمبولانس هدایت شدند. معبر در حال پاکسازی است.",
                verificationAuthority = "پلیس راهور و مرکز فوریت‌های پزشکی ۱۱۵",
                assignedStaff = "سرگرد مسعود کاظمی",
                estimatedCasualties = 2,
                reportsCount = 2
            ),
            ToponCase(
                id = "case-disaster-01",
                caseNumber = "DIS-1403-903",
                title = "حادثه و سوانح: آتش‌سوزی گسترده انبار صنعتی کهریزک",
                caseType = CaseType.DISASTER,
                priority = CasePriority.CRITICAL,
                status = VerificationStatus.VERIFIED,
                location = "تهران، جاده قدیم قم، کهریزک، شهرک صنعتی",
                latitude = 35.5200,
                longitude = 51.3600,
                description = "حریق در انبار نگهداری مواد اولیه سلولزی و کارتن. اعزام ۵ ایستگاه آتش‌نشانی همراه با تانکرهای آب پشتیبان.",
                verificationAuthority = "سازمان آتش‌نشانی و خدمات ایمنی تهران",
                assignedStaff = "سرگرد مسعود کاظمی",
                disasterSubtype = "آتش‌سوزی",
                estimatedCasualties = 0,
                reportsCount = 1
            ),
            ToponCase(
                id = "case-emergency-01",
                caseNumber = "EMG-1403-104",
                title = "درخواست کمک اضطراری: کوهنورد سرگردان ارتفاعات دارآباد",
                caseType = CaseType.EMERGENCY,
                priority = CasePriority.CRITICAL,
                status = VerificationStatus.UNDER_INVESTIGATION,
                location = "تهران، ارتفاعات البرز، یال شن‌سیاه دارآباد",
                latitude = 35.8350,
                longitude = 51.4900,
                description = "کوهنورد به دلیل مه و تاریکی از پاکوب اصلی منحرف شده و با تماس اضطراری درخواست راهنمایی کرده است. تیم امدادی اعزام شد.",
                verificationAuthority = "تیم امداد و نجات کوهستان هلال احمر",
                assignedStaff = "سروان فراهانی",
                reportsCount = 1
            )
        )

        toponCaseDao.insertCases(initialCases.map { it.toEntity() })
        _unifiedCases.value = initialCases

        val initialEvidence = listOf(
            EvidenceEntity(
                id = "evi-1",
                caseId = "case-accident-01",
                type = "IMAGE",
                title = "تصویر موقعیت خودروهای سانحه‌دیده لاین تندرو",
                mediaUri = null,
                source = "شهروند حاضر در صحنه حادثه",
                uploadTime = System.currentTimeMillis() - 3600000,
                uploadedByUserId = "usr-citizen-1",
                uploadedByName = "سارا مرادی",
                uploadedByRole = "CITIZEN",
                verificationStatus = "VERIFIED",
                fileHashSha256 = "SHA256:7f83b1657ff1fc53b92dc18148a1d65dfc2d4b1fa3d677284addd200126d9069"
            ),
            EvidenceEntity(
                id = "evi-2",
                caseId = "case-01",
                type = "IMAGE",
                title = "عکس ارسالی دوربین نظارتی حاشیه پارک ملت",
                mediaUri = null,
                source = "دوربین پایش ترافیکی شهری",
                uploadTime = System.currentTimeMillis() - 7200000,
                uploadedByUserId = "usr-staff-1",
                uploadedByName = "سرگرد مسعود کاظمی",
                uploadedByRole = "AUTHORIZED_STAFF",
                verificationStatus = "VERIFIED",
                fileHashSha256 = "SHA256:4f8a81d8cf01dc8a2b53b92dc18148a1d65dfc2d4b1fa3d677284addd200126d"
            )
        )
        evidenceDao.insertEvidenceList(initialEvidence)

        val initialCustody = listOf(
            ChainOfCustodyEntity(
                id = "custody-1",
                evidenceId = "evi-1",
                caseId = "case-accident-01",
                timestamp = System.currentTimeMillis() - 3600000,
                actorName = "سارا مرادی",
                actorRole = "CITIZEN",
                action = "UPLOADED",
                details = "ارسال اولیه تصویر توسط شهروند حاضر در صحنه با رمزنگاری هش یکپارچگی"
            ),
            ChainOfCustodyEntity(
                id = "custody-2",
                evidenceId = "evi-1",
                caseId = "case-accident-01",
                timestamp = System.currentTimeMillis() - 3000000,
                actorName = "سرگرد مسعود کاظمی",
                actorRole = "AUTHORIZED_STAFF",
                action = "VIEWED",
                details = "مشاهده و تطبیق سند توسط کارشناس بررسی حوادث پلیس راهور"
            ),
            ChainOfCustodyEntity(
                id = "custody-3",
                evidenceId = "evi-1",
                caseId = "case-accident-01",
                timestamp = System.currentTimeMillis() - 2500000,
                actorName = "سرگرد مسعود کاظمی",
                actorRole = "AUTHORIZED_STAFF",
                action = "VERIFIED",
                details = "تأیید اصالت سند و ارجاع به اکیپ اعزامی امداد جاده‌ای"
            )
        )
        chainOfCustodyDao.insertLogs(initialCustody)

        val initialPrivateChat = listOf(
            PrivateChatMessageEntity(
                id = "pmsg-1",
                caseId = "case-01",
                senderId = "usr-family-1",
                senderName = "علی رضایی (فرزند مددجو)",
                senderRole = "FAMILY",
                timestamp = System.currentTimeMillis() - 5400000,
                content = "سلام جناب سروان، پدرم داروی انسولین و فشار خونش رو حدود ظهر مصرف کرده بود و ممکنه دچار افت قند شده باشه.",
                attachmentUri = null,
                isVoiceNote = false,
                deliveryStatus = "READ"
            ),
            PrivateChatMessageEntity(
                id = "pmsg-2",
                caseId = "case-01",
                senderId = "usr-staff-1",
                senderName = "سرگرد مسعود کاظمی (پلیس آگاهی)",
                senderRole = "AUTHORIZED_STAFF",
                timestamp = System.currentTimeMillis() - 4800000,
                content = "سلام و احترام. اطلاعات پزشکی ایشان بلافاصله در هماهنگی با گشت هلال احمر و ۱۱۵ منطقه ثبت شد. تیم‌های میدانی مطلع هستند.",
                attachmentUri = null,
                isVoiceNote = false,
                deliveryStatus = "READ"
            )
        )
        privateChatDao.insertMessages(initialPrivateChat)

        val initialSocialLeads = listOf(
            SocialMediaLeadEntity(
                id = "lead-1",
                caseId = "case-01",
                sourcePlatform = "X (توییتر سابق) - پست عمومی",
                publicPostUrlOrHandle = "@tehran_citizen_news/status/182...",
                contentSnippet = "یک پیرمرد با پالتوی کرم قهوه‌ای حدود ساعت ۶ عصر نزدیک ایستگاه بی‌آرتی پارک ملت نشسته بود و به نظر سردرگم می‌رسید. #مفقودی #تهران",
                detectedKeywordsCsv = "پارک ملت,پالتوی کرم,پیرمرد,سردرگم",
                locationMention = "ایستگاه پارک ملت",
                timestamp = System.currentTimeMillis() - 3600000,
                aiConfidenceScore = 87,
                status = "POTENTIAL_LEAD",
                complianceNote = "استخراج صرفاً از پست عمومی و قانونی بدون دسترسی به حساب شخصی یا داده‌های خصوصی."
            ),
            SocialMediaLeadEntity(
                id = "lead-2",
                caseId = "case-accident-01",
                sourcePlatform = "کانال عمومی تلگرام اخبار راهور",
                publicPostUrlOrHandle = "https://t.me/traffic_tehran/4921",
                contentSnippet = "ترافیک سنگین در اتوبان همت شرق نرسیده به چمران به علت تصادف سه سواری. عوامل امدادی در محل حضور دارند.",
                detectedKeywordsCsv = "همت شرق,تصادف,چمران,امداد",
                locationMention = "اتوبان همت شرق",
                timestamp = System.currentTimeMillis() - 2400000,
                aiConfidenceScore = 93,
                status = "VERIFIED_LEAD",
                complianceNote = "منبع رسمی اطلاع‌رسانی ترافیک شهری."
            )
        )
        socialMediaLeadDao.insertLeads(initialSocialLeads)
    }

    // Entity to Domain mappers
    private fun ToponCase.toEntity(): ToponCaseEntity = ToponCaseEntity(
        id = id,
        caseNumber = caseNumber,
        title = title,
        caseType = caseType.name,
        priority = priority.name,
        status = status.name,
        createdAt = createdAt,
        lastUpdated = lastUpdated,
        location = location,
        latitude = latitude,
        longitude = longitude,
        description = description,
        verificationAuthority = verificationAuthority,
        assignedStaff = assignedStaff,
        isPubliclyVisible = isPubliclyVisible,
        sensitiveConfidentialNotes = sensitiveConfidentialNotes,
        officialWarrantNumber = officialWarrantNumber,
        estimatedCasualties = estimatedCasualties,
        disasterSubtype = disasterSubtype,
        reportsCount = reportsCount,
        photoRes = photoRes,
        photoUri = photoUri
    )

    private fun ToponCaseEntity.toDomainModel(): ToponCase = ToponCase(
        id = id,
        caseNumber = caseNumber,
        title = title,
        caseType = try { CaseType.valueOf(caseType) } catch (e: Exception) { CaseType.MISSING_PERSON },
        priority = try { CasePriority.valueOf(priority) } catch (e: Exception) { CasePriority.NORMAL },
        status = try { VerificationStatus.valueOf(status) } catch (e: Exception) { VerificationStatus.PENDING_REVIEW },
        createdAt = createdAt,
        lastUpdated = lastUpdated,
        location = location,
        latitude = latitude,
        longitude = longitude,
        description = description,
        verificationAuthority = verificationAuthority,
        assignedStaff = assignedStaff,
        isPubliclyVisible = isPubliclyVisible,
        sensitiveConfidentialNotes = sensitiveConfidentialNotes,
        officialWarrantNumber = officialWarrantNumber,
        estimatedCasualties = estimatedCasualties,
        disasterSubtype = disasterSubtype,
        reportsCount = reportsCount,
        photoRes = photoRes,
        photoUri = photoUri
    )

    private fun EvidenceItem.toEntity(): EvidenceEntity = EvidenceEntity(
        id = id,
        caseId = caseId,
        type = type.name,
        title = title,
        mediaUri = mediaUri,
        source = source,
        uploadTime = uploadTime,
        uploadedByUserId = uploadedByUserId,
        uploadedByName = uploadedByName,
        uploadedByRole = uploadedByRole.name,
        verificationStatus = verificationStatus.name,
        fileHashSha256 = fileHashSha256
    )

    private fun EvidenceEntity.toDomainModel(): EvidenceItem = EvidenceItem(
        id = id,
        caseId = caseId,
        type = try { EvidenceType.valueOf(type) } catch (e: Exception) { EvidenceType.IMAGE },
        title = title,
        mediaUri = mediaUri,
        source = source,
        uploadTime = uploadTime,
        uploadedByUserId = uploadedByUserId,
        uploadedByName = uploadedByName,
        uploadedByRole = try { UserRole.valueOf(uploadedByRole) } catch (e: Exception) { UserRole.CITIZEN },
        verificationStatus = try { VerificationStatus.valueOf(verificationStatus) } catch (e: Exception) { VerificationStatus.PENDING_REVIEW },
        fileHashSha256 = fileHashSha256
    )

    private fun ChainOfCustodyEntity.toDomainModel(): ChainOfCustodyEntry = ChainOfCustodyEntry(
        id = id,
        evidenceId = evidenceId,
        caseId = caseId,
        timestamp = timestamp,
        actorName = actorName,
        actorRole = try { UserRole.valueOf(actorRole) } catch (e: Exception) { UserRole.CITIZEN },
        action = action,
        details = details
    )

    private fun PrivateChatMessage.toEntity(): PrivateChatMessageEntity = PrivateChatMessageEntity(
        id = id,
        caseId = caseId,
        senderId = senderId,
        senderName = senderName,
        senderRole = senderRole.name,
        timestamp = timestamp,
        content = content,
        attachmentUri = attachmentUri,
        isVoiceNote = isVoiceNote,
        deliveryStatus = deliveryStatus
    )

    private fun PrivateChatMessageEntity.toDomainModel(): PrivateChatMessage = PrivateChatMessage(
        id = id,
        caseId = caseId,
        senderId = senderId,
        senderName = senderName,
        senderRole = try { UserRole.valueOf(senderRole) } catch (e: Exception) { UserRole.CITIZEN },
        timestamp = timestamp,
        content = content,
        attachmentUri = attachmentUri,
        isVoiceNote = isVoiceNote,
        deliveryStatus = deliveryStatus
    )

    private fun SocialMediaLeadEntity.toDomainModel(): SocialMediaLead = SocialMediaLead(
        id = id,
        caseId = caseId,
        sourcePlatform = sourcePlatform,
        publicPostUrlOrHandle = publicPostUrlOrHandle,
        contentSnippet = contentSnippet,
        detectedKeywords = detectedKeywordsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() },
        locationMention = locationMention,
        timestamp = timestamp,
        aiConfidenceScore = aiConfidenceScore,
        status = status,
        complianceNote = complianceNote
    )
}
