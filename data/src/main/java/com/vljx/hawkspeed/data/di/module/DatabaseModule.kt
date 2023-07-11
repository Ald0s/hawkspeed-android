package com.vljx.hawkspeed.data.di.module

import android.app.Application
import com.vljx.hawkspeed.data.database.AppDatabase
import com.vljx.hawkspeed.data.database.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {
    @Provides
    fun provideDatabase(application: Application): AppDatabase =
        AppDatabase.invoke(application)

    @Provides
    fun provideAccountDao(appDatabase: AppDatabase): AccountDao =
        appDatabase.accountDao()

    @Provides
    fun provideGameSettingsDao(appDatabase: AppDatabase): GameSettingsDao =
        appDatabase.gameSettingsDao()

    @Provides
    fun provideTrackDao(appDatabase: AppDatabase): TrackDao =
        appDatabase.trackDao()

    @Provides
    fun provideTrackPathDao(appDatabase: AppDatabase): TrackPathDao =
        appDatabase.trackPathDao()

    @Provides
    fun provideTrackPointDao(appDatabase: AppDatabase): TrackPointDao =
        appDatabase.trackPointDao()

    @Provides
    fun provideTrackDraftDao(appDatabase: AppDatabase): TrackDraftDao =
        appDatabase.trackDraftDao()

    @Provides
    fun provideTrackPointDraftDao(appDatabase: AppDatabase): TrackPointDraftDao =
        appDatabase.trackPointDraftDao()

    @Provides
    fun provideRaceDao(appDatabase: AppDatabase): RaceDao =
        appDatabase.raceDao()

    @Provides
    fun provideRaceOutcomeDao(appDatabase: AppDatabase): RaceLeaderboardDao =
        appDatabase.raceOutComeDao()

    @Provides
    fun provideUserDao(appDatabase: AppDatabase): UserDao =
        appDatabase.userDao()

    @Provides
    fun provideVehicleDao(appDatabase: AppDatabase): VehicleDao =
        appDatabase.vehicleDao()

    @Provides
    fun provideTrackCommentDao(appDatabase: AppDatabase): TrackCommentDao =
        appDatabase.trackCommentDao()
}