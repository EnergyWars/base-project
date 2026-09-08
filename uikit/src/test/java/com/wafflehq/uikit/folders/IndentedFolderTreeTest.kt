package com.wafflehq.uikit.folders

import org.junit.Assert.assertEquals
import org.junit.Test

private data class TreeFolder(val id: Long, val parentId: Long?, val name: String)

class IndentedFolderTreeTest {

    @Test
    fun `toIndentedFolderTree indents nested folders by depth`() {
        val folders = listOf(
            TreeFolder(1, null, "Root"),
            TreeFolder(2, 1, "Child"),
            TreeFolder(3, 2, "Grandchild")
        )

        val indented = folders.toIndentedFolderTree(
            id = { it.id },
            parentId = { it.parentId },
            name = { it.name }
        )

        assertEquals(listOf(1L to 0, 2L to 1, 3L to 2), indented.map { it.folder.id to it.depth })
    }

    @Test
    fun `toIndentedFolderTree sorts siblings case insensitively`() {
        val folders = listOf(
            TreeFolder(1, null, "banane"),
            TreeFolder(2, null, "Apfel")
        )

        val indented = folders.toIndentedFolderTree(
            id = { it.id },
            parentId = { it.parentId },
            name = { it.name }
        )

        assertEquals(listOf(2L, 1L), indented.map { it.folder.id })
    }

    @Test
    fun `toIndentedFolderTree ignores a folder reachable only through a cycle`() {
        val folders = listOf(
            TreeFolder(1, 2, "A"),
            TreeFolder(2, 1, "B")
        )

        val indented = folders.toIndentedFolderTree(
            id = { it.id },
            parentId = { it.parentId },
            name = { it.name }
        )

        assertEquals(emptyList<Long>(), indented.map { it.folder.id })
    }
}
