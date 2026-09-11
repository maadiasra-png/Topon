package com.example.data.auth

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.UUID

object SecurityUtils {

    private val secureRandom = SecureRandom()

    /**
     * Generates a cryptographically secure 16-byte random hex salt
     */
    fun generateSalt(): String {
        val saltBytes = ByteArray(16)
        secureRandom.nextBytes(saltBytes)
        return saltBytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Computes SHA-256 hash of (password + salt)
     * Never stores or compares plain text passwords.
     */
    fun hashPassword(password: String, salt: String): String {
        val combined = "$password:$salt"
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(combined.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Verifies plain text password against stored salt and hash
     */
    fun verifyPassword(passwordPlain: String, salt: String, expectedHash: String): Boolean {
        val calculated = hashPassword(passwordPlain, salt)
        // Constant time comparison to prevent timing attacks
        return MessageDigest.isEqual(calculated.toByteArray(), expectedHash.toByteArray())
    }

    /**
     * Generates a cryptographically secure session token
     */
    fun generateSessionToken(): String {
        val randomBytes = ByteArray(32)
        secureRandom.nextBytes(randomBytes)
        val tokenHex = randomBytes.joinToString("") { "%02x".format(it) }
        return "tpn_sec_${UUID.randomUUID().toString().take(8)}_$tokenHex"
    }

    /**
     * Validates email address format
     */
    fun isValidEmail(email: String): Boolean {
        val pattern = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$"
        return email.trim().matches(pattern.toRegex())
    }

    /**
     * Validates Iranian phone number format (09xxxxxxxxx or Persian digits)
     */
    fun isValidPhone(phone: String): Boolean {
        val normalized = normalizeDigits(phone).replace("[^0-9]".toRegex(), "")
        return normalized.matches("^09[0-9]{9}$".toRegex())
    }

    /**
     * Validates password strength (minimum 6 chars, containing both letter and number)
     */
    fun validatePasswordStrength(password: String): Pair<Boolean, String?> {
        if (password.length < 6) {
            return Pair(false, "رمز عبور باید حداقل شامل ۶ کاراکتر باشد.")
        }
        val hasLetter = password.any { it.isLetter() }
        val hasDigit = password.any { it.isDigit() }
        if (!hasLetter || !hasDigit) {
            return Pair(false, "رمز عبور باید ترکیبی از حروف و اعداد باشد.")
        }
        return Pair(true, null)
    }

    /**
     * Converts Persian/Arabic digits to standard English digits
     */
    fun normalizeDigits(input: String): String {
        var result = input
        val persian = arrayOf("۰", "۱", "۲", "۳", "۴", "۵", "۶", "۷", "۸", "۹")
        val arabic = arrayOf("٠", "١", "٢", "٣", "٤", "٥", "٦", "٧", "٨", "٩")
        for (i in 0..9) {
            result = result.replace(persian[i], i.toString())
            result = result.replace(arabic[i], i.toString())
        }
        return result.trim()
    }
}

/**
 * In-memory Rate Limiter to prevent brute force attacks on Login and MFA
 * Locks identifier for 60 seconds after 5 consecutive failed attempts.
 */
class RateLimiter(
    private val maxAttempts: Int = 5,
    private val lockoutDurationMs: Long = 60_000L
) {
    private data class AttemptRecord(
        var failedCount: Int = 0,
        var lockedUntilTimestamp: Long = 0L
    )

    private val attempts = mutableMapOf<String, AttemptRecord>()

    @Synchronized
    fun isLocked(identifier: String): Pair<Boolean, Int> {
        val key = SecurityUtils.normalizeDigits(identifier.lowercase())
        val record = attempts[key] ?: return Pair(false, 0)

        val now = System.currentTimeMillis()
        if (record.lockedUntilTimestamp > now) {
            val remainingSec = ((record.lockedUntilTimestamp - now) / 1000).toInt() + 1
            return Pair(true, remainingSec)
        } else if (record.lockedUntilTimestamp != 0L) {
            // Lock expired, reset count
            record.failedCount = 0
            record.lockedUntilTimestamp = 0L
        }
        return Pair(false, 0)
    }

    @Synchronized
    fun recordFailedAttempt(identifier: String): Pair<Boolean, Int> {
        val key = SecurityUtils.normalizeDigits(identifier.lowercase())
        val record = attempts.getOrPut(key) { AttemptRecord() }
        record.failedCount++

        val now = System.currentTimeMillis()
        if (record.failedCount >= maxAttempts) {
            record.lockedUntilTimestamp = now + lockoutDurationMs
            val remainingSec = (lockoutDurationMs / 1000).toInt()
            return Pair(true, remainingSec)
        }
        return Pair(false, 0)
    }

    @Synchronized
    fun recordSuccess(identifier: String) {
        val key = SecurityUtils.normalizeDigits(identifier.lowercase())
        attempts.remove(key)
    }
}
