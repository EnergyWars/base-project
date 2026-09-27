package com.wafflehq.lib.uicore.button

import android.view.MotionEvent
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TapjackingProtectionTest {

    @Test
    fun `unobscured touch is not flagged`() {
        assertFalse(isObscuredTouch(0))
    }

    @Test
    fun `fully obscured touch is flagged`() {
        assertTrue(isObscuredTouch(MotionEvent.FLAG_WINDOW_IS_OBSCURED))
    }

    @Test
    fun `partially obscured touch is flagged`() {
        assertTrue(isObscuredTouch(MotionEvent.FLAG_WINDOW_IS_PARTIALLY_OBSCURED))
    }

    @Test
    fun `both obscured flags together are still flagged`() {
        assertTrue(isObscuredTouch(MotionEvent.FLAG_WINDOW_IS_OBSCURED or MotionEvent.FLAG_WINDOW_IS_PARTIALLY_OBSCURED))
    }

    @Test
    fun `unrelated flag bit alone is not flagged`() {
        assertFalse(isObscuredTouch(MotionEvent.FLAG_CANCELED))
    }

    @Test
    fun `unrelated flag combined with an obscured flag is still flagged`() {
        assertTrue(isObscuredTouch(MotionEvent.FLAG_CANCELED or MotionEvent.FLAG_WINDOW_IS_OBSCURED))
    }
}
