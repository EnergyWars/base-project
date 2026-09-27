package com.wafflehq.lib.settings.encryption

enum class PasswordReminderInterval {
    NEVER, EVERY_LAUNCH, DAILY, WEEKLY, MONTHLY;

    companion object {
        fun fromOrdinal(value: Int?): PasswordReminderInterval =
            entries.getOrNull(value ?: -1) ?: NEVER
    }
}
