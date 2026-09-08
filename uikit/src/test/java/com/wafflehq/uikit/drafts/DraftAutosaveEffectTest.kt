package com.wafflehq.uikit.drafts

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DraftAutosaveEffectTest {

    @Test
    fun runDraftAutosaveLoop_ticksRepeatedlyAtInterval() = runTest {
        var ticks = 0
        val job = launch { runDraftAutosaveLoop(intervalMs = 10L) { ticks++ } }

        advanceTimeBy(55L)
        assertEquals(5, ticks)

        job.cancel()
    }

    @Test
    fun runDraftAutosaveLoop_neverTicksBeforeFirstInterval() = runTest {
        var ticks = 0
        val job = launch { runDraftAutosaveLoop(intervalMs = 10L) { ticks++ } }

        advanceTimeBy(9L)
        assertEquals(0, ticks)

        job.cancel()
    }

    @Test
    fun runDraftAutosaveLoop_onTickReadsLatestValueAtEachTick() = runTest {
        var value = 0
        val observed = mutableListOf<Int>()
        val job = launch { runDraftAutosaveLoop(intervalMs = 10L) { observed.add(value) } }

        advanceTimeBy(10L)
        runCurrent()
        value = 42
        advanceTimeBy(10L)
        runCurrent()

        assertEquals(listOf(0, 42), observed)
        job.cancel()
    }

    @Test
    fun runDraftAutosaveLoop_stopsWhenCancelled() = runTest {
        var ticks = 0
        val job = launch { runDraftAutosaveLoop(intervalMs = 10L) { ticks++ } }

        advanceTimeBy(10L)
        val afterFirstTick = ticks
        job.cancel()
        advanceTimeBy(200L)

        assertEquals(afterFirstTick, ticks)
    }
}
