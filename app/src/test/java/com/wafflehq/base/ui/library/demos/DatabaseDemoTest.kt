package com.wafflehq.base.ui.library.demos

import android.app.Application
import com.wafflehq.base.R
import com.wafflehq.base.ui.library.LibraryDemoTest
import com.wafflehq.lib.database.crypto.PasswordStrength
import com.wafflehq.lib.database.crypto.WrappedDek
import com.wafflehq.lib.database.state.ConversionState
import kotlinx.coroutines.Dispatchers
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
class DatabaseDemoTest : LibraryDemoTest() {

    private fun showDemo() = show { DatabaseDemo(workDispatcher = Dispatchers.Unconfined) }

    @Test
    fun rendersThePasswordStrengthOfTheDefaultPassword() {
        showDemo()

        node(DemoTags.section("database")).assertExists()
        assertTagText(DatabaseTags.STRENGTH, string(R.string.libex_database_strength_strong))
    }

    @Test
    fun weakPasswordsAreFlagged() {
        showDemo()

        replaceText(DatabaseTags.PASSWORD, "abc")

        assertTagText(DatabaseTags.STRENGTH, string(R.string.libex_database_strength_weak))
    }

    @Test
    fun wrapAndUnwrapConfirmsTheRoundTrip() {
        showDemo()

        click(DatabaseTags.WRAP)
        waitForTag(DatabaseTags.WRAP_RESULT)

        val yes = string(R.string.libex_value_yes)
        assertTagText(DatabaseTags.WRAP_RESULT, string(R.string.libex_database_wrap_result, yes, yes, yes, 600_000))
    }

    @Test
    fun openPlanForAFreshInstallUsesThePlainDatabaseFile() {
        showDemo()

        click(DatabaseTags.PLAN)
        waitForTag(DatabaseTags.PLAN_RESULT)

        assertTagText(DatabaseTags.PLAN_FILE, DatabaseDemoLogic.DATABASE_FILE)
        val no = string(R.string.libex_value_no)
        assertTagText(
            DatabaseTags.PLAN_RESULT,
            string(R.string.libex_database_plan_result, no, ConversionState.NONE.name, no, no),
        )
    }

    @Test
    fun planOpenDescribesAnUnencryptedDatabase() {
        val plan = DatabaseDemoLogic.planOpen(context)

        assertEquals(DatabaseDemoLogic.DATABASE_FILE, plan.fileName)
        assertFalse(plan.encrypted)
        assertFalse(plan.hasOpenHelperFactory)
        assertFalse(plan.keyUnavailable)
        assertEquals(ConversionState.NONE, plan.conversionState)
        assertTrue(plan.path.endsWith(DatabaseDemoLogic.DATABASE_FILE))
    }

    @Test
    fun wrapRoundTripRejectsTheWrongPassword() {
        val result = DatabaseDemoLogic.wrapRoundTrip("correct horse battery staple")

        assertTrue(result.roundTripOk)
        assertTrue(result.wrongPasswordRejected)
        assertTrue(result.aesRoundTripOk)
        assertEquals(600_000, result.iterations)
    }

    @Test
    fun strengthMapsToToneAndLabel() {
        assertEquals(DemoTone.Error, DatabaseDemoLogic.strengthTone(PasswordStrength.WEAK))
        assertEquals(DemoTone.Warning, DatabaseDemoLogic.strengthTone(PasswordStrength.MEDIUM))
        assertEquals(DemoTone.Success, DatabaseDemoLogic.strengthTone(PasswordStrength.STRONG))
        assertEquals(3, PasswordStrength.entries.map { DatabaseDemoLogic.strengthLabel(it) }.toSet().size)
    }

    @Test
    fun inMemoryKeyWrapperRestoresTheKey() {
        val wrapper = InMemoryKeyWrapper()
        val dek = ByteArray(32) { it.toByte() }

        val wrapped: WrappedDek = wrapper.wrap(dek)

        assertTrue(wrapper.unwrap(wrapped).contentEquals(dek))
        assertFalse(wrapped.ciphertext.contentEquals(dek))
    }
}
