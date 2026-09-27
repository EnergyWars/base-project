package com.wafflehq.lib.settings.backup

import android.net.Uri
import com.wafflehq.lib.settings.backup.access.BackupFileGateway
import com.wafflehq.lib.settings.backup.access.BackupScheduleGateway
import com.wafflehq.lib.settings.backup.access.RestoredEncryptionActivator
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

internal class RecordingBackupScheduler : BackupScheduleGateway {

    var scheduleCount = 0
    var cancelCount = 0

    override suspend fun schedule() { scheduleCount++ }

    override fun cancel() { cancelCount++ }
}

internal class InMemoryBackupFileGateway(
    var readContent: String = "imported-payload",
    var writable: Boolean = true,
    var readable: Boolean = true
) : BackupFileGateway {

    val written = ByteArrayOutputStream()

    override fun openForWrite(uri: Uri): OutputStream? = if (writable) written else null

    override fun openForRead(uri: Uri): InputStream? =
        if (readable) ByteArrayInputStream(readContent.toByteArray()) else null

    fun writtenText(): String = written.toByteArray().decodeToString()
}

internal class RecordingRestoredEncryptionActivator : RestoredEncryptionActivator {

    val activations = mutableListOf<Pair<String, String>>()

    override fun activate(recoveredDek: ByteArray, backupPassword: CharArray) {
        activations += recoveredDek.decodeToString() to backupPassword.concatToString()
    }
}
