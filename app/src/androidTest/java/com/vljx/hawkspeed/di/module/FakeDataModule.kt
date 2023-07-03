package com.vljx.hawkspeed.di.module

import com.vljx.hawkspeed.data.database.AccountLocalDataImpl
import com.vljx.hawkspeed.data.database.RaceLocalDataImpl
import com.vljx.hawkspeed.data.database.RaceLeaderboardLocalDataImpl
import com.vljx.hawkspeed.data.database.TrackCommentLocalDataImpl
import com.vljx.hawkspeed.data.database.TrackDraftLocalDataImpl
import com.vljx.hawkspeed.data.database.TrackLocalDataImpl
import com.vljx.hawkspeed.data.database.TrackPathLocalDataImpl
import com.vljx.hawkspeed.data.database.UserLocalDataImpl
import com.vljx.hawkspeed.data.database.VehicleLocalDataImpl
import com.vljx.hawkspeed.data.di.module.DataModule
import com.vljx.hawkspeed.data.network.AccountRemoteDataImpl
import com.vljx.hawkspeed.data.network.RaceLeaderboardRemoteDataImpl
import com.vljx.hawkspeed.data.network.TrackCommentRemoteDataImpl
import com.vljx.hawkspeed.data.network.TrackPathRemoteDataImpl
import com.vljx.hawkspeed.data.network.TrackRemoteDataImpl
import com.vljx.hawkspeed.data.network.UserRemoteDataImpl
import com.vljx.hawkspeed.data.network.VehicleRemoteDataImpl
import com.vljx.hawkspeed.data.source.*
import com.vljx.hawkspeed.data.source.account.AccountLocalData
import com.vljx.hawkspeed.data.source.account.AccountRemoteData
import com.vljx.hawkspeed.data.source.race.RaceLocalData
import com.vljx.hawkspeed.data.source.race.RaceLeaderboardLocalData
import com.vljx.hawkspeed.data.source.race.RaceLeaderboardRemoteData
import com.vljx.hawkspeed.data.source.track.TrackCommentLocalData
import com.vljx.hawkspeed.data.source.track.TrackCommentRemoteData
import com.vljx.hawkspeed.data.source.track.TrackDraftLocalData
import com.vljx.hawkspeed.data.source.track.TrackLocalData
import com.vljx.hawkspeed.data.source.track.TrackPathLocalData
import com.vljx.hawkspeed.data.source.track.TrackPathRemoteData
import com.vljx.hawkspeed.data.source.track.TrackRemoteData
import com.vljx.hawkspeed.data.source.user.UserLocalData
import com.vljx.hawkspeed.data.source.vehicle.VehicleLocalData
import com.vljx.hawkspeed.data.source.vehicle.VehicleRemoteData
import com.vljx.hawkspeed.domain.repository.WorldSocketRepository
import com.vljx.hawkspeed.repository.FakeWorldSocketRepositoryImpl
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
    fun bindTrackDraftLocalData(trackDraftLocalDataImpl: TrackDraftLocalDataImpl): TrackDraftLocalData

    @Binds
    fun bindRaceLocalData(raceLocalDataImpl: RaceLocalDataImpl): RaceLocalData

    @Binds
    fun bindRaceOutcomeLocalData(raceOutcomeLocalDataImpl: RaceLeaderboardLocalDataImpl): RaceLeaderboardLocalData

    @Binds
    fun bindRaceOutcomeRemoteData(raceOutcomeRemoteDataImpl: RaceLeaderboardRemoteDataImpl): RaceLeaderboardRemoteData

    @Binds
    fun bindUserLocalData(userLocalDataImpl: UserLocalDataImpl): UserLocalData

    @Binds
    fun bindUserRemoteData(userRemoteDataImpl: UserRemoteDataImpl): UserRemoteData

    @Binds
    fun bindVehicleLocalData(vehicleLocalDataImpl: VehicleLocalDataImpl): VehicleLocalData

    @Binds
    fun bindVehicleRemoteData(vehicleRemoteDataImpl: VehicleRemoteDataImpl): VehicleRemoteData

    @Binds
    fun bindTrackCommentLocalData(trackCommentLocalDataImpl: TrackCommentLocalDataImpl): TrackCommentLocalData

    @Binds
    fun bindTrackCommentRemoteData(trackCommentRemoteDataImpl: TrackCommentRemoteDataImpl): TrackCommentRemoteData

    @Binds
    fun bindWorldSocketRepository(fakeWorldSocketRepositoryImpl: FakeWorldSocketRepositoryImpl): WorldSocketRepository
}