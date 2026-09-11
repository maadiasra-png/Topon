package com.example.data.database

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "citizen_reports")
data class ReportEntity(
    @PrimaryKey val id: String,
    val caseId: String,
    val reporterUserId: String,
    val reporterName: String,
    val reportedPersonName: String,
    val timestamp: Long,
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val photoUri: String?,
    val photoRes: Int?,
    val audioUri: String?,
    val hasAudio: Boolean,
    val citizenNotes: String,
    val isEmergency: Boolean,
    val status: String, // SUBMITTED, UNDER_REVIEW, VERIFIED, REJECTED, ESCALATED
    val potentialMatchScore: Int?,
    val aiConfidencePercent: Int?,
    val staffReviewNotes: String?,
    val reviewedByStaffName: String?,
    val reviewedTimestamp: Long?,
    val estimatedRewardContributionPoints: Int
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey val id: String,
    val timestamp: Long,
    val actorUserId: String,
    val actorUsername: String,
    val actorRole: String,
    val actionType: String,
    val targetResourceId: String,
    val details: String,
    val ipAddressOrClient: String
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val message: String,
    val timestamp: Long,
    val priority: String, // CRITICAL, HIGH, NORMAL
    val targetRole: String?,
    val caseId: String?,
    val reportId: String?,
    val isRead: Boolean
)

@Entity(tableName = "user_accounts")
data class UserAccountEntity(
    @PrimaryKey val id: String,
    val username: String,
    val email: String,
    val phone: String,
    val fullName: String,
    val passwordSalt: String,
    val passwordHash: String,
    val role: String, // CITIZEN, FAMILY, AUTHORIZED_STAFF, ADMIN
    val organization: String?,
    val badgeNumber: String?,
    val contributionPoints: Int,
    val isMfaEnabled: Boolean,
    val createdAt: Long
)

@Entity(tableName = "topon_cases")
data class ToponCaseEntity(
    @PrimaryKey val id: String,
    val caseNumber: String,
    val title: String,
    val caseType: String,
    val priority: String,
    val status: String,
    val createdAt: Long,
    val lastUpdated: Long,
    val location: String,
    val latitude: Double,
    val longitude: Double,
    val description: String,
    val verificationAuthority: String,
    val assignedStaff: String?,
    val isPubliclyVisible: Boolean,
    val sensitiveConfidentialNotes: String?,
    val officialWarrantNumber: String?,
    val estimatedCasualties: Int?,
    val disasterSubtype: String?,
    val reportsCount: Int,
    val photoRes: Int?,
    val photoUri: String?
)

@Entity(tableName = "evidence_items")
data class EvidenceEntity(
    @PrimaryKey val id: String,
    val caseId: String,
    val type: String,
    val title: String,
    val mediaUri: String?,
    val source: String,
    val uploadTime: Long,
    val uploadedByUserId: String,
    val uploadedByName: String,
    val uploadedByRole: String,
    val verificationStatus: String,
    val fileHashSha256: String
)

@Entity(tableName = "chain_of_custody_logs")
data class ChainOfCustodyEntity(
    @PrimaryKey val id: String,
    val evidenceId: String,
    val caseId: String,
    val timestamp: Long,
    val actorName: String,
    val actorRole: String,
    val action: String,
    val details: String
)

@Entity(tableName = "private_chat_messages")
data class PrivateChatMessageEntity(
    @PrimaryKey val id: String,
    val caseId: String,
    val senderId: String,
    val senderName: String,
    val senderRole: String,
    val timestamp: Long,
    val content: String,
    val attachmentUri: String?,
    val isVoiceNote: Boolean,
    val deliveryStatus: String
)

@Entity(tableName = "social_media_leads")
data class SocialMediaLeadEntity(
    @PrimaryKey val id: String,
    val caseId: String,
    val sourcePlatform: String,
    val publicPostUrlOrHandle: String,
    val contentSnippet: String,
    val detectedKeywordsCsv: String,
    val locationMention: String?,
    val timestamp: Long,
    val aiConfidenceScore: Int,
    val status: String,
    val complianceNote: String
)

@Dao
interface ToponCaseDao {
    @Query("SELECT * FROM topon_cases ORDER BY lastUpdated DESC")
    fun getAllCases(): Flow<List<ToponCaseEntity>>

    @Query("SELECT * FROM topon_cases WHERE caseType = :caseType ORDER BY lastUpdated DESC")
    fun getCasesByType(caseType: String): Flow<List<ToponCaseEntity>>

    @Query("SELECT * FROM topon_cases WHERE id = :id LIMIT 1")
    suspend fun getCaseById(id: String): ToponCaseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCase(caseEntity: ToponCaseEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCases(cases: List<ToponCaseEntity>)

    @Update
    suspend fun updateCase(caseEntity: ToponCaseEntity)

    @Query("UPDATE topon_cases SET status = :status, lastUpdated = :timestamp WHERE id = :id")
    suspend fun updateCaseStatus(id: String, status: String, timestamp: Long)
}

@Dao
interface EvidenceDao {
    @Query("SELECT * FROM evidence_items WHERE caseId = :caseId ORDER BY uploadTime DESC")
    fun getEvidenceForCase(caseId: String): Flow<List<EvidenceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvidence(evidence: EvidenceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvidenceList(evidenceList: List<EvidenceEntity>)

    @Query("UPDATE evidence_items SET verificationStatus = :status WHERE id = :id")
    suspend fun updateVerificationStatus(id: String, status: String)
}

@Dao
interface ChainOfCustodyDao {
    @Query("SELECT * FROM chain_of_custody_logs WHERE caseId = :caseId ORDER BY timestamp DESC")
    fun getLogsForCase(caseId: String): Flow<List<ChainOfCustodyEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ChainOfCustodyEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogs(logs: List<ChainOfCustodyEntity>)
}

@Dao
interface PrivateChatDao {
    @Query("SELECT * FROM private_chat_messages WHERE caseId = :caseId ORDER BY timestamp ASC")
    fun getMessagesForCase(caseId: String): Flow<List<PrivateChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: PrivateChatMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<PrivateChatMessageEntity>)
}

@Dao
interface SocialMediaLeadDao {
    @Query("SELECT * FROM social_media_leads WHERE caseId = :caseId ORDER BY timestamp DESC")
    fun getLeadsForCase(caseId: String): Flow<List<SocialMediaLeadEntity>>

    @Query("SELECT * FROM social_media_leads ORDER BY timestamp DESC")
    fun getAllLeads(): Flow<List<SocialMediaLeadEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLead(lead: SocialMediaLeadEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeads(leads: List<SocialMediaLeadEntity>)

    @Query("UPDATE social_media_leads SET status = :status WHERE id = :id")
    suspend fun updateLeadStatus(id: String, status: String)
}

@Dao
interface ReportDao {
    @Query("SELECT * FROM citizen_reports ORDER BY timestamp DESC")
    fun getAllReports(): Flow<List<ReportEntity>>

    @Query("SELECT * FROM citizen_reports WHERE reporterUserId = :userId ORDER BY timestamp DESC")
    fun getReportsByReporter(userId: String): Flow<List<ReportEntity>>

    @Query("SELECT * FROM citizen_reports WHERE caseId = :caseId ORDER BY timestamp DESC")
    fun getReportsForCase(caseId: String): Flow<List<ReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ReportEntity)

    @Update
    suspend fun updateReport(report: ReportEntity)

    @Query("UPDATE citizen_reports SET status = :status, staffReviewNotes = :notes, reviewedByStaffName = :reviewer, reviewedTimestamp = :time WHERE id = :reportId")
    suspend fun updateReviewStatus(reportId: String, status: String, notes: String, reviewer: String, time: Long)
}

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AuditLogEntity)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)
}

@Dao
interface UserAccountDao {
    @Query("SELECT * FROM user_accounts WHERE username = :identifier OR email = :identifier OR phone = :identifier LIMIT 1")
    suspend fun findByIdentifier(identifier: String): UserAccountEntity?

    @Query("SELECT * FROM user_accounts WHERE id = :userId LIMIT 1")
    suspend fun findById(userId: String): UserAccountEntity?

    @Query("SELECT COUNT(*) FROM user_accounts")
    suspend fun getUserCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserAccountEntity)

    @Query("UPDATE user_accounts SET passwordHash = :hash, passwordSalt = :salt WHERE id = :userId")
    suspend fun updatePassword(userId: String, hash: String, salt: String)
}

@Database(
    entities = [
        ReportEntity::class,
        AuditLogEntity::class,
        NotificationEntity::class,
        UserAccountEntity::class,
        ToponCaseEntity::class,
        EvidenceEntity::class,
        ChainOfCustodyEntity::class,
        PrivateChatMessageEntity::class,
        SocialMediaLeadEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun reportDao(): ReportDao
    abstract fun auditLogDao(): AuditLogDao
    abstract fun notificationDao(): NotificationDao
    abstract fun userAccountDao(): UserAccountDao
    abstract fun toponCaseDao(): ToponCaseDao
    abstract fun evidenceDao(): EvidenceDao
    abstract fun chainOfCustodyDao(): ChainOfCustodyDao
    abstract fun privateChatDao(): PrivateChatDao
    abstract fun socialMediaLeadDao(): SocialMediaLeadDao
}
