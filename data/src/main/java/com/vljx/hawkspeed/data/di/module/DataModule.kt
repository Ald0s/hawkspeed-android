package com.vljx.hawkspeed.data.di.module

import com.vljx.hawkspeed.data.database.*
import com.vljx.hawkspeed.data.network.AccountRemoteDataImpl
import com.vljx.hawkspeed.data.network.RaceOutcomeRemoteDataImpl
import com.vljx.hawkspeed.data.network.TrackPathRemoteDataImpl
import com.vljx.hawkspeed.data.network.TrackRemoteDataImpl
import com.vljx.hawkspeed.data.source.*
import com.vljx.hawkspeed.domain.di.component.AuthenticationComponent
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn

@Module
@InstallIn(AuthenticationComponent::class)
interface DataModule {
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

    @Binds
    fun bindRaceLocalData(raceLocalDataImpl: RaceLocalDataImpl): RaceLocalData

    @Binds
    fun bindRaceOutcomeLocalData(raceOutcomeLocalDataImpl: RaceOutcomeLocalDataImpl): RaceOutcomeLocalData

    @Binds
    fun bindRaceOutcomeRemoteData(raceOutcomeRemoteDataImpl: RaceOutcomeRemoteDataImpl): RaceOutcomeRemoteData
}