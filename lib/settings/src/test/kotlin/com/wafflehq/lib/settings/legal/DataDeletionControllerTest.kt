package com.wafflehq.lib.settings.legal

import com.wafflehq.lib.settings.legal.access.DataResetter
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration.Companion.seconds

class DataDeletionControllerTest {

    private val scopes = mutableListOf<CoroutineScope>()

    @After
    fun tearDown() {
        scopes.forEach { it.cancel() }
        scopes.clear()
    }

    private fun controller(resetter: DataResetter): DataDeletionController {
        val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
        scopes += scope
        return DataDeletionController(resetter, scope)
    }

    private suspend fun DataDeletionController.awaitState(expected: DataDeletionState) {
        withTimeout(5.seconds) { state.first { it == expected } }
    }

    @Test
    fun `initial state is idle`() {
        val controller = controller { }

        assertEquals(DataDeletionState.IDLE, controller.state.value)
    }

    @Test
    fun `deleteAllData reaches done on success`() = runBlocking {
        val calls = AtomicInteger()
        val controller = controller { calls.incrementAndGet() }

        controller.deleteAllData()

        controller.awaitState(DataDeletionState.DONE)
        assertEquals(1, calls.get())
    }

    @Test
    fun `deleteAllData reaches error when the resetter throws`() = runBlocking {
        val controller = controller { throw IllegalStateException("boom") }

        controller.deleteAllData()

        controller.awaitState(DataDeletionState.ERROR)
    }

    @Test
    fun `a second call while deleting is ignored`() = runBlocking {
        val gate = CompletableDeferred<Unit>()
        val calls = AtomicInteger()
        val controller = controller {
            calls.incrementAndGet()
            gate.await()
        }

        controller.deleteAllData()
        controller.awaitState(DataDeletionState.DELETING)
        controller.deleteAllData()
        gate.complete(Unit)

        controller.awaitState(DataDeletionState.DONE)
        assertEquals(1, calls.get())
    }

    @Test
    fun `retrying after an error reaches done`() = runBlocking {
        val calls = AtomicInteger()
        val controller = controller {
            if (calls.incrementAndGet() == 1) throw IllegalStateException("boom")
        }

        controller.deleteAllData()
        controller.awaitState(DataDeletionState.ERROR)
        controller.deleteAllData()

        controller.awaitState(DataDeletionState.DONE)
        assertEquals(2, calls.get())
    }
}
