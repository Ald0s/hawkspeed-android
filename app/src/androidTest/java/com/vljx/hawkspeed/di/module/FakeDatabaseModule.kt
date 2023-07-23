package com.vljx.hawkspeed.di.module

import android.app.Application
import com.vljx.hawkspeed.data.database.AppDatabase
import com.vljx.hawkspeed.data.database.dao.AccountDao
import com.vljx.hawkspeed.data.database.dao.GameSettingsDao
import com.vljx.hawkspeed.data.database.dao.TrackCommentDao
import com.vljx.hawkspeed.data.database.dao.RaceDao
import com.vljx.hawkspeed.data.database.dao.RaceLeaderboardDao
import com.vljx.hawkspeed.data.database.dao.TrackDao
import com.vljx.hawkspeed.data.database.dao.TrackDraftDao
import com.vljx.hawkspeed.data.database.dao.TrackPathDao
import com.vljx.hawkspeed.data.database.dao.TrackPointDao
import com.vljx.hawkspeed.data.database.dao.TrackPointDraftDao
import com.vljx.hawkspeed.data.database.dao.UserDao
import com.vljx.hawkspeed.data.database.dao.VehicleDao
import com.vljx.hawkspeed.data.database.dao.VehicleStockDao
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
    fun provideVehicleStockDao(appDatabase: AppDatabase): VehicleStockDao =
        appDatabase.vehicleStockDao()

    @Provides
    fun provideTrackCommentDao(appDatabase: AppDatabase): TrackCommentDao =
        appDatabase.trackCommentDao()
}