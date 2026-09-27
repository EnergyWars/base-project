package com.wafflehq.lib.uicore.testing

import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ApplicationInfo
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val ROOT_TAG = "resource_id_root"
private const val PROPERTY_NAME = "TestTagsAsResourceId"

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TestTagResourceIdsTest {

    @get:Rule
    val rule = createComposeRule()

    private fun contextWithFlags(flags: Int): Context {
        val base = ApplicationProvider.getApplicationContext<Context>()
        return object : ContextWrapper(base) {
            override fun getApplicationInfo(): ApplicationInfo =
                ApplicationInfo(base.applicationInfo).apply { this.flags = flags }
        }
    }

    private fun exposedValue(): Any? {
        val config = rule.onNodeWithTag(ROOT_TAG).fetchSemanticsNode().config
        return config.firstOrNull { it.key.name == PROPERTY_NAME }?.value
    }

    @Test
    fun enabledModifierExposesTestTagsAsResourceIds() {
        rule.setContent {
            Box(Modifier.testTag(ROOT_TAG).testTagsAsResourceIds(true))
        }

        assertEquals(true, exposedValue())
    }

    @Test
    fun disabledModifierMarksTestTagsAsNotExposed() {
        rule.setContent {
            Box(Modifier.testTag(ROOT_TAG).testTagsAsResourceIds(false))
        }

        assertEquals(false, exposedValue())
    }

    @Test
    fun debugBuildExposesTestTags() {
        rule.setContent {
            CompositionLocalProvider(LocalContext provides contextWithFlags(ApplicationInfo.FLAG_DEBUGGABLE)) {
                Box(Modifier.testTag(ROOT_TAG).exposeTestTagsInDebugBuilds())
            }
        }

        assertEquals(true, exposedValue())
    }

    @Test
    fun releaseBuildDoesNotExposeTestTags() {
        rule.setContent {
            CompositionLocalProvider(LocalContext provides contextWithFlags(0)) {
                Box(Modifier.testTag(ROOT_TAG).exposeTestTagsInDebugBuilds())
            }
        }

        assertEquals(false, exposedValue())
    }

    @Test
    fun isDebuggableAppReflectsDebuggableFlag() {
        assertTrue(contextWithFlags(ApplicationInfo.FLAG_DEBUGGABLE).isDebuggableApp())
    }

    @Test
    fun isDebuggableAppIgnoresUnrelatedFlags() {
        assertFalse(contextWithFlags(ApplicationInfo.FLAG_HAS_CODE).isDebuggableApp())
    }

    @Test
    fun isDebuggableAppDetectsFlagAmongOthers() {
        val flags = ApplicationInfo.FLAG_HAS_CODE or ApplicationInfo.FLAG_DEBUGGABLE
        assertTrue(contextWithFlags(flags).isDebuggableApp())
    }
}
