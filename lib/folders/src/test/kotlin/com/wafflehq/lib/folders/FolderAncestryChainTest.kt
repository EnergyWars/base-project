package com.wafflehq.lib.folders

import org.junit.Assert.assertEquals
import org.junit.Test

class FolderAncestryChainTest {

    private data class Node(val id: Long, val parentId: Long?, val name: String)

    private val root = Node(1, null, "root")
    private val child = Node(2, 1, "child")
    private val grandchild = Node(3, 2, "grandchild")
    private val nodes = listOf(root, child, grandchild)

    private fun List<Node>.chain(folderId: Long?) =
        folderAncestryChain(folderId, id = { it.id }, parentId = { it.parentId })

    @Test
    fun `null folder id returns empty chain`() {
        assertEquals(emptyList<Node>(), nodes.chain(null))
    }

    @Test
    fun `root folder returns single item chain`() {
        assertEquals(listOf(root), nodes.chain(1))
    }

    @Test
    fun `nested folder returns chain from root to leaf`() {
        assertEquals(listOf(root, child, grandchild), nodes.chain(3))
    }

    @Test
    fun `unknown folder id returns empty chain`() {
        assertEquals(emptyList<Node>(), nodes.chain(999))
    }

    @Test
    fun `empty list returns empty chain`() {
        assertEquals(emptyList<Node>(), emptyList<Node>().chain(1))
    }

    @Test
    fun `chain is independent of list order`() {
        assertEquals(listOf(root, child, grandchild), listOf(grandchild, root, child).chain(3))
    }

    @Test
    fun `unknown parent stops chain at last known ancestor`() {
        val orphan = Node(5, 404, "orphan")
        assertEquals(listOf(orphan), listOf(orphan).chain(5))
    }

    @Test
    fun `cycle is cut without looping forever`() {
        val a = Node(1, 2, "a")
        val b = Node(2, 1, "b")
        assertEquals(listOf(b, a), listOf(a, b).chain(1))
    }

    @Test
    fun `self referencing folder returns itself once`() {
        val self = Node(7, 7, "self")
        assertEquals(listOf(self), listOf(self).chain(7))
    }
}
