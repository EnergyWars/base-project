package com.wafflehq.lib.navigation.shell

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class NavShortcutsTest {

    private val all = setOf("a", "b", "c", "d", "e", "f", "g")

    @Test
    fun recordVisit_putsNewestFirst() {
        assertEquals(listOf("c", "a", "b"), NavShortcuts.recordVisit(listOf("a", "b"), "c"))
    }

    @Test
    fun recordVisit_movesExistingIdToFrontWithoutDuplicate() {
        assertEquals(listOf("b", "a", "c"), NavShortcuts.recordVisit(listOf("a", "b", "c"), "b"))
    }

    @Test
    fun recordVisit_sameIdAtFront_isUnchanged() {
        val recent = listOf("a", "b")
        assertEquals(recent, NavShortcuts.recordVisit(recent, "a"))
    }

    @Test
    fun recordVisit_blankId_isIgnored() {
        assertEquals(listOf("a"), NavShortcuts.recordVisit(listOf("a"), " "))
    }

    @Test
    fun recordVisit_respectsCapacity() {
        val recent = (1..MAX_RECENT_NAV_VISITS).map { "id$it" }
        val result = NavShortcuts.recordVisit(recent, "new")
        assertEquals(MAX_RECENT_NAV_VISITS, result.size)
        assertEquals("new", result.first())
        assertTrue("id${MAX_RECENT_NAV_VISITS}" !in result)
    }

    @Test
    fun recordVisit_withCustomCapacity() {
        assertEquals(listOf("c", "a"), NavShortcuts.recordVisit(listOf("a", "b"), "c", capacity = 2))
    }

    @Test
    fun resolve_recent_returnsFirstFourAvailable() {
        val result = NavShortcuts.resolve(
            mode = NavShortcutMode.RECENT,
            recent = listOf("g", "f", "e", "d", "c", "b", "a"),
            custom = listOf("a"),
            available = all
        )
        assertEquals(listOf("g", "f", "e", "d"), result)
    }

    @Test
    fun resolve_recent_skipsUnavailableAndFillsUp() {
        val result = NavShortcuts.resolve(
            mode = NavShortcutMode.RECENT,
            recent = listOf("x", "g", "y", "f", "e", "d", "c", "b"),
            custom = emptyList(),
            available = all
        )
        assertEquals(listOf("g", "f", "e", "d"), result)
    }

    @Test
    fun resolve_recent_removesDuplicates() {
        val result = NavShortcuts.resolve(NavShortcutMode.RECENT, listOf("a", "a", "b"), emptyList(), all)
        assertEquals(listOf("a", "b"), result)
    }

    @Test
    fun resolve_recent_withoutHistory_isEmpty() {
        assertTrue(NavShortcuts.resolve(NavShortcutMode.RECENT, emptyList(), listOf("a"), all).isEmpty())
    }

    @Test
    fun resolve_custom_keepsChosenOrderAndIgnoresRecent() {
        val result = NavShortcuts.resolve(NavShortcutMode.CUSTOM, listOf("g", "f"), listOf("c", "a", "b"), all)
        assertEquals(listOf("c", "a", "b"), result)
    }

    @Test
    fun resolve_custom_dropsDisabledModules() {
        val result = NavShortcuts.resolve(NavShortcutMode.CUSTOM, emptyList(), listOf("a", "gone", "b"), all)
        assertEquals(listOf("a", "b"), result)
    }

    @Test
    fun resolve_custom_isLimitedToFour() {
        val result = NavShortcuts.resolve(NavShortcutMode.CUSTOM, emptyList(), listOf("a", "b", "c", "d", "e", "f", "g"), all)
        assertEquals(MAX_NAV_SHORTCUTS, result.size)
        assertEquals(listOf("a", "b", "c", "d"), result)
    }

    @Test
    fun resolve_withoutAvailableModules_isEmpty() {
        assertTrue(NavShortcuts.resolve(NavShortcutMode.RECENT, listOf("a"), listOf("a"), emptySet()).isEmpty())
        assertTrue(NavShortcuts.resolve(NavShortcutMode.CUSTOM, listOf("a"), listOf("a"), emptySet()).isEmpty())
    }

    @Test
    fun resolve_negativeLimit_isTreatedAsZero() {
        assertTrue(NavShortcuts.resolve(NavShortcutMode.RECENT, listOf("a"), emptyList(), all, limit = -1).isEmpty())
    }

    @Test
    fun toggleCustom_addsAtEnd() {
        assertEquals(listOf("a", "b"), NavShortcuts.toggleCustom(listOf("a"), "b"))
    }

    @Test
    fun toggleCustom_removesExisting() {
        assertEquals(listOf("a", "c"), NavShortcuts.toggleCustom(listOf("a", "b", "c"), "b"))
    }

    @Test
    fun toggleCustom_atLimit_doesNotAdd() {
        val full = listOf("a", "b", "c", "d")
        assertEquals(full, NavShortcuts.toggleCustom(full, "f"))
    }

    @Test
    fun toggleCustom_atLimit_stillAllowsRemoval() {
        val full = listOf("a", "b", "c", "d")
        assertEquals(listOf("b", "c", "d"), NavShortcuts.toggleCustom(full, "a"))
    }

    @Test
    fun move_swapsWithNeighbour() {
        assertEquals(listOf("b", "a", "c"), NavShortcuts.move(listOf("a", "b", "c"), "b", -1))
        assertEquals(listOf("a", "c", "b"), NavShortcuts.move(listOf("a", "b", "c"), "b", 1))
    }

    @Test
    fun move_clampsAtBounds() {
        val list = listOf("a", "b", "c")
        assertEquals(list, NavShortcuts.move(list, "a", -1))
        assertEquals(list, NavShortcuts.move(list, "c", 1))
        assertEquals(listOf("c", "a", "b"), NavShortcuts.move(list, "c", -10))
    }

    @Test
    fun move_unknownId_isUnchanged() {
        val list = listOf("a", "b")
        assertEquals(list, NavShortcuts.move(list, "x", 1))
    }

    @Test
    fun encodeDecode_roundTrip() {
        val ids = listOf("weight", "todo", "notes")
        assertEquals(ids, NavShortcuts.decode(NavShortcuts.encode(ids)))
    }

    @Test
    fun encode_skipsBlankAndSeparatorIds() {
        assertEquals("a\nb", NavShortcuts.encode(listOf("a", " ", "x\ny", "b")))
    }

    @Test
    fun decode_nullOrEmpty_isEmpty() {
        assertTrue(NavShortcuts.decode(null).isEmpty())
        assertTrue(NavShortcuts.decode("").isEmpty())
    }

    @Test
    fun decode_removesBlanksAndDuplicates() {
        assertEquals(listOf("a", "b"), NavShortcuts.decode("a\n\nb\na"))
    }

    @Test
    fun mode_fromName_parsesAndFallsBackToRecent() {
        assertEquals(NavShortcutMode.CUSTOM, NavShortcutMode.fromName("CUSTOM"))
        assertEquals(NavShortcutMode.RECENT, NavShortcutMode.fromName("RECENT"))
        assertEquals(NavShortcutMode.RECENT, NavShortcutMode.fromName("bogus"))
        assertEquals(NavShortcutMode.RECENT, NavShortcutMode.fromName(null))
    }

    @Test
    fun maxShortcuts_isFour() {
        assertEquals(4, MAX_NAV_SHORTCUTS)
    }

    @Test
    fun mode_recentSortsAlphabetically_customKeepsOrder() {
        assertTrue(NavShortcutMode.RECENT.sortsAlphabetically)
        assertFalse(NavShortcutMode.CUSTOM.sortsAlphabetically)
    }

    @Test
    fun sortAlphabetically_ordersByLabelIgnoringCase() {
        val result = NavShortcuts.sortAlphabetically(listOf("weight", "Notes", "todo", "Alarm"), Locale.ENGLISH) { it }
        assertEquals(listOf("Alarm", "Notes", "todo", "weight"), result)
    }

    @Test
    fun sortAlphabetically_respectsGermanUmlauts() {
        val result = NavShortcuts.sortAlphabetically(listOf("Zeit", "Ärzte", "Bilder", "Apotheke"), Locale.GERMAN) { it }
        assertEquals(listOf("Apotheke", "Ärzte", "Bilder", "Zeit"), result)
    }

    @Test
    fun sortAlphabetically_usesLabelSelector() {
        val result = NavShortcuts.sortAlphabetically(listOf("x" to "Beta", "y" to "Alpha"), Locale.ENGLISH) { it.second }
        assertEquals(listOf("y" to "Alpha", "x" to "Beta"), result)
    }

    @Test
    fun sortAlphabetically_emptyAndSingle_areUnchanged() {
        assertTrue(NavShortcuts.sortAlphabetically(emptyList<String>(), Locale.ENGLISH) { it }.isEmpty())
        assertEquals(listOf("a"), NavShortcuts.sortAlphabetically(listOf("a"), Locale.ENGLISH) { it })
    }
}
