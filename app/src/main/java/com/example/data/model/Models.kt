package com.example.data.model

import androidx.annotation.DrawableRes

/**
 * User Roles in TOPON
 */
enum class UserRole(val labelFa: String, val badgeColorHex: Long) {
    CITIZEN("شهروند / داوطلب", 0xFF0D9488),
    FAMILY("خانواده فرد گمشده", 0xFF7C3AED),
    AUTHORIZED_STAFF("کارمند / نهاد مجاز (پلیس و امداد)", 0xFF1E3A8A),
    ADMIN("مدیر ارشد سامانه (Admin)", 0xFFDC2626)
}

data class User(
    val id: String,
    val username: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val role: UserRole,
    val organization: String? = null,
    val badgeNumber: String? = null,
    val contributionPoints: Int = 0,
    val isMfaEnabled: Boolean = false
)

/**
 * Objective, ethical Priority Levels (based on vulnerability, elapsed time, weather, health needs)
 * Never based on race, gender, or appearance.
 */
enum class CasePriority(val labelFa: String, val colorHex: Long, val orderWeight: Int) {
    CRITICAL("بحرانی (سرخ)", 0xFFEF4444, 4),
    HIGH("بالا (نارنجی)", 0xFFF97316, 3),
    MEDIUM("متوسط (زرد)", 0xFFFBBF24, 2),
    NORMAL("عادی (سبز)", 0xFF10B981, 1);

    val badgeColorHex: Long get() = colorHex
}

enum class ConditionType(val label: String, val badgeColorHex: Long) {
    ALZHEIMER("آلزایمر / زوال عقل", 0xFF8B5CF6),
    AUTISM("طیف اوتیسم", 0xFF0EA5E9),
    SENIOR_CONFUSION("سالمند سردرگم محیطی", 0xFFF59E0B),
    CHILD("کودک گمشده", 0xFFEC4899),
    EMERGENCY_ALERT("هشدار فوری پزشکی/محیطی", 0xFFEF4444)
}

enum class VerificationStatus(val label: String) {
    PENDING_REVIEW("در انتظار بررسی و احراز اولیه"),
    UNDER_INVESTIGATION("در حال تحقیق و پایش میدانی"),
    VERIFIED("تأیید رسمی نهادها (منتشر شده برای عموم)"),
    RESOLVED("یافت شده و تحویل خانواده")
}

data class PhysicalAttributes(
    val heightCm: Int,
    val buildType: String,
    val eyeDistanceRatio: String,
    val voiceCharacteristics: String,
    val clothingDescription: String,
    val medicalNeeds: String
)

data class MissingPersonCase(
    val id: String,
    val caseNumber: String,
    val fullName: String,
    val age: Int,
    val priority: CasePriority,
    val conditionType: ConditionType,
    @DrawableRes val photoRes: Int? = null,
    val photoUri: String? = null,
    val verificationAuthority: String,
    val status: VerificationStatus,
    val reportedTimeAgo: String,
    val timestamp: Long = System.currentTimeMillis(),
    val lastSeenLocation: String,
    val latitude: Double,
    val longitude: Double,
    val searchRadiusKm: Double,
    val physicalAttributes: PhysicalAttributes,
    val rewardAmountToman: Long = 0L,
    val rewardDistributionPolicy: String = "",
    val familyEncryptedContact: String = "",
    val nearbyVolunteersCount: Int = 0,
    val highAlert: Boolean = false,
    val registeredByUserId: String? = null,
    val isPubliclyVisible: Boolean = true
)

/**
 * Report Status Lifecycle with Human-in-the-Loop Review
 */
enum class ReportStatus(val labelFa: String, val colorHex: Long) {
    SUBMITTED("ثبت شده توسط شهروند", 0xFF64748B),
    UNDER_REVIEW("در صف بررسی کارشناس", 0xFF3B82F6),
    VERIFIED("تأیید نهایی توسط کارمند مجاز", 0xFF10B981),
    REJECTED("رد شده پس از بررسی کارشناس", 0xFFEF4444),
    ESCALATED("ارجاع فوری به گشت عملیاتی", 0xFFDC2626)
}

data class CitizenReport(
    val id: String,
    val caseId: String,
    val reporterUserId: String,
    val reporterName: String,
    val reportedPersonName: String,
    val timestamp: Long,
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val photoUri: String? = null,
    @DrawableRes val photoRes: Int? = null,
    val audioUri: String? = null,
    val hasAudio: Boolean = false,
    val citizenNotes: String,
    val isEmergency: Boolean = false,
    val status: ReportStatus = ReportStatus.SUBMITTED,
    val potentialMatchScore: Int? = null,
    val aiConfidencePercent: Int? = null,
    val staffReviewNotes: String? = null,
    val reviewedByStaffName: String? = null,
    val reviewedTimestamp: Long? = null,
    val estimatedRewardContributionPoints: Int = 25
)

/**
 * AI Image / Face Analysis Result
 */
data class ImageQualityAssessment(
    val isAcceptable: Boolean,
    val lightingCondition: String, // e.g., "نور مناسب و یکنواخت" or "کم‌نور / سایه شدید"
    val angleAssessment: String,   // e.g., "زاویه روبرو (انحراف کمتر از ۱۰ درجه)"
    val clarityResolution: String, // e.g., "وضوح و فوکوس شفاف"
    val faceDetectedSize: String,  // e.g., "چهره با ابعاد استاندارد (بیش از ۲۵۰ پیکسل)"
    val warnings: List<String> = emptyList()
)

data class FaceAnalysisResult(
    val caseId: String,
    val potentialMatchPercent: Int, // 0 to 100
    val confidencePercent: Int,
    val quality: ImageQualityAssessment,
    val analysisTimestamp: Long = System.currentTimeMillis(),
    val isHumanReviewRequired: Boolean = true,
    val imageSourceDescription: String,
    val privacyEncryptedHash: String,
    val forensicDetails: String,
    val disclaimer: String = "این نتیجه فقط یک سرنخ احتمالی است و برای تأیید نیاز به بررسی انسانی دارد."
)

/**
 * Audio / Voice Analysis Result
 */
data class AudioAnalysisResult(
    val caseId: String,
    val formatValid: Boolean,
    val audioDurationSeconds: Double,
    val sampleRateHz: Int,
    val qualityScore: Int,
    val potentialVoiceMatchPercent: Int,
    val pitchCharacteristics: String,
    val disclaimer: String = "این نتیجه فقط یک سرنخ احتمالی است و برای تأیید نیاز به بررسی انسانی دارد."
)

/**
 * Security Audit Log for Traceability & Compliance
 */
data class AuditLogItem(
    val id: String,
    val timestamp: Long,
    val actorUserId: String,
    val actorUsername: String,
    val actorRole: UserRole,
    val actionType: String, // LOGIN, VIEW_CASE, SUBMIT_REPORT, APPROVE_REPORT, REJECT_REPORT, CHANGE_STATUS
    val targetResourceId: String,
    val details: String,
    val ipAddressOrClient: String = "TOPON-Android-Client"
)

/**
 * System & Priority Notifications
 */
enum class NotificationPriority {
    CRITICAL, HIGH, NORMAL
}

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: Long,
    val priority: NotificationPriority,
    val targetRole: UserRole? = null, // null for all
    val caseId: String? = null,
    val reportId: String? = null,
    val isRead: Boolean = false
)

/**
 * Reward and Contribution
 */
data class RewardContribution(
    val userId: String,
    val caseId: String,
    val reportId: String,
    val actionLabel: String,
    val pointsAwarded: Int,
    val estimatedTomanShare: Long,
    val isApprovedByStaff: Boolean,
    val timestamp: Long
)

/**
 * Incident Chat Message
 */
enum class SenderRole(val label: String, val badgeColorHex: Long) {
    POLICE("پلیس آگاهی / گشت انتظامی", 0xFF1E3A8A),
    RESCUE_PARAMEDIC("تیم امداد و نجات هلال احمر", 0xFFDC2626),
    CITIZEN_VOLUNTEER("شهروند داوطلب تاپان", 0xFF0D9488),
    FAMILY("خانواده مددجو", 0xFF7C3AED)
}

data class RescueChannelMessage(
    val id: String,
    val caseId: String,
    val senderName: String,
    val senderRole: SenderRole,
    val timeAgo: String,
    val text: String,
    val isEncrypted: Boolean = true,
    val hasAttachment: Boolean = false,
    val priorityHigh: Boolean = false
)

data class OfflineMapZone(
    val id: String,
    val title: String,
    val region: String,
    val isDownloaded: Boolean,
    val sizeMb: Int,
    val cachedCasesCount: Int
)

data class SightingReportItem(
    val id: String,
    val caseId: String,
    val personName: String,
    val locationName: String,
    val citizenNotes: String,
    val similarityScore: Int,
    val estimatedRewardShareToman: Long,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Section 40 & 41: Unified Case Management Types
 */
enum class CaseType(val labelFa: String, val badgeColorHex: Long) {
    MISSING_PERSON("فرد گمشده", 0xFF0D9488),
    WANTED_PERSON("فرد تحت تعقیب (مراجع رسمی)", 0xFFDC2626),
    ACCIDENT("گزارش تصادف", 0xFFF97316),
    DISASTER("حادثه و سوانح طبیعی", 0xFF8B5CF6),
    EMERGENCY("درخواست کمک اضطراری", 0xFFEF4444)
}

enum class DisasterType(val labelFa: String) {
    FIRE("آتش‌سوزی"),
    FLOOD("سیل و آب‌گرفتگی"),
    EARTHQUAKE("زلزله"),
    COLLAPSE("ریزش ساختمان و آوار"),
    ROAD_HAZARD("حادثه جاده‌ای و کوهستان"),
    INDUSTRIAL("حادثه صنعتی و کارگاهی"),
    OTHER("سایر حوادث اضطراری")
}

enum class AccidentType(val labelFa: String) {
    VEHICLE_COLLISION("تصادف خودرویی"),
    PEDESTRIAN("برخورد با عابر پیاده"),
    ROLLOVER("واژگونی خودرو"),
    MULTI_CAR("تصادف زنجیره‌ای"),
    MOTORCYCLE("تصادف موتورسیکلت")
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
    val fileHashSha256: String = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"
)

data class ChainOfCustodyEntry(
    val id: String,
    val evidenceId: String,
    val caseId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val actorName: String,
    val actorRole: UserRole,
    val action: String, // UPLOADED, VIEWED, STATUS_CHANGED, VERIFIED, EXPORTED
    val details: String
)

data class CasualtyAnalysisResult(
    val isFaceDetected: Boolean,
    val peopleCount: Int,
    val imageQualityScore: Int, // 0 - 100
    val lightingAssessment: String,
    val clarityAssessment: String,
    val obstructionNotes: String,
    val preliminaryStatus: String = "Potential Identification / Requires Verification",
    val ethicalDisclaimer: String = "این سامانه تشخیص پزشکی قطعی صادر نمی‌کند و هویت افراد را بدون تأیید نهاد مجاز به صورت عمومی منتشر نمی‌نماید."
)

data class PrivateChatMessage(
    val id: String,
    val caseId: String,
    val senderId: String,
    val senderName: String,
    val senderRole: UserRole,
    val timestamp: Long = System.currentTimeMillis(),
    val content: String,
    val attachmentUri: String? = null,
    val isVoiceNote: Boolean = false,
    val deliveryStatus: String = "SENT" // SENT, DELIVERED, READ
)

data class SocialMediaLead(
    val id: String,
    val caseId: String,
    val sourcePlatform: String, // e.g. "X (توییتر سابق) - پست عمومی", "کانال عمومی تلگرام", "اخبار رسمی حوادث"
    val publicPostUrlOrHandle: String,
    val contentSnippet: String,
    val detectedKeywords: List<String>,
    val locationMention: String?,
    val timestamp: Long = System.currentTimeMillis(),
    val aiConfidenceScore: Int,
    val status: String = "POTENTIAL_LEAD", // POTENTIAL_LEAD, UNDER_HUMAN_REVIEW, VERIFIED_LEAD, DISMISSED
    val complianceNote: String = "داده‌ها صرفاً از منابع عمومی و قانونی استخراج شده و هیچ‌گونه حریم خصوصی نقض نشده است."
)

data class ToponCase(
    val id: String,
    val caseNumber: String,
    val title: String,
    val caseType: CaseType,
    val priority: CasePriority,
    val status: VerificationStatus,
    val createdAt: Long = System.currentTimeMillis(),
    val lastUpdated: Long = System.currentTimeMillis(),
    val location: String,
    val latitude: Double,
    val longitude: Double,
    val description: String,
    val verificationAuthority: String,
    val assignedStaff: String? = null,
    val isPubliclyVisible: Boolean = true,
    val sensitiveConfidentialNotes: String? = null, // Only visible to Staff/Admin
    val officialWarrantNumber: String? = null, // For Wanted Persons
    val estimatedCasualties: Int? = null, // For Accident / Disaster
    val disasterSubtype: String? = null,
    val evidenceList: List<EvidenceItem> = emptyList(),
    val reportsCount: Int = 0,
    @DrawableRes val photoRes: Int? = null,
    val photoUri: String? = null
)
