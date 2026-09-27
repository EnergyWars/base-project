package com.wafflehq.lib.settings.onboarding

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class OnboardingStateStoreTest {

    private lateinit var store: OnboardingStateStore

    @Before
    fun setUp() {
        store = OnboardingStateStore(FakePreferencesDataStore())
    }

    @Test
    fun `onboardingCompleted defaults to false`() = runBlocking {
        assertFalse(store.onboardingCompleted.first())
    }

    @Test
    fun `setOnboardingCompleted persists the value`() = runBlocking {
        store.setOnboardingCompleted(true)
        assertTrue(store.onboardingCompleted.first())
    }

    @Test
    fun `startupPermissionCheckDone round-trips`() = runBlocking {
        assertFalse(store.startupPermissionCheckDone.first())
        store.setStartupPermissionCheckDone(true)
        assertTrue(store.startupPermissionCheckDone.first())
    }

    @Test
    fun `the xiaomi autostart flags are tracked separately`() = runBlocking {
        store.setXiaomiAutostartHintShown(true)
        assertTrue(store.xiaomiAutostartHintShown.first())
        assertFalse(store.xiaomiAutostartOpened.first())
        store.setXiaomiAutostartOpened(true)
        assertTrue(store.xiaomiAutostartOpened.first())
    }

    @Test
    fun `consumeModuleOnboardingTrigger fires once per module`() = runBlocking {
        assertTrue(store.consumeModuleOnboardingTrigger("notes"))
        assertFalse(store.consumeModuleOnboardingTrigger("notes"))
    }

    @Test
    fun `consumeModuleOnboardingTrigger tracks modules independently`() = runBlocking {
        assertTrue(store.consumeModuleOnboardingTrigger("notes"))
        assertTrue(store.consumeModuleOnboardingTrigger("tagebuch"))
        assertFalse(store.consumeModuleOnboardingTrigger("notes"))
        assertFalse(store.consumeModuleOnboardingTrigger("tagebuch"))
    }

    @Test
    fun `existing user gets all active modules marked as seen`() = runBlocking {
        store.setOnboardingCompleted(true)

        store.markActiveModulesSeenForExistingUser(listOf("notes", "tagebuch"))

        assertFalse(store.consumeModuleOnboardingTrigger("notes"))
        assertFalse(store.consumeModuleOnboardingTrigger("tagebuch"))
        assertTrue(store.consumeModuleOnboardingTrigger("todo"))
    }

    @Test
    fun `fresh install keeps every module unseen`() = runBlocking {
        store.markActiveModulesSeenForExistingUser(listOf("notes"))

        assertTrue(store.consumeModuleOnboardingTrigger("notes"))
    }

    @Test
    fun `migration runs only once`() = runBlocking {
        store.markActiveModulesSeenForExistingUser(listOf("notes"))
        store.setOnboardingCompleted(true)

        store.markActiveModulesSeenForExistingUser(listOf("tagebuch"))

        assertTrue(store.consumeModuleOnboardingTrigger("tagebuch"))
    }

    @Test
    fun `migration keeps previously seen modules`() = runBlocking {
        store.setOnboardingCompleted(true)
        store.consumeModuleOnboardingTrigger("todo")

        store.markActiveModulesSeenForExistingUser(listOf("notes"))

        assertFalse(store.consumeModuleOnboardingTrigger("todo"))
        assertFalse(store.consumeModuleOnboardingTrigger("notes"))
    }

    @Test
    fun `clearAll forgets everything`() = runBlocking {
        store.setOnboardingCompleted(true)
        store.consumeModuleOnboardingTrigger("notes")

        store.clearAll()

        assertFalse(store.onboardingCompleted.first())
        assertTrue(store.consumeModuleOnboardingTrigger("notes"))
    }
}
