package com.wafflehq.base.ui.library.demos

import android.app.Application
import com.wafflehq.base.R
import com.wafflehq.base.ui.library.LibraryDemoTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34], qualifiers = "w411dp-h1800dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PrefsBackupDemoTest : LibraryDemoTest() {

    @Test
    fun showsOneSnapshotLinePerPreference() {
        show { PrefsBackupDemo() }

        node(DemoTags.section("prefsbackup")).assertExists()
        assertTagCount(PrefsBackupTags.SNAPSHOT, 7)
    }

    @Test
    fun restoreConfirmsTheRoundTrip() {
        show { PrefsBackupDemo() }

        click(PrefsBackupTags.RESTORE)

        assertTagText(PrefsBackupTags.RESTORE_RESULT, string(R.string.libex_prefsbackup_restore_ok))
    }

    @Test
    fun editingAValueResetsTheResult() {
        show { PrefsBackupDemo() }
        click(PrefsBackupTags.RESTORE)

        replaceText(PrefsBackupTags.NAME, "Other")

        assertTagCount(PrefsBackupTags.RESTORE_RESULT, 0)
    }

    @Test
    fun wrongTypedEntriesAreRejected() {
        show { PrefsBackupDemo() }

        click(PrefsBackupTags.TAMPER)

        assertTagText(PrefsBackupTags.TAMPER_RESULT, string(R.string.libex_prefsbackup_tamper_rejected))
    }

    @Test
    fun counterStepperChangesTheEncodedValue() {
        show { PrefsBackupDemo() }
        assertTagText(PrefsBackupTags.SNAPSHOT, "i:3")

        stepUp(PrefsBackupTags.COUNTER)

        assertTagText(PrefsBackupTags.SNAPSHOT, "i:4")
    }

    @Test
    fun encodeTagsEveryValueWithItsType() {
        val snapshot = PrefsBackupDemoLogic.encode(PrefsBackupDemoLogic.preferences(darkMode = true, counter = 3, name = "Waffle"))

        assertEquals("b:true", snapshot["dark_mode"])
        assertEquals("i:3", snapshot["counter"])
        assertEquals("s:Waffle", snapshot["display_name"])
        assertEquals("l:1700000000000", snapshot["last_sync"])
        assertEquals("f:0.75", snapshot["ratio"])
        assertEquals("d:3.14159", snapshot["precise"])
        assertTrue(snapshot.getValue("tags").startsWith("ss:"))
        assertEquals(7, snapshot.size)
    }

    @Test
    fun restoreRebuildsTheOriginalPreferences() {
        val original = PrefsBackupDemoLogic.preferences(darkMode = false, counter = 42, name = "Ünïcode ✓")

        val restored = PrefsBackupDemoLogic.restore(PrefsBackupDemoLogic.encode(original))

        assertTrue(PrefsBackupDemoLogic.roundTripMatches(original, restored))
        assertEquals(42, restored[PrefsBackupDemoLogic.COUNTER])
        assertEquals(setOf("alpha", "beta"), restored[PrefsBackupDemoLogic.TAGS])
    }

    @Test
    fun typeMismatchKeepsTheExistingValue() {
        val original = PrefsBackupDemoLogic.preferences(darkMode = true, counter = 5, name = "Waffle")
        val snapshot = PrefsBackupDemoLogic.encode(original)

        val result = PrefsBackupDemoLogic.restoreOverExisting(original, PrefsBackupDemoLogic.tamperedSnapshot(snapshot))

        assertEquals(5, result[PrefsBackupDemoLogic.COUNTER])
        assertTrue(PrefsBackupDemoLogic.tamperingRejected(original, snapshot))
    }

    @Test
    fun differentPreferencesDoNotMatch() {
        val first = PrefsBackupDemoLogic.preferences(darkMode = true, counter = 1, name = "A")
        val second = PrefsBackupDemoLogic.preferences(darkMode = true, counter = 2, name = "A")

        assertFalse(PrefsBackupDemoLogic.roundTripMatches(first, second))
    }
}
