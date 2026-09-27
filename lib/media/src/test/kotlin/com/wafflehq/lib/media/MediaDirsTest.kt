package com.wafflehq.lib.media

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
class MediaDirsTest {

    @Test
    fun rootsAreTheDistinctTopLevelDirectories() {
        assertEquals(listOf("fakecall", "vocabulary", "finance", "recipes", "voicememo"), MediaDirs.ROOTS)
    }

    @Test
    fun fakeCallDirectoriesLiveBelowTheFakeCallRoot() {
        assertEquals("fakecall/images", MediaDirs.FAKE_CALL_IMAGES)
        assertEquals("fakecall/audio", MediaDirs.FAKE_CALL_AUDIO)
    }

    @Test
    fun allContainsEveryManagedDirectoryWithoutDuplicates() {
        assertEquals(6, MediaDirs.ALL.size)
        assertEquals(MediaDirs.ALL.size, MediaDirs.ALL.toSet().size)
    }

    @Test
    fun mediaFileManagerCreatesEveryDirectoryFromTheConstants() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        MediaFileManager(context)
        MediaDirs.ALL.forEach { relative ->
            assertTrue(relative, context.filesDir.resolve(relative).isDirectory)
        }
    }

    @Test
    fun exposedDirectoriesMatchTheConstants() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        val manager = MediaFileManager(context)
        assertEquals(context.filesDir.resolve(MediaDirs.FINANCE_RECEIPTS), manager.financeReceiptsDir)
        assertEquals(context.filesDir.resolve(MediaDirs.RECIPE_PHOTOS), manager.recipePhotosDir)
        assertEquals(context.filesDir.resolve(MediaDirs.VOICE_MEMO), manager.voiceMemoDir)
    }
}
