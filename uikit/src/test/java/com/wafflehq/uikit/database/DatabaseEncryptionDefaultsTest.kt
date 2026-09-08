package com.wafflehq.uikit.database

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class DatabaseEncryptionDefaultsTest {

    @Test
    fun `key alias is stable`() {
        assertEquals("encryption_module_kek", DatabaseEncryptionDefaults.KEK_ALIAS)
    }

    @Test
    fun `state prefs name is stable`() {
        assertEquals("encryption_state", DatabaseEncryptionDefaults.STATE_PREFS_NAME)
    }

    @Test
    fun `alias and prefs name do not collide`() {
        assertNotEquals(DatabaseEncryptionDefaults.KEK_ALIAS, DatabaseEncryptionDefaults.STATE_PREFS_NAME)
    }
}
