package com.wafflehq.lib.settings.backup.access

import android.content.ContentResolver
import android.net.Uri
import java.io.InputStream
import java.io.OutputStream

interface BackupFileGateway {

    fun openForWrite(uri: Uri): OutputStream?

    fun openForRead(uri: Uri): InputStream?
}

class ContentResolverBackupFileGateway(private val resolver: ContentResolver) : BackupFileGateway {

    override fun openForWrite(uri: Uri): OutputStream? = resolver.openOutputStream(uri)

    override fun openForRead(uri: Uri): InputStream? = resolver.openInputStream(uri)
}
