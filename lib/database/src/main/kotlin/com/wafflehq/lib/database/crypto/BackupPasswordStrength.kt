package com.wafflehq.lib.database.crypto

enum class PasswordStrength { WEAK, MEDIUM, STRONG }

object BackupPasswordStrength {

    const val MIN_LENGTH = 10

    fun evaluate(password: String): PasswordStrength {
        if (password.length < MIN_LENGTH) return PasswordStrength.WEAK
        if (isTrivial(password)) return PasswordStrength.WEAK

        val varietyCount = listOf(
            password.any { it.isLowerCase() },
            password.any { it.isUpperCase() },
            password.any { it.isDigit() },
            password.any { !it.isLetterOrDigit() }
        ).count { it }

        return when {
            password.length >= 14 && varietyCount >= 3 -> PasswordStrength.STRONG
            varietyCount >= 2 -> PasswordStrength.MEDIUM
            else -> PasswordStrength.WEAK
        }
    }

    private fun isTrivial(password: String): Boolean {
        val lower = password.lowercase()
        return COMMON_PASSWORDS.any { lower.contains(it) } || isSingleCharacter(password) || isSequentialRun(lower)
    }

    private fun isSingleCharacter(password: String): Boolean = password.toSet().size <= 1

    private fun isSequentialRun(lower: String): Boolean {
        if (lower.length < 4) return false
        var ascending = true
        var descending = true
        for (i in 1 until lower.length) {
            val diff = lower[i].code - lower[i - 1].code
            if (diff != 1) ascending = false
            if (diff != -1) descending = false
        }
        return ascending || descending
    }

    private val COMMON_PASSWORDS = listOf(
        "password", "passwort", "123456", "12345678", "qwertz", "qwerty", "asdfgh", "letmein",
        "iloveyou", "admin123", "welcome", "monkey", "dragon", "sunshine", "master", "abc123",
        "trustno1", "changeme"
    )
}
