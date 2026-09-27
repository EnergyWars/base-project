package com.wafflehq.lib.uicore.render

import android.app.Presentation
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PixelFormat
import android.graphics.drawable.ColorDrawable
import android.hardware.display.DisplayManager
import android.media.ImageReader
import android.os.Handler
import android.os.Looper
import android.view.Choreographer
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

class OffscreenComposeRenderer(private val context: Context) {

    suspend fun render(spec: OffscreenRenderSpec, content: @Composable () -> Unit): Bitmap =
        withContext(Dispatchers.Main) { renderOnMainThread(spec, content) }

    private suspend fun renderOnMainThread(spec: OffscreenRenderSpec, content: @Composable () -> Unit): Bitmap {
        val imageReader = ImageReader.newInstance(spec.widthPx, spec.maxHeightPx, PixelFormat.RGBA_8888, IMAGE_READER_BUFFERS)
        imageReader.setOnImageAvailableListener(
            { reader -> reader.acquireLatestImage()?.close() },
            Handler(Looper.getMainLooper())
        )
        val displayManager = context.getSystemService(DisplayManager::class.java)
        val virtualDisplay = displayManager.createVirtualDisplay(
            VIRTUAL_DISPLAY_NAME,
            spec.widthPx,
            spec.maxHeightPx,
            spec.densityDpi,
            imageReader.surface,
            0
        )
        val owner = OffscreenViewTreeOwner()
        val presentation = Presentation(context, virtualDisplay.display)
        try {
            var contentHeightPx = 0
            val composeView = ComposeView(presentation.context).apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
                setContent {
                    Box(modifier = Modifier.fillMaxWidth().onSizeChanged { contentHeightPx = it.height }) {
                        content()
                    }
                }
            }
            owner.attachTo(composeView)
            presentation.window?.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))
            presentation.setContentView(
                composeView,
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            )
            presentation.show()
            owner.resume()
            val tracker = LayoutStabilityTracker(STABLE_FRAME_COUNT)
            var frames = 0
            while (frames < MAX_WAIT_FRAMES && !tracker.isStable) {
                awaitFrame()
                frames++
                if (composeView.isLaidOut && !composeView.isLayoutRequested) {
                    tracker.record(contentHeightPx)
                }
            }
            val bitmap = Bitmap.createBitmap(
                composeView.width.coerceAtLeast(1),
                contentHeightPx.coerceIn(1, spec.maxHeightPx),
                Bitmap.Config.ARGB_8888
            )
            composeView.draw(Canvas(bitmap))
            return bitmap
        } finally {
            presentation.dismiss()
            owner.destroy()
            virtualDisplay.release()
            imageReader.close()
        }
    }

    private suspend fun awaitFrame() {
        val choreographer = Choreographer.getInstance()
        suspendCancellableCoroutine { continuation ->
            val callback = Choreographer.FrameCallback {
                if (continuation.isActive) continuation.resume(Unit)
            }
            choreographer.postFrameCallback(callback)
            continuation.invokeOnCancellation { choreographer.removeFrameCallback(callback) }
        }
    }

    private class OffscreenViewTreeOwner : LifecycleOwner, SavedStateRegistryOwner {

        private val lifecycleRegistry = LifecycleRegistry(this)
        private val savedStateController = SavedStateRegistryController.create(this)

        override val lifecycle: Lifecycle
            get() = lifecycleRegistry

        override val savedStateRegistry: SavedStateRegistry
            get() = savedStateController.savedStateRegistry

        init {
            savedStateController.performAttach()
            savedStateController.performRestore(null)
            lifecycleRegistry.currentState = Lifecycle.State.CREATED
        }

        fun attachTo(view: ComposeView) {
            view.setViewTreeLifecycleOwner(this)
            view.setViewTreeSavedStateRegistryOwner(this)
        }

        fun resume() {
            lifecycleRegistry.currentState = Lifecycle.State.RESUMED
        }

        fun destroy() {
            lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        }
    }

    private companion object {
        const val VIRTUAL_DISPLAY_NAME = "offscreen-compose-renderer"
        const val IMAGE_READER_BUFFERS = 2
        const val STABLE_FRAME_COUNT = 3
        const val MAX_WAIT_FRAMES = 90
    }
}
