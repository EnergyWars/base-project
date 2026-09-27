package com.wafflehq.lib.modules.permissions

import android.app.Application
import android.content.ComponentName
import android.provider.Settings
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowBuild

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
class XiaomiAutostartTest {

    private val context get() = ApplicationProvider.getApplicationContext<Application>()

    @Test
    fun xiaomiManufacturersAreDetected() {
        listOf("Xiaomi", "XIAOMI", "Redmi", "POCO", "poco").forEach { manufacturer ->
            ShadowBuild.setManufacturer(manufacturer)

            assertTrue(manufacturer, isXiaomiDevice())
        }
    }

    @Test
    fun otherManufacturersAreNotDetected() {
        listOf("Samsung", "Google", "OnePlus", "").forEach { manufacturer ->
            ShadowBuild.setManufacturer(manufacturer)

            assertFalse(manufacturer, isXiaomiDevice())
        }
    }

    @Test
    fun intentTargetsSecurityCenterWhenAutostartActivityIsAvailable() {
        val component = ComponentName(
            "com.miui.securitycenter",
            "com.miui.permcenter.autostart.AutoStartManagementActivity"
        )
        shadowOf(context.packageManager).addActivityIfNotPresent(component)

        val intent = xiaomiAutostartIntent(context)

        assertEquals(component, intent.component)
    }

    @Test
    fun intentFallsBackToAppDetailsWhenAutostartActivityIsMissing() {
        val intent = xiaomiAutostartIntent(context)

        assertEquals(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, intent.action)
        assertEquals("package:${context.packageName}", intent.data.toString())
    }
}
