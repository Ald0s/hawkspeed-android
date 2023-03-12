package com.vljx.hawkspeed

import com.vljx.hawkspeed.data.database.AccountLocalDataImpl
import com.vljx.hawkspeed.data.database.TrackLocalDataImpl
import com.vljx.hawkspeed.data.database.TrackPathLocalDataImpl
import com.vljx.hawkspeed.data.di.module.DataModule
import com.vljx.hawkspeed.data.network.AccountRemoteDataImpl
import com.vljx.hawkspeed.data.network.TrackPathRemoteDataImpl
import com.vljx.hawkspeed.data.network.TrackRemoteDataImpl
import com.vljx.hawkspeed.data.source.*
import dagger.Binds
import dagger.Module
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DataModule::class]
)
interface FakeDataModule {
    @Binds
    fun bindAccountLocalData(accountLocalDataImpl: AccountLocalDataImpl): AccountLocalData

    @Binds
    fun bindAccountRemoteData(accountRemoteDataImpl: AccountRemoteDataImpl): AccountRemoteData

    @Binds
    fun bindTrackLocalData(trackLocalDataImpl: TrackLocalDataImpl): TrackLocalData

    @Binds
    fun bindTrackRemoteData(trackRemoteDataImpl: TrackRemoteDataImpl): TrackRemoteData

    @Binds
    fun bindTrackPathLocalData(trackPathLocalDataImpl: TrackPathLocalDataImpl): TrackPathLocalData

    @Binds
    fun bindTrackPathRemoteData(trackPathRemoteDataImpl: TrackPathRemoteDataImpl): TrackPathRemoteData
}