package com.wafflehq.lib.modules.permissions

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PermissionRegistryCheckTest {

    private fun definition(id: String, vararg permissions: String) = PermissionDefinition(
        id = id,
        titleRes = 1,
        requirement = PermissionRequirement.Runtime(permissions.toList())
    )

    private fun usage(permissionId: String) = PermissionUsage(
        permissionId = permissionId,
        ownerLabelRes = 2,
        purposeRes = 3
    )

    @Test
    fun `a fully registered permission is consistent`() {
        val result = PermissionRegistryCheck.verify(
            manifestPermissions = setOf("CAMERA"),
            definitions = setOf(definition("camera", "CAMERA")),
            usages = setOf(usage("camera"))
        )

        assertTrue(result.isConsistent)
        assertEquals("", result.describe())
    }

    @Test
    fun `a manifest permission without definition is reported`() {
        val result = PermissionRegistryCheck.verify(
            manifestPermissions = setOf("CAMERA", "RECORD_AUDIO"),
            definitions = setOf(definition("camera", "CAMERA")),
            usages = setOf(usage("camera"))
        )

        assertEquals(setOf("RECORD_AUDIO"), result.notInCatalog)
        assertFalse(result.isConsistent)
        assertTrue(result.describe().contains("RECORD_AUDIO"))
    }

    @Test
    fun `an implicit permission needs no definition`() {
        val result = PermissionRegistryCheck.verify(
            manifestPermissions = setOf("CAMERA", "INTERNET"),
            definitions = setOf(definition("camera", "CAMERA")),
            usages = setOf(usage("camera")),
            implicitPermissions = setOf("INTERNET")
        )

        assertTrue(result.isConsistent)
    }

    @Test
    fun `a definition naming a permission the manifest lacks is reported`() {
        val result = PermissionRegistryCheck.verify(
            manifestPermissions = setOf("CAMERA"),
            definitions = setOf(definition("camera", "CAMERA", "BODY_SENSORS")),
            usages = setOf(usage("camera"))
        )

        assertEquals(setOf("BODY_SENSORS"), result.notInManifest)
        assertFalse(result.isConsistent)
    }

    @Test
    fun `a definition nobody uses is reported`() {
        val result = PermissionRegistryCheck.verify(
            manifestPermissions = setOf("CAMERA"),
            definitions = setOf(definition("camera", "CAMERA")),
            usages = emptySet()
        )

        assertEquals(setOf("camera"), result.definitionsWithoutUsage)
        assertTrue(result.describe().contains("camera"))
    }

    @Test
    fun `a usage pointing at an unknown id is reported`() {
        val result = PermissionRegistryCheck.verify(
            manifestPermissions = setOf("CAMERA"),
            definitions = setOf(definition("camera", "CAMERA")),
            usages = setOf(usage("camera"), usage("microphone"))
        )

        assertEquals(setOf("microphone"), result.usagesWithoutDefinition)
    }

    @Test
    fun `two definitions with the same id are reported`() {
        val result = PermissionRegistryCheck.verify(
            manifestPermissions = setOf("CAMERA", "RECORD_AUDIO"),
            definitions = setOf(definition("camera", "CAMERA"), definition("camera", "RECORD_AUDIO")),
            usages = setOf(usage("camera"))
        )

        assertEquals(setOf("camera"), result.duplicateIds)
        assertFalse(result.isConsistent)
    }

    @Test
    fun `an empty registry against an empty manifest is consistent`() {
        assertTrue(PermissionRegistryCheck.verify(emptySet(), emptySet(), emptySet()).isConsistent)
    }

    @Test
    fun `several problems are reported together`() {
        val result = PermissionRegistryCheck.verify(
            manifestPermissions = setOf("CAMERA", "RECORD_AUDIO"),
            definitions = setOf(definition("camera", "CAMERA"), definition("contacts", "READ_CONTACTS")),
            usages = setOf(usage("microphone"))
        )

        assertEquals(setOf("RECORD_AUDIO"), result.notInCatalog)
        assertEquals(setOf("READ_CONTACTS"), result.notInManifest)
        assertEquals(setOf("camera", "contacts"), result.definitionsWithoutUsage)
        assertEquals(setOf("microphone"), result.usagesWithoutDefinition)
    }
}
