package com.wafflehq.base.ui.library.demos

import android.app.Application
import com.wafflehq.base.R
import com.wafflehq.base.ui.library.LibraryDemoTest
import com.wafflehq.lib.media.MediaDirs
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34], qualifiers = "w411dp-h1800dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MediaDemoTest : LibraryDemoTest() {

    private fun showDemo() = show { MediaDemo(workDispatcher = Dispatchers.Unconfined) }

    @Test
    fun listsTheMediaDirectoriesWithoutCreatingThem() {
        showDemo()

        node(DemoTags.section("media")).assertExists()
        assertTagText(
            MediaTags.DIRECTORIES,
            string(R.string.libex_media_directories, MediaDirs.ROOTS.joinToString(), MediaDirs.ALL.size),
        )
        assertTrue(!File(context.filesDir, MediaDirs.VOCABULARY_IMAGES).exists())
    }

    @Test
    fun storeAndLoadShowsTheLoadedImage() {
        showDemo()

        click(MediaTags.RUN)
        waitForTag(MediaTags.RESULT)

        node(MediaTags.IMAGE).assertExists()
        node(MediaTags.ERROR).assertDoesNotExist()
    }

    @Test
    fun storeAndLoadReturnsADownscaledBitmapAndCleansUp() {
        val result = MediaDemoLogic.storeAndLoad(context)

        assertNotNull(result)
        assertTrue(result!!.width in 1..MediaDemoLogic.MAX_DIMENSION_PX)
        assertTrue(result.height in 1..MediaDemoLogic.MAX_DIMENSION_PX)
        assertTrue(result.storedBytes > 0)
        val leftovers = File(context.filesDir, MediaDirs.VOCABULARY_IMAGES).listFiles().orEmpty()
        assertEquals(0, leftovers.size)
    }

    @Test
    fun directoriesAreResolvedBelowTheFilesFolder() {
        val directories = MediaDemoLogic.directories(context)

        assertEquals(MediaDirs.ALL.size, directories.size)
        assertTrue(directories.all { it.startsWith(context.filesDir.path) })
    }
}
