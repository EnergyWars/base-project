package com.wafflehq.base.di

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.wafflehq.base.domain.colortheme.ColorTokenCatalog
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [34])
class ModulesTest {

    private val context: Application = ApplicationProvider.getApplicationContext()

    @Test
    fun `app module provides the database and repositories`() {
        val database = AppModule.provideDatabase(context)
        assertNotNull(database)
        database.close()
        assertNotNull(AppModule.provideSettingsRepository(context))
        assertNotNull(AppModule.provideFeatureFilesRepository(context))
    }

    @Test
    fun `color theme module provides the catalog registry and dependent instances`() {
        val registry = ColorThemeModule.provideColorTokenRegistry()
        assertSame(ColorTokenCatalog.registry, registry)

        val store = ColorThemeModule.provideColorOverrideStore(context)
        assertNotNull(store)
        assertNotNull(ColorThemeModule.provideColorThemeExportRepository(store, registry))
    }
}
