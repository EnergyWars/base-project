package com.wafflehq.lib.settings.encryption

sealed interface PasswordTestResult {
    data object Idle : PasswordTestResult
    data object Checking : PasswordTestResult
    data object Correct : PasswordTestResult
    data object Incorrect : PasswordTestResult
}
