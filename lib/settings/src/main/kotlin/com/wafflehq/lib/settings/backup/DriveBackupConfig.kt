package com.wafflehq.lib.settings.backup

class DriveBackupConfig(
    val fileNamePrefix: String,
    val driveApplicationName: String,
    val encryptedFileNameInfix: String = DEFAULT_ENCRYPTED_FILE_NAME_INFIX,
    val onSignedOut: suspend () -> Unit = {}
) {

    val encryptedFileNamePrefix: String = fileNamePrefix + encryptedFileNameInfix

    companion object {
        const val DEFAULT_ENCRYPTED_FILE_NAME_INFIX = "enc_"
    }
}
