package com.wafflehq.base.ui.navigation

import android.app.Application
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
class RoutesTest {

    @Test
    fun `feature file detail route encodes the file name`() {
        assertEquals("feature_file_detail/a%20b.md", Routes.featureFileDetail("a b.md"))
        assertEquals("feature_file_detail/plain.md", Routes.featureFileDetail("plain.md"))
    }

    @Test
    fun `color category route encodes the category key`() {
        assertEquals("settings_color_category/GLOBAL", Routes.colorCategory("GLOBAL"))
        assertEquals("settings_color_category/A%2FB", Routes.colorCategory("A/B"))
    }

    @Test
    fun `route patterns contain their argument placeholders`() {
        assertTrue(Routes.FEATURE_FILE_DETAIL.endsWith("{fileName}"))
        assertTrue(Routes.SETTINGS_COLOR_CATEGORY.endsWith("{categoryKey}"))
    }

    @Test
    fun `all routes are distinct`() {
        val routes = listOf(
            Routes.HOME, Routes.SETTINGS, Routes.SETTINGS_DISPLAY, Routes.SETTINGS_COLORS,
            Routes.SETTINGS_COLOR_CATEGORY, Routes.EXAMPLE_1, Routes.EXAMPLE_2, Routes.EXAMPLE_3,
            Routes.LIBRARY_EXAMPLES, Routes.FEATURE_FILES, Routes.FEATURE_FILE_DETAIL
        )
        assertEquals(routes.size, routes.toSet().size)
    }
}
