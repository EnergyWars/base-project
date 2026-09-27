package com.wafflehq.base.data.features

import com.wafflehq.base.testutil.FakeAssetContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FeatureFilesRepositoryTest {

    @Test
    fun `list returns markdown files sorted by name with titles from the first heading`() {
        val repository = FeatureFilesRepository(
            FakeAssetContext.withFiles(
                mapOf(
                    "b.md" to "\n\n## Beta feature\ntext",
                    "a.md" to "# Alpha feature\ntext",
                    "notes.txt" to "ignored"
                )
            )
        )

        val files = repository.list()

        assertEquals(listOf("a.md", "b.md"), files.map { it.fileName })
        assertEquals(listOf("Alpha feature", "Beta feature"), files.map { it.title })
        assertEquals("# Alpha feature\ntext", files.first().content)
    }

    @Test
    fun `title falls back to the file name for blank or heading only content`() {
        val repository = FeatureFilesRepository(
            FakeAssetContext.withFiles(mapOf("empty.md" to "   \n", "hash.md" to "#  "))
        )

        val titles = repository.list().associate { it.fileName to it.title }

        assertEquals("empty", titles.getValue("empty.md"))
        assertEquals("hash", titles.getValue("hash.md"))
    }

    @Test
    fun `read returns a known file and null for unknown names`() {
        val repository = FeatureFilesRepository(FakeAssetContext.withFiles(mapOf("a.md" to "# Alpha")))

        assertEquals("Alpha", repository.read("a.md")?.title)
        assertNull(repository.read("../secret.md"))
        assertNull(repository.read("missing.md"))
    }

    @Test
    fun `missing asset directory yields an empty list`() {
        val repository = FeatureFilesRepository(FakeAssetContext.withMissingDirectory())

        assertTrue(repository.list().isEmpty())
        assertNull(repository.read("a.md"))
    }

    @Test
    fun `io failure while listing yields an empty list`() {
        val repository = FeatureFilesRepository(FakeAssetContext.failingToList())

        assertTrue(repository.list().isEmpty())
        assertNull(repository.read("a.md"))
    }
}
