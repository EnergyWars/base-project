package com.wafflehq.lib.pdf

import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.pdf.PdfRenderer
import org.apache.pdfbox.rendering.ImageType
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements
import org.robolectric.annotation.RealObject
import org.robolectric.shadow.api.Shadow
import kotlin.math.roundToInt

@Implements(PdfRenderer.Page::class)
class ShadowPdfRendererPage {

    @RealObject
    private lateinit var realPage: PdfRenderer.Page

    private lateinit var owner: PdfRenderer
    private var index: Int = 0
    private var pageWidth: Int = 0
    private var pageHeight: Int = 0
    private var closed = false

    @Implementation
    fun __constructor__(renderer: PdfRenderer, index: Int) {
        owner = renderer
        this.index = index
        val shadowRenderer = Shadow.extract<ShadowPdfRenderer>(renderer)
        val mediaBox = shadowRenderer.document.getPage(index).mediaBox
        pageWidth = mediaBox.width.roundToInt()
        pageHeight = mediaBox.height.roundToInt()
    }

    @Implementation
    fun getIndex(): Int = index

    @Implementation
    fun getWidth(): Int {
        throwIfClosed()
        return pageWidth
    }

    @Implementation
    fun getHeight(): Int {
        throwIfClosed()
        return pageHeight
    }

    @Implementation
    fun render(bitmap: Bitmap, destClip: Rect?, transform: Matrix?, renderMode: Int) {
        throwIfClosed()
        val shadowRenderer = Shadow.extract<ShadowPdfRenderer>(owner)
        val pdPage = shadowRenderer.document.getPage(index)
        val dpi = 72f * bitmap.width / pdPage.mediaBox.width
        val rendered = renderImageWithDpiMethod.invoke(shadowRenderer.renderer, index, dpi, ImageType.ARGB)
        val renderedWidth = bufferedImageWidthMethod.invoke(rendered) as Int
        val renderedHeight = bufferedImageHeightMethod.invoke(rendered) as Int
        val renderedPixels = IntArray(renderedWidth * renderedHeight)
        bufferedImageGetRgbMethod.invoke(rendered, 0, 0, renderedWidth, renderedHeight, renderedPixels, 0, renderedWidth)
        val renderedBitmap = Bitmap.createBitmap(renderedPixels, renderedWidth, renderedHeight, Bitmap.Config.ARGB_8888)
        val target = if (renderedWidth == bitmap.width && renderedHeight == bitmap.height) {
            renderedBitmap
        } else {
            Bitmap.createScaledBitmap(renderedBitmap, bitmap.width, bitmap.height, true)
        }
        val pixels = IntArray(bitmap.width * bitmap.height)
        target.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        bitmap.setPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
    }

    @Implementation
    fun close() {
        throwIfClosed()
        closed = true
        Shadow.extract<ShadowPdfRenderer>(owner).onPageClosed(realPage)
    }

    private fun throwIfClosed() = check(!closed) { "Page already closed" }

    companion object {
        private val bufferedImageClass = Class.forName("java.awt.image.BufferedImage")
        private val renderImageWithDpiMethod = Class.forName("org.apache.pdfbox.rendering.PDFRenderer").getMethod(
            "renderImageWithDPI", Int::class.javaPrimitiveType, Float::class.javaPrimitiveType, ImageType::class.java
        )
        private val bufferedImageWidthMethod = bufferedImageClass.getMethod("getWidth")
        private val bufferedImageHeightMethod = bufferedImageClass.getMethod("getHeight")
        private val bufferedImageGetRgbMethod = bufferedImageClass.getMethod(
            "getRGB",
            Int::class.javaPrimitiveType,
            Int::class.javaPrimitiveType,
            Int::class.javaPrimitiveType,
            Int::class.javaPrimitiveType,
            IntArray::class.java,
            Int::class.javaPrimitiveType,
            Int::class.javaPrimitiveType
        )
    }
}
