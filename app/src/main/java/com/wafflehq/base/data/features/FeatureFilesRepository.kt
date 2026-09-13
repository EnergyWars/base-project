package com.wafflehq.base.data.features

import android.content.Context
import java.io.IOException

private const val ASSET_DIR = "features"

data class FeatureFile(
    val fileName: String,
    val title: String,
    val content: String,
)

class FeatureFilesRepository(private val context: Context) {

    fun list(): List<FeatureFile> = fileNames().map(::readFile)

    fun read(fileName: String): FeatureFile? =
        if (fileName in fileNames()) readFile(fileName) else null

    private fun fileNames(): List<String> = try {
        context.assets.list(ASSET_DIR)
            ?.filter { it.endsWith(".md") }
            ?.sorted()
            .orEmpty()
    } catch (e: IOException) {
        emptyList()
    }

    private fun readFile(fileName: String): FeatureFile {
        val content = context.assets.open("$ASSET_DIR/$fileName")
            .bufferedReader()
            .use { it.readText() }
        return FeatureFile(fileName = fileName, title = titleOf(fileName, content), content = content)
    }

    private fun titleOf(fileName: String, content: String): String =
        content.lineSequence()
            .firstOrNull { it.isNotBlank() }
            ?.trimStart('#', ' ')
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: fileName.removeSuffix(".md")
}
