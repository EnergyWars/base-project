package com.wafflehq.lib.modules.permissions

import android.Manifest
import android.app.Application
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
class PermissionCatalogTest {

    private val context = ApplicationProvider.getApplicationContext<Application>()

    private fun grant(vararg permissions: String) {
        shadowOf(context).grantPermissions(*permissions)
    }

    private fun runtimeDefinition(
        id: String = "camera",
        permissions: List<String> = listOf(Manifest.permission.CAMERA),
        requireAll: Boolean = true,
        minSdk: Int = 0
    ) = PermissionDefinition(
        id = id,
        titleRes = android.R.string.copy,
        requirement = PermissionRequirement.Runtime(permissions, requireAll),
        minSdk = minSdk
    )

    private fun usage(
        permissionId: String = "camera",
        optional: Boolean = false,
        active: Boolean = true
    ) = PermissionUsage(
        permissionId = permissionId,
        ownerLabelRes = android.R.string.cancel,
        purposeRes = android.R.string.ok,
        optional = optional,
        isActive = MutableStateFlow(active)
    )

    private fun catalog(
        definitions: Set<PermissionDefinition>,
        usages: Set<PermissionUsage> = emptySet(),
        sdkInt: Int = 34
    ) = PermissionCatalog(definitions, usages, sdkInt)

    @Test
    fun `a granted runtime permission is reported as granted`() {
        grant(Manifest.permission.CAMERA)
        val catalog = catalog(setOf(runtimeDefinition()))

        val entry = catalog.entries(context, emptyMap()).single()

        assertTrue(entry.granted)
    }

    @Test
    fun `a missing runtime permission is reported as denied`() {
        val entry = catalog(setOf(runtimeDefinition())).entries(context, emptyMap()).single()

        assertFalse(entry.granted)
    }

    @Test
    fun `requireAll needs every permission`() {
        grant(Manifest.permission.CAMERA)
        val definition = runtimeDefinition(
            permissions = listOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
        )

        assertFalse(catalog(setOf(definition)).entries(context, emptyMap()).single().granted)
    }

    @Test
    fun `without requireAll a single permission is enough`() {
        grant(Manifest.permission.CAMERA)
        val definition = runtimeDefinition(
            permissions = listOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO),
            requireAll = false
        )

        assertTrue(catalog(setOf(definition)).entries(context, emptyMap()).single().granted)
    }

    @Test
    fun `a definition without permissions counts as granted`() {
        val definition = runtimeDefinition(permissions = emptyList())

        assertTrue(catalog(setOf(definition)).entries(context, emptyMap()).single().granted)
    }

    @Test
    fun `a special requirement decides for itself and offers its settings intent`() {
        val intent = Intent("open.settings")
        val definition = PermissionDefinition(
            id = "battery",
            titleRes = android.R.string.copy,
            requirement = PermissionRequirement.Special(
                permissions = listOf("android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS"),
                isGranted = { true },
                settingsIntent = { intent }
            )
        )
        val catalog = catalog(setOf(definition))

        assertTrue(catalog.entries(context, emptyMap()).single().granted)
        assertEquals(intent, catalog.settingsIntent(definition, context))
    }

    @Test
    fun `a runtime definition has no settings intent`() {
        val definition = runtimeDefinition()

        assertNull(catalog(setOf(definition)).settingsIntent(definition, context))
    }

    @Test
    fun `definitions above the running sdk are left out`() {
        val catalog = catalog(
            setOf(runtimeDefinition(id = "old"), runtimeDefinition(id = "future", minSdk = 99)),
            sdkInt = 34
        )

        assertEquals(listOf("old"), catalog.available.map { it.id })
    }

    @Test
    fun `definitions below the running sdk are left out`() {
        val legacy = runtimeDefinition(id = "storage").copy(maxSdk = 28)
        val catalog = catalog(setOf(runtimeDefinition(id = "camera"), legacy), sdkInt = 34)

        assertEquals(listOf("camera"), catalog.available.map { it.id })
    }

    @Test
    fun `a legacy definition is available on an old sdk`() {
        val legacy = runtimeDefinition(id = "storage").copy(maxSdk = 28)

        assertTrue(catalog(setOf(legacy), sdkInt = 28).available.map { it.id }.contains("storage"))
    }

    @Test
    fun `a definition the device does not support is left out`() {
        val autostart = runtimeDefinition(id = "autostart").copy(deviceSupported = false)

        assertTrue(catalog(setOf(autostart), sdkInt = 34).available.isEmpty())
    }

    @Test
    fun `a special requirement falls back to its declared permissions for the manifest check`() {
        val definition = PermissionDefinition(
            id = "notifications",
            titleRes = android.R.string.copy,
            requirement = PermissionRequirement.Special(
                permissions = listOf(Manifest.permission.POST_NOTIFICATIONS),
                isGranted = { false }
            )
        )

        assertEquals(listOf(Manifest.permission.POST_NOTIFICATIONS), definition.manifestPermissions)
    }

    @Test
    fun `usages are grouped per permission`() = runBlocking {
        val catalog = catalog(
            setOf(runtimeDefinition(), runtimeDefinition(id = "audio")),
            setOf(usage(), usage(), usage(permissionId = "audio"))
        )

        val states = catalog.usageStates.first()

        assertEquals(2, states.getValue("camera").size)
        assertEquals(1, states.getValue("audio").size)
    }

    @Test
    fun `an inactive usage is reported as inactive instead of being dropped`() = runBlocking {
        val active = MutableStateFlow(false)
        val catalog = catalog(
            setOf(runtimeDefinition()),
            setOf(PermissionUsage("camera", android.R.string.cancel, android.R.string.ok, isActive = active))
        )

        val entry = catalog.entries(context, catalog.usageStates.first()).single()

        assertEquals(1, entry.usages.size)
        assertFalse(entry.usages.single().active)
        assertFalse(entry.isUsed)
    }

    @Test
    fun `switching a module on marks its usage active`() = runBlocking {
        val active = MutableStateFlow(false)
        val catalog = catalog(
            setOf(runtimeDefinition()),
            setOf(PermissionUsage("camera", android.R.string.cancel, android.R.string.ok, isActive = active))
        )
        assertFalse(catalog.usageStates.first().getValue("camera").single().active)

        active.value = true

        assertTrue(catalog.usageStates.first().getValue("camera").single().active)
    }

    @Test
    fun `an optional usage does not make a permission required`() = runBlocking {
        val catalog = catalog(
            setOf(runtimeDefinition()),
            setOf(usage(optional = true))
        )

        val entry = catalog.entries(context, catalog.usageStates.first()).single()

        assertTrue(entry.isUsed)
        assertFalse(entry.isRequired)
    }

    @Test
    fun `a mandatory usage makes the permission required`() = runBlocking {
        val catalog = catalog(setOf(runtimeDefinition()), setOf(usage(optional = true), usage()))

        assertTrue(catalog.entries(context, catalog.usageStates.first()).single().isRequired)
    }

    @Test
    fun `a permission nobody registered shows up without users`() {
        val entry = catalog(setOf(runtimeDefinition())).entries(context, emptyMap()).single()

        assertTrue(entry.usages.isEmpty())
        assertFalse(entry.isUsed)
    }

    @Test
    fun `without any usage the state flow stays empty`() = runBlocking {
        assertEquals(emptyMap<String, List<PermissionUsageState>>(), catalog(setOf(runtimeDefinition())).usageStates.first())
    }
}
