package com.example.data.api

import com.example.data.model.CasePriority
import com.example.data.model.CaseType
import com.example.data.model.EvidenceItem
import com.example.data.model.PrivateChatMessage
import com.example.data.model.SocialMediaLead
import com.example.data.model.ToponCase
import com.example.data.model.UserRole
import com.example.data.model.VerificationStatus

/**
 * Sections 55, 56, 57, 58: Universal API Architecture Contract
 *
 * Designed for interoperability across:
 * - Android Native (Kotlin / Java)
 * - Cross-Platform (Flutter / Dart)
 * - Web (React / TypeScript)
 *
 * All clients consume this shared Backend REST API contract.
 * Security specifications:
 * - Bearer Token Auth (JWT with expiration & cryptographic signature)
 * - Role-Based Access Control (RBAC): CITIZEN, FAMILY, AUTHORIZED_STAFF, ADMIN
 * - Rate Limiting & Audit Logging on every sensitive endpoint
 * - Input sanitization & Secure File Upload verification (SHA-256)
 */

// Request & Response DTOs
data class ApiCaseFilterRequest(
    val caseType: CaseType? = null,
    val priority: CasePriority? = null,
    val status: VerificationStatus? = null,
    val boundingBox: GeoBoundingBox? = null,
    val limit: Int = 50,
    val offset: Int = 0
)

data class GeoBoundingBox(
    val northLat: Double,
    val southLat: Double,
    val eastLon: Double,
    val westLon: Double
)

data class CreateAccidentReportRequest(
    val accidentType: String,
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,
    val description: String,
    val estimatedCasualties: Int,
    val isEmergencyAmbulanceNeeded: Boolean,
    val photoBase64OrUri: String?,
    val videoUri: String? = null
)

data class CreateDisasterReportRequest(
    val disasterType: String,
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val severityLevel: String, // CRITICAL, HIGH, MODERATE
    val description: String,
    val mediaUriList: List<String> = emptyList()
)

data class UploadEvidenceRequest(
    val caseId: String,
    val evidenceType: String,
    val fileBytes: ByteArray?,
    val fileName: String,
    val sourceDescription: String
)

data class SendPrivateMessageRequest(
    val caseId: String,
    val content: String,
    val attachmentUri: String? = null,
    val isVoiceNote: Boolean = false
)

data class ApiResponse<T>(
    val success: Boolean,
    val statusCode: Int,
    val message: String,
    val data: T? = null,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Interface defining the universal TOPON Backend API Contract
 */
interface ToponApiClient {
    suspend fun getCases(filter: ApiCaseFilterRequest, userRole: UserRole): ApiResponse<List<ToponCase>>
    suspend fun getCaseDetails(caseId: String, userRole: UserRole): ApiResponse<ToponCase>
    suspend fun submitAccidentReport(request: CreateAccidentReportRequest, reporterUserId: String): ApiResponse<ToponCase>
    suspend fun submitDisasterReport(request: CreateDisasterReportRequest, reporterUserId: String): ApiResponse<ToponCase>
    suspend fun uploadEvidence(request: UploadEvidenceRequest, uploaderUserId: String): ApiResponse<EvidenceItem>
    suspend fun getCaseEvidence(caseId: String, userRole: UserRole): ApiResponse<List<EvidenceItem>>
    suspend fun getPrivateChat(caseId: String, userRole: UserRole, userId: String): ApiResponse<List<PrivateChatMessage>>
    suspend fun sendPrivateMessage(request: SendPrivateMessageRequest, senderUserId: String): ApiResponse<PrivateChatMessage>
    suspend fun getSocialMediaLeads(caseId: String, userRole: UserRole): ApiResponse<List<SocialMediaLead>>
}
