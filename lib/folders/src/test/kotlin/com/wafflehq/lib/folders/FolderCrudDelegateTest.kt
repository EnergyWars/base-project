package com.wafflehq.lib.folders

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FolderCrudDelegateTest {

    private val scope = TestScope(UnconfinedTestDispatcher())
    private val created = mutableListOf<Pair<String, Long?>>()
    private val renamed = mutableListOf<Pair<String, String>>()
    private val deleted = mutableListOf<Pair<String, FolderDeletionAction>>()

    private val delegate = FolderCrudDelegate<String>(
        scope = scope,
        create = { name, parent -> created += name to parent },
        rename = { folder, name -> renamed += folder to name },
        delete = { folder, action -> deleted += folder to action }
    )

    @Test
    fun createFolder_trimsNameAndPassesParent() {
        delegate.createFolder("  Kuchen \n", 7L)

        assertEquals(listOf<Pair<String, Long?>>("Kuchen" to 7L), created)
    }

    @Test
    fun createFolder_withoutParent_usesNullParent() {
        delegate.createFolder("Wurzel")

        assertEquals(listOf<Pair<String, Long?>>("Wurzel" to null), created)
    }

    @Test
    fun createFolder_blankName_isIgnored() {
        delegate.createFolder("   ")
        delegate.createFolder("")

        assertTrue(created.isEmpty())
    }

    @Test
    fun renameFolder_trimsName() {
        delegate.renameFolder("alt", "  neu ")

        assertEquals(listOf("alt" to "neu"), renamed)
    }

    @Test
    fun renameFolder_blankName_isIgnored() {
        delegate.renameFolder("alt", " ")

        assertTrue(renamed.isEmpty())
    }

    @Test
    fun deleteFolder_forwardsAction() {
        delegate.deleteFolder("a", FolderDeletionAction.DELETE_CONTENTS)
        delegate.deleteFolder("b", FolderDeletionAction.MOVE_CONTENTS_UP)

        assertEquals(
            listOf(
                "a" to FolderDeletionAction.DELETE_CONTENTS,
                "b" to FolderDeletionAction.MOVE_CONTENTS_UP
            ),
            deleted
        )
    }
}
