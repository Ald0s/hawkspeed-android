package com.vljx.hawkspeed

import android.app.Application
import com.vljx.hawkspeed.data.database.AppDatabase
import com.vljx.hawkspeed.data.database.dao.AccountDao
import com.vljx.hawkspeed.data.database.dao.TrackDao
import com.vljx.hawkspeed.data.database.dao.TrackPointDao
import com.vljx.hawkspeed.data.di.module.DatabaseModule
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DatabaseModule::class]
)
class FakeDatabaseModule {
    @Provides
    fun provideDatabase(application: Application): AppDatabase =
        AppDatabase.invoke(application)

    @Provides
    fun provideAccountDao(appDatabase: AppDatabase): AccountDao =
        appDatabase.accountDao()

    @Provides
    fun provideTrackDao(appDatabase: AppDatabase): TrackDao =
        appDatabase.trackDao()

    @Provides
    fun provideTrackPointDao(appDatabase: AppDatabase): TrackPointDao =
        appDatabase.trackPointDao()
}