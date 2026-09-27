package com.wafflehq.lib.uicore.render

class LayoutStabilityTracker(private val requiredStableFrames: Int) {

    init {
        require(requiredStableFrames > 0) { "requiredStableFrames must be positive" }
    }

    private var lastValue = UNSET
    private var stableFrames = 0

    val isStable: Boolean
        get() = stableFrames >= requiredStableFrames

    fun record(value: Int) {
        when {
            value <= 0 -> {
                lastValue = UNSET
                stableFrames = 0
            }
            value == lastValue -> stableFrames++
            else -> {
                lastValue = value
                stableFrames = 1
            }
        }
    }

    private companion object {
        const val UNSET = -1
    }
}
