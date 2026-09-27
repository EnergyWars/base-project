package com.wafflehq.lib.modules.permissions

import android.Manifest
import android.app.Application
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
class ModulePermissionSpecTest {

    private val context get() = ApplicationProvider.getApplicationContext<Application>()

    private val anySpec = ModulePermissionSpec(
        permissions = listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
        rationaleTitleRes = 1,
        rationaleTextRes = 2,
        hintRes = 3
    )
    private val allSpec = anySpec.copy(requireAll = true)

    @Test
    fun emptyPermissionsAreAlwaysGranted() {
        val spec = anySpec.copy(permissions = emptyList())
        assertTrue(spec.isGranted(context))
        assertTrue(spec.evaluate(emptyMap()))
    }

    @Test
    fun anyModeIsGrantedWhenOnePermissionIsGranted() {
        assertFalse(anySpec.isGranted(context))
        shadowOf(context).grantPermissions(Manifest.permission.ACCESS_COARSE_LOCATION)
        assertTrue(anySpec.isGranted(context))
    }

    @Test
    fun allModeRequiresEveryPermission() {
        shadowOf(context).grantPermissions(Manifest.permission.ACCESS_COARSE_LOCATION)
        assertFalse(allSpec.isGranted(context))
        shadowOf(context).grantPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
        assertTrue(allSpec.isGranted(context))
    }

    @Test
    fun evaluateMirrorsRequireAllSemantics() {
        val partial = mapOf(Manifest.permission.ACCESS_COARSE_LOCATION to true, Manifest.permission.ACCESS_FINE_LOCATION to false)
        assertTrue(anySpec.evaluate(partial))
        assertFalse(allSpec.evaluate(partial))
        assertFalse(anySpec.evaluate(emptyMap()))
        val full = partial + (Manifest.permission.ACCESS_FINE_LOCATION to true)
        assertTrue(allSpec.evaluate(full))
    }
}
