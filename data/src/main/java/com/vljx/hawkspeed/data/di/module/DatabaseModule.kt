package com.vljx.hawkspeed.data.di.module

import android.app.Application
import com.vljx.hawkspeed.data.database.AppDatabase
import com.vljx.hawkspeed.data.database.dao.AccountDao
import com.vljx.hawkspeed.data.database.dao.TrackDao
import com.vljx.hawkspeed.data.database.dao.TrackPointDao
import com.vljx.hawkspeed.domain.di.component.AuthenticationComponent
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import javax.inject.Singleton

@Module
@InstallIn(AuthenticationComponent::class)
class DatabaseModule {
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