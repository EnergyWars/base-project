package com.wafflehq.base.di

import android.content.Context
import com.wafflehq.base.domain.colortheme.ColorTokenCatalog
import com.wafflehq.lib.settings.colors.ColorOverrideStore
import com.wafflehq.lib.settings.colors.ColorThemeExportRepository
import com.wafflehq.lib.settings.colors.ColorTokenRegistry
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ColorThemeModule {

    @Provides
    @Singleton
    fun provideColorOverrideStore(@ApplicationContext context: Context): ColorOverrideStore =
        ColorOverrideStore(context)

    @Provides
    @Singleton
    fun provideColorTokenRegistry(): ColorTokenRegistry = ColorTokenCatalog.registry

    @Provides
    @Singleton
    fun provideColorThemeExportRepository(
        store: ColorOverrideStore,
        registry: ColorTokenRegistry
    ): ColorThemeExportRepository = ColorThemeExportRepository(store, registry)
}
