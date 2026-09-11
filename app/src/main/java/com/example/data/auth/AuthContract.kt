package com.example.data.auth

import com.example.data.model.User
import com.example.data.model.UserRole

/**
 * Data Transfer Objects (DTO) and API Contract for TOPON Authentication
 * Allows seamless switching between Room local repository and remote REST/Ktor backend.
 */
data class LoginRequest(
    val identifier: String,
    val passwordPlain: String,
    val rememberMe: Boolean = false
)

data class RegisterRequest(
    val fullName: String,
    val username: String,
    val email: String,
    val phone: String,
    val passwordPlain: String,
    val role: UserRole,
    val organization: String? = null,
    val authorizationCode: String? = null
)

data class PasswordResetRequest(
    val identifier: String
)

data class PasswordResetConfirmRequest(
    val identifier: String,
    val resetCode: String,
    val newPasswordPlain: String
)

data class UserSession(
    val token: String,
    val user: User,
    val issuedAt: Long,
    val expiresAt: Long,
    val rememberMe: Boolean
) {
    val isExpired: Boolean
        get() = System.currentTimeMillis() > expiresAt
}

sealed class AuthResult {
    data class Success(
        val session: UserSession,
        val targetDashboard: String
    ) : AuthResult()

    data class MfaRequired(
        val pendingUserId: String,
        val userRole: UserRole,
        val identifier: String,
        val message: String
    ) : AuthResult()

    data class Error(
        val message: String,
        val isRateLimited: Boolean = false,
        val retryAfterSeconds: Int = 0
    ) : AuthResult()
}

interface AuthService {
    suspend fun login(request: LoginRequest): AuthResult
    suspend fun verifyMfa(userId: String, otpCode: String, rememberMe: Boolean): AuthResult
    suspend fun register(request: RegisterRequest): AuthResult
    suspend fun requestPasswordReset(identifier: String): Result<String>
    suspend fun confirmPasswordReset(request: PasswordResetConfirmRequest): Result<Boolean>
    suspend fun logout(sessionToken: String?)
    suspend fun validateSession(): UserSession?
    fun isRouteAllowed(role: UserRole, targetRoute: String): Boolean
}
