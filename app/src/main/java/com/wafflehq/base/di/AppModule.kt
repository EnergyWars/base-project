package com.wafflehq.base.di

import android.content.Context
import androidx.room.Room
import com.wafflehq.base.data.db.AppDatabase
import com.wafflehq.base.data.features.FeatureFilesRepository
import com.wafflehq.base.data.settings.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder(ctx, AppDatabase::class.java, "app.db")
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    @Singleton
    fun provideSettingsRepository(@ApplicationContext ctx: Context): SettingsRepository =
        SettingsRepository(ctx)

    @Provides
    @Singleton
    fun provideFeatureFilesRepository(@ApplicationContext ctx: Context): FeatureFilesRepository =
        FeatureFilesRepository(ctx)
}
