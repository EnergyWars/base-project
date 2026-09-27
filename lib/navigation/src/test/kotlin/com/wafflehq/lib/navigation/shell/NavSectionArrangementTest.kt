package com.wafflehq.lib.navigation.shell

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class NavSectionArrangementTest {

    private fun item(label: String, id: String = label, pinned: Boolean = false) = AppNavItem(
        label = label,
        icon = Icons.Default.Home,
        selected = false,
        id = id,
        pinned = pinned,
        onClick = {}
    )

    private fun arrange(sections: List<AppNavSection>, locale: Locale = Locale.ENGLISH) =
        arrangeNavSections(sections, pinnedLabel = "Pinned", locale = locale)

    @Test
    fun itemsAreSortedAlphabeticallyWithinEachSection() {
        val result = arrange(
            listOf(
                AppNavSection(label = "Pages", items = listOf(item("Search"), item("Calendar"))),
                AppNavSection(label = "Tools", items = listOf(item("Weight"), item("Alcohol"), item("Notes")))
            )
        )
        assertEquals(listOf("Calendar", "Search"), result[0].items.map { it.label })
        assertEquals(listOf("Alcohol", "Notes", "Weight"), result[1].items.map { it.label })
    }

    @Test
    fun sectionOrderIsPreserved() {
        val result = arrange(
            listOf(
                AppNavSection(label = "Zeta", items = listOf(item("A"))),
                AppNavSection(label = "Alpha", items = listOf(item("B")))
            )
        )
        assertEquals(listOf("Zeta", "Alpha"), result.map { it.label })
    }

    @Test
    fun sortingIgnoresCase() {
        val result = arrange(listOf(AppNavSection(items = listOf(item("beta"), item("Alpha"), item("gamma")))))
        assertEquals(listOf("Alpha", "beta", "gamma"), result.single().items.map { it.label })
    }

    @Test
    fun sortingUsesLocaleRulesForUmlauts() {
        val result = arrange(
            listOf(AppNavSection(items = listOf(item("Zebra"), item("Übung"), item("Ofen")))),
            locale = Locale.GERMAN
        )
        assertEquals(listOf("Ofen", "Übung", "Zebra"), result.single().items.map { it.label })
    }

    @Test
    fun pinnedItemsAreCopiedToLeadingPinnedSectionAndStayInTheirOriginalSection() {
        val result = arrange(
            listOf(
                AppNavSection(label = "Pages", items = listOf(item("Calendar"), item("Search", pinned = true))),
                AppNavSection(label = "Tools", items = listOf(item("Weight", pinned = true), item("Alcohol")))
            )
        )
        assertEquals(listOf("Pinned", "Pages", "Tools"), result.map { it.label })
        assertTrue(result[0].pinned)
        assertEquals(listOf("Search", "Weight"), result[0].items.map { it.label })
        assertEquals(listOf("Calendar", "Search"), result[1].items.map { it.label })
        assertEquals(listOf("Alcohol", "Weight"), result[2].items.map { it.label })
    }

    @Test
    fun pinnedItemsAreSortedAlphabeticallyAcrossSections() {
        val result = arrange(
            listOf(
                AppNavSection(label = "A", items = listOf(item("Zoo", pinned = true))),
                AppNavSection(label = "B", items = listOf(item("Ant", pinned = true), item("Bee", pinned = true)))
            )
        )
        assertEquals(listOf("Ant", "Bee", "Zoo"), result.first().items.map { it.label })
    }

    @Test
    fun noPinnedItems_omitsPinnedSection() {
        val result = arrange(listOf(AppNavSection(label = "Pages", items = listOf(item("Calendar")))))
        assertEquals(listOf("Pages"), result.map { it.label })
    }

    @Test
    fun onlyPinnedSectionIsFlaggedAsPinned() {
        val result = arrange(listOf(AppNavSection(label = "Pages", items = listOf(item("Calendar", pinned = true)))))
        assertEquals(listOf(true, false), result.map { it.pinned })
    }

    @Test
    fun emptyInput_returnsEmptyList() {
        assertTrue(arrange(emptyList()).isEmpty())
    }

    @Test
    fun emptySectionInInput_isDropped() {
        val result = arrange(listOf(AppNavSection(label = "Empty", items = emptyList()), AppNavSection(items = listOf(item("A")))))
        assertEquals(1, result.size)
    }

    @Test
    fun itemsKeepTheirIdentityAndFlags() {
        val original = item("Weight", id = "module:weight", pinned = true)
        val result = arrange(listOf(AppNavSection(label = "Tools", items = listOf(original))))
        assertEquals("module:weight", result.first().items.single().id)
        assertTrue(result.first().items.single().pinned)
    }

    @Test
    fun defaultIdIsLabel() {
        assertEquals("Calendar", item("Calendar").id)
    }
}

class NavSectionFilterTest {

    private fun item(label: String, pinned: Boolean = false) = AppNavItem(
        label = label,
        icon = Icons.Default.Home,
        selected = false,
        pinned = pinned,
        onClick = {}
    )

    private val sections = listOf(
        AppNavSection(label = "Pinned", pinned = true, items = listOf(item("Notes", pinned = true))),
        AppNavSection(label = "Health", items = listOf(item("Weight"), item("Steps"))),
        AppNavSection(label = "Organization", items = listOf(item("Notes"), item("Todos")))
    )

    @Test
    fun blankQuery_returnsSectionsUnchanged() {
        assertEquals(sections, filterNavSections(sections, ""))
        assertEquals(sections, filterNavSections(sections, "   "))
    }

    @Test
    fun query_keepsOnlyMatchingItemsCaseInsensitive() {
        val result = filterNavSections(sections, "WEI")
        assertEquals(listOf("Health"), result.map { it.label })
        assertEquals(listOf("Weight"), result.single().items.map { it.label })
    }

    @Test
    fun query_dropsPinnedSection() {
        val result = filterNavSections(sections, "notes")
        assertEquals(listOf("Organization"), result.map { it.label })
    }

    @Test
    fun query_isTrimmed() {
        assertEquals(listOf("Weight"), filterNavSections(sections, "  weight ").single().items.map { it.label })
    }

    @Test
    fun query_withoutMatch_returnsEmptyList() {
        assertTrue(filterNavSections(sections, "zzz").isEmpty())
    }

    @Test
    fun query_matchesSubstringAnywhereInLabel() {
        assertEquals(listOf("Todos"), filterNavSections(sections, "od").single().items.map { it.label })
    }
}

class NavHiddenItemsTest {

    private fun item(label: String, hidden: Boolean = false) = AppNavItem(
        label = label,
        icon = Icons.Default.Home,
        selected = false,
        hidden = hidden,
        onClick = {}
    )

    private val sections = listOf(
        AppNavSection(items = listOf(item("Calendar"))),
        AppNavSection(label = "Health", id = "HEALTH", items = listOf(item("Weight", hidden = true), item("Steps"))),
        AppNavSection(label = "Tools", id = "TOOLS", items = listOf(item("Alcohol", hidden = true)))
    )

    @Test
    fun showHidden_returnsSectionsUnchanged() {
        assertEquals(sections, withoutHiddenNavItems(sections, showHidden = true))
    }

    @Test
    fun notShowingHidden_dropsHiddenItems() {
        val result = withoutHiddenNavItems(sections, showHidden = false)
        assertEquals(listOf("Steps"), result.single { it.label == "Health" }.items.map { it.label })
    }

    @Test
    fun notShowingHidden_dropsSectionsWithOnlyHiddenItems() {
        val result = withoutHiddenNavItems(sections, showHidden = false)
        assertEquals(listOf(null, "Health"), result.map { it.label })
    }

    @Test
    fun noHiddenItems_changesNothing() {
        val plain = listOf(AppNavSection(label = "A", items = listOf(item("x"))))
        assertEquals(plain, withoutHiddenNavItems(plain, showHidden = false))
    }

    @Test
    fun hasHiddenNavItems_detectsAnyHiddenItem() {
        assertTrue(hasHiddenNavItems(sections))
        assertTrue(!hasHiddenNavItems(listOf(AppNavSection(label = "A", items = listOf(item("x"))))))
        assertTrue(!hasHiddenNavItems(emptyList()))
    }

    @Test
    fun sectionId_defaultsToLabelOrEmpty() {
        assertEquals("Health", AppNavSection(label = "Health", items = emptyList()).id)
        assertEquals("", AppNavSection(items = emptyList()).id)
    }
}
