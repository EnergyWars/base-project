package com.wafflehq.lib.uicore.io

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream

fun InputStream.readBytesLimited(
    maxBytes: Long,
    onExceeded: () -> Throwable = { IOException("Stream exceeded the allowed limit of $maxBytes bytes") }
): ByteArray {
    val buffer = ByteArrayOutputStream()
    val chunk = ByteArray(DEFAULT_BUFFER_SIZE)
    var total = 0L
    while (true) {
        val read = read(chunk)
        if (read == -1) break
        total += read
        if (total > maxBytes) throw onExceeded()
        buffer.write(chunk, 0, read)
    }
    return buffer.toByteArray()
}
