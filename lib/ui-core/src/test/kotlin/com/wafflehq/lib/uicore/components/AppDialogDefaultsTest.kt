package com.wafflehq.lib.uicore.components

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppDialogDefaultsTest {

    @Test
    fun properties_doNotDismissOnOutsideTapButKeepBackPress() {
        val properties = AppDialogDefaults.properties

        assertFalse(properties.dismissOnClickOutside)
        assertTrue(properties.dismissOnBackPress)
        assertTrue(properties.usePlatformDefaultWidth)
    }

    @Test
    fun fullWidthProperties_doNotDismissOnOutsideTapAndUseFullWidth() {
        val properties = AppDialogDefaults.fullWidthProperties

        assertFalse(properties.dismissOnClickOutside)
        assertTrue(properties.dismissOnBackPress)
        assertFalse(properties.usePlatformDefaultWidth)
    }
}
