package com.vljx.hawkspeed

import com.vljx.hawkspeed.data.WorldSocketRepositoryImpl
import com.vljx.hawkspeed.data.database.AccountLocalDataImpl
import com.vljx.hawkspeed.data.database.RaceLocalDataImpl
import com.vljx.hawkspeed.data.database.RaceOutcomeLocalDataImpl
import com.vljx.hawkspeed.data.database.TrackCommentLocalDataImpl
import com.vljx.hawkspeed.data.database.TrackLocalDataImpl
import com.vljx.hawkspeed.data.database.TrackPathLocalDataImpl
import com.vljx.hawkspeed.data.database.UserLocalDataImpl
import com.vljx.hawkspeed.data.di.module.DataModule
import com.vljx.hawkspeed.data.network.AccountRemoteDataImpl
import com.vljx.hawkspeed.data.network.RaceOutcomeRemoteDataImpl
import com.vljx.hawkspeed.data.network.TrackCommentRemoteDataImpl
import com.vljx.hawkspeed.data.network.TrackPathRemoteDataImpl
import com.vljx.hawkspeed.data.network.TrackRemoteDataImpl
import com.vljx.hawkspeed.data.network.UserRemoteDataImpl
import com.vljx.hawkspeed.data.source.*
import com.vljx.hawkspeed.data.source.account.AccountLocalData
import com.vljx.hawkspeed.data.source.account.AccountRemoteData
import com.vljx.hawkspeed.data.source.race.RaceLocalData
import com.vljx.hawkspeed.data.source.race.RaceOutcomeLocalData
import com.vljx.hawkspeed.data.source.race.RaceOutcomeRemoteData
import com.vljx.hawkspeed.data.source.track.TrackCommentLocalData
import com.vljx.hawkspeed.data.source.user.UserLocalData
import com.vljx.hawkspeed.domain.repository.WorldSocketRepository
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

    @Binds
    fun bindRaceLocalData(raceLocalDataImpl: RaceLocalDataImpl): RaceLocalData

    @Binds
    fun bindRaceOutcomeLocalData(raceOutcomeLocalDataImpl: RaceOutcomeLocalDataImpl): RaceOutcomeLocalData

    @Binds
    fun bindRaceOutcomeRemoteData(raceOutcomeRemoteDataImpl: RaceOutcomeRemoteDataImpl): RaceOutcomeRemoteData

    @Binds
    fun bindUserLocalData(userLocalDataImpl: UserLocalDataImpl): UserLocalData

    @Binds
    fun bindUserRemoteData(userRemoteDataImpl: UserRemoteDataImpl): UserRemoteData

    @Binds
    fun bindTrackCommentLocalData(trackCommentLocalDataImpl: TrackCommentLocalDataImpl): TrackCommentLocalData

    @Binds
    fun bindTrackCommentRemoteData(trackCommentRemoteDataImpl: TrackCommentRemoteDataImpl): TrackCommentRemoteData

    @Binds
    fun bindWorldSocketRepository(worldSocketRepositoryImpl: WorldSocketRepositoryImpl): WorldSocketRepository
}