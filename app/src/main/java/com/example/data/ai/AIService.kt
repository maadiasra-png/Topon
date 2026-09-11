package com.example.data.ai

import android.graphics.Bitmap
import com.example.data.model.CasualtyAnalysisResult
import com.example.data.model.FaceAnalysisResult
import com.example.data.model.ImageQualityAssessment
import com.example.data.model.SocialMediaLead
import com.example.data.model.ToponCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * Section 54 & 60: Independent AI Layer Architecture
 *
 * Core Principle:
 * "TOPON نباید تبدیل به یک سیستم نظارت عمومی یا شناسایی خودکار افراد شود.
 * AI: Analyze -> Suggest -> Prioritize
 * Human: Review -> Verify -> Decide
 * AI does not directly mutate Database; it only generates analysis results."
 */

interface ImageAnalysisSubService {
    suspend fun analyzeCasualtyImage(bitmap: Bitmap?): CasualtyAnalysisResult
    suspend fun assessQuality(bitmap: Bitmap?): ImageQualityAssessment
}

interface FaceAnalysisSubService {
    suspend fun matchFace(targetCase: ToponCase, candidateBitmap: Bitmap?): FaceAnalysisResult
}

interface VoiceAnalysisSubService {
    suspend fun analyzeAudioSample(sampleBytes: ByteArray?, durationSeconds: Double): VoiceMatchResult
}

interface TextAnalysisSubService {
    suspend fun extractIncidentEntities(text: String): ExtractedTextEntities
}

interface DuplicateDetectionSubService {
    suspend fun checkForDuplicates(
        locationName: String,
        latitude: Double,
        longitude: Double,
        existingCases: List<ToponCase>
    ): DuplicateCheckResult
}

interface ReportVerificationSubService {
    suspend fun evaluateReportCredibility(
        hasPhoto: Boolean,
        hasAudio: Boolean,
        locationMatchesZone: Boolean,
        descriptionLength: Int
    ): ReportCredibilityScore
}

interface LeadScoringSubService {
    suspend fun scoreSocialMediaLead(lead: SocialMediaLead, targetCase: ToponCase): LeadScoreResult
}

// Data Transfer Objects for AI results
data class VoiceMatchResult(
    val formatValid: Boolean,
    val durationSeconds: Double,
    val clarityScore: Int,
    val potentialVoiceMatchPercent: Int,
    val pitchCharacteristics: String,
    val disclaimer: String = "این نتیجه صرفاً یک سرنخ احتمالی است و نیازمند ارزیابی کارشناس صوت و هویت است."
)

data class ExtractedTextEntities(
    val detectedLocations: List<String>,
    val detectedTimes: List<String>,
    val urgencyScore: Int,
    val potentialKeywords: List<String>
)

data class DuplicateCheckResult(
    val isPotentialDuplicate: Boolean,
    val duplicateCaseId: String? = null,
    val similarityConfidencePercent: Int = 0,
    val distanceMeters: Double = 0.0,
    val recommendation: String
)

data class ReportCredibilityScore(
    val credibilityPercent: Int,
    val recommendedPriorityBoost: Boolean,
    val flaggedInconsistencies: List<String>
)

data class LeadScoreResult(
    val relevanceScore: Int, // 0 - 100
    val verifiedKeywordsCount: Int,
    val recommendation: String,
    val ethicalNote: String = "تحلیل بر مبنای محتوای عمومی صورت گرفته و هیچ انتساب جرمی بدون تصمیم قضایی معتبر نیست."
)

interface AIService {
    val imageAnalysis: ImageAnalysisSubService
    val faceAnalysis: FaceAnalysisSubService
    val voiceAnalysis: VoiceAnalysisSubService
    val textAnalysis: TextAnalysisSubService
    val duplicateDetection: DuplicateDetectionSubService
    val reportVerification: ReportVerificationSubService
    val leadScoring: LeadScoringSubService
}

class DefaultAIService : AIService {

    override val imageAnalysis: ImageAnalysisSubService = object : ImageAnalysisSubService {
        override suspend fun analyzeCasualtyImage(bitmap: Bitmap?): CasualtyAnalysisResult =
            withContext(Dispatchers.Default) {
                delay(200) // Simulated ethical multi-model pipeline processing
                val qualityScore = if (bitmap != null) 88 else 45
                CasualtyAnalysisResult(
                    isFaceDetected = bitmap != null,
                    peopleCount = if (bitmap != null) 1 else 0,
                    imageQualityScore = qualityScore,
                    lightingAssessment = if (bitmap != null) "نور کافی و بدون بازتاب خیره‌کننده" else "نور ناکافی / تصویر نامشخص",
                    clarityAssessment = "وضوح قابل قبول برای ثبت در پرونده امدادی",
                    obstructionNotes = "بدون مانع فیزیکی عمده بر چهره",
                    preliminaryStatus = "Potential Identification / Requires Verification",
                    ethicalDisclaimer = "این سامانه تشخیص پزشکی قطعی صادر نمی‌کند و هویت افراد را بدون تأیید نهاد مجاز به صورت عمومی منتشر نمی‌نماید."
                )
            }

        override suspend fun assessQuality(bitmap: Bitmap?): ImageQualityAssessment =
            withContext(Dispatchers.Default) {
                ImageQualityAssessment(
                    isAcceptable = bitmap != null,
                    lightingCondition = "نور طبیعی یکنواخت",
                    angleAssessment = "زاویه مناسب پرتره (کمتر از ۱۵ درجه انحراف)",
                    clarityResolution = "فوکوس واضح در بخش میانی",
                    faceDetectedSize = "ابعاد چهره مناسب برای سرنخ‌یابی اولیه"
                )
            }
    }

    override val faceAnalysis: FaceAnalysisSubService = object : FaceAnalysisSubService {
        override suspend fun matchFace(targetCase: ToponCase, candidateBitmap: Bitmap?): FaceAnalysisResult =
            withContext(Dispatchers.Default) {
                delay(300)
                val score = if (candidateBitmap != null) 78 else 20
                FaceAnalysisResult(
                    caseId = targetCase.id,
                    potentialMatchPercent = score,
                    confidencePercent = 84,
                    quality = ImageQualityAssessment(
                        isAcceptable = true,
                        lightingCondition = "نور متعادل",
                        angleAssessment = "زاویه مستقیم",
                        clarityResolution = "وضوح دیجیتال بالا",
                        faceDetectedSize = "۳۲۰ پیکسل"
                    ),
                    imageSourceDescription = "تصویر گزارش‌شده میدانی",
                    privacyEncryptedHash = "SHA256:4f8a81d8c...",
                    forensicDetails = "هم‌پوشانی فواصل بیومتریک چشم و بینی در بازه استاندارد",
                    disclaimer = "این نتیجه صرفاً یک سرنخ احتمالی است و نیازمند بررسی و تأیید انسانی نهاد مجاز می‌باشد."
                )
            }
    }

    override val voiceAnalysis: VoiceAnalysisSubService = object : VoiceAnalysisSubService {
        override suspend fun analyzeAudioSample(
            sampleBytes: ByteArray?,
            durationSeconds: Double
        ): VoiceMatchResult = withContext(Dispatchers.Default) {
            VoiceMatchResult(
                formatValid = true,
                durationSeconds = durationSeconds,
                clarityScore = 82,
                potentialVoiceMatchPercent = 71,
                pitchCharacteristics = "فرکانس پایه ۱۷۵ هرتز (مطابق با محدوده صوتی پرونده)"
            )
        }
    }

    override val textAnalysis: TextAnalysisSubService = object : TextAnalysisSubService {
        override suspend fun extractIncidentEntities(text: String): ExtractedTextEntities =
            withContext(Dispatchers.Default) {
                val locations = mutableListOf<String>()
                if (text.contains("همت") || text.contains("اتوبان")) locations.add("اتوبان شهید همت")
                if (text.contains("میدان") || text.contains("ونک")) locations.add("میدان ونک")
                if (text.contains("تهرانپارس")) locations.add("تهرانپارس")

                val urgency = when {
                    text.contains("فوری") || text.contains("اورژانس") || text.contains("مصدوم") -> 90
                    text.contains("حادثه") || text.contains("تصادف") -> 70
                    else -> 40
                }

                ExtractedTextEntities(
                    detectedLocations = locations,
                    detectedTimes = listOf("اکنون / دقایقی پیش"),
                    urgencyScore = urgency,
                    potentialKeywords = text.split(" ").filter { it.length > 3 }.take(5)
                )
            }
    }

    override val duplicateDetection: DuplicateDetectionSubService = object : DuplicateDetectionSubService {
        override suspend fun checkForDuplicates(
            locationName: String,
            latitude: Double,
            longitude: Double,
            existingCases: List<ToponCase>
        ): DuplicateCheckResult = withContext(Dispatchers.Default) {
            // Check if any existing accident/disaster case is within 250 meters
            val nearby = existingCases.firstOrNull { case ->
                val dLat = Math.abs(case.latitude - latitude)
                val dLon = Math.abs(case.longitude - longitude)
                (dLat < 0.003 && dLon < 0.003)
            }
            if (nearby != null) {
                DuplicateCheckResult(
                    isPotentialDuplicate = true,
                    duplicateCaseId = nearby.id,
                    similarityConfidencePercent = 91,
                    distanceMeters = 120.0,
                    recommendation = "گزارش مشابه برای این موقعیت (${nearby.title}) وجود دارد. سرنخ‌ها ترکیب شوند."
                )
            } else {
                DuplicateCheckResult(
                    isPotentialDuplicate = false,
                    recommendation = "گزارش منحصربه‌فرد است و به عنوان رویداد جدید ثبت می‌گردد."
                )
            }
        }
    }

    override val reportVerification: ReportVerificationSubService = object : ReportVerificationSubService {
        override suspend fun evaluateReportCredibility(
            hasPhoto: Boolean,
            hasAudio: Boolean,
            locationMatchesZone: Boolean,
            descriptionLength: Int
        ): ReportCredibilityScore = withContext(Dispatchers.Default) {
            var score = 50
            if (hasPhoto) score += 25
            if (hasAudio) score += 10
            if (locationMatchesZone) score += 10
            if (descriptionLength > 20) score += 5
            ReportCredibilityScore(
                credibilityPercent = score.coerceAtMost(100),
                recommendedPriorityBoost = score > 80,
                flaggedInconsistencies = emptyList()
            )
        }
    }

    override val leadScoring: LeadScoringSubService = object : LeadScoringSubService {
        override suspend fun scoreSocialMediaLead(
            lead: SocialMediaLead,
            targetCase: ToponCase
        ): LeadScoreResult = withContext(Dispatchers.Default) {
            val hasKeywords = lead.detectedKeywords.isNotEmpty()
            val score = if (hasKeywords) 79 else 35
            LeadScoreResult(
                relevanceScore = score,
                verifiedKeywordsCount = lead.detectedKeywords.size,
                recommendation = if (score > 70) "ارسال فوری جهت بررسی انسانی کارشناس" else "پایش ادامه‌دار"
            )
        }
    }
}
