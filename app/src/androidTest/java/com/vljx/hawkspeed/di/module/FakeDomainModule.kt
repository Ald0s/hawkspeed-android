package com.vljx.hawkspeed.di.module

import com.vljx.hawkspeed.data.AccountRepositoryImpl
import com.vljx.hawkspeed.data.GameSettingsRepositoryImpl
import com.vljx.hawkspeed.data.LeaderboardRepositoryImpl
import com.vljx.hawkspeed.data.RaceLeaderboardRepositoryImpl
import com.vljx.hawkspeed.data.RaceRepositoryImpl
import com.vljx.hawkspeed.data.TrackCommentRepositoryImpl
import com.vljx.hawkspeed.data.TrackDraftRepositoryImpl
import com.vljx.hawkspeed.data.TrackPathRepositoryImpl
import com.vljx.hawkspeed.data.TrackRepositoryImpl
import com.vljx.hawkspeed.data.UserRepositoryImpl
import com.vljx.hawkspeed.data.VehicleRepositoryImpl
import com.vljx.hawkspeed.data.WorldRepositoryImpl
import com.vljx.hawkspeed.data.di.module.DomainModule
import com.vljx.hawkspeed.domain.di.component.AuthenticationComponent
import com.vljx.hawkspeed.domain.repository.AccountRepository
import com.vljx.hawkspeed.domain.repository.GameSettingsRepository
import com.vljx.hawkspeed.domain.repository.LeaderboardRepository
import com.vljx.hawkspeed.domain.repository.RaceLeaderboardRepository
import com.vljx.hawkspeed.domain.repository.RaceRepository
import com.vljx.hawkspeed.domain.repository.TrackCommentRepository
import com.vljx.hawkspeed.domain.repository.TrackDraftRepository
import com.vljx.hawkspeed.domain.repository.TrackPathRepository
import com.vljx.hawkspeed.domain.repository.TrackRepository
import com.vljx.hawkspeed.domain.repository.UserRepository
import com.vljx.hawkspeed.domain.repository.VehicleRepository
import com.vljx.hawkspeed.domain.repository.WorldRepository
import com.vljx.hawkspeed.repository.FakeTrackPathRepositoryImpl
import com.vljx.hawkspeed.repository.FakeVehicleRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.testing.TestInstallIn

@Module
@TestInstallIn(
    components = [AuthenticationComponent::class],
    replaces = [DomainModule::class]
)
interface FakeDomainModule {
    @Binds
    fun bindAccountRepository(accountRepositoryImpl: AccountRepositoryImpl): AccountRepository

    @Binds
    fun bindGameSettingsRepository(gameSettingsRepositoryImpl: GameSettingsRepositoryImpl): GameSettingsRepository

    @Binds
    fun bindTrackRepository(trackRepositoryImpl: TrackRepositoryImpl): TrackRepository

    @Binds
    fun bindTrackPathRepository(fakeTrackPathRepositoryImpl: FakeTrackPathRepositoryImpl): TrackPathRepository

    @Binds
    fun bindTrackCommentRepository(trackCommentRepositoryImpl: TrackCommentRepositoryImpl): TrackCommentRepository

    @Binds
    fun bindTrackDraftRepository(trackDraftRepositoryImpl: TrackDraftRepositoryImpl): TrackDraftRepository

    @Binds
    fun bindRaceRepository(raceRepositoryImpl: RaceRepositoryImpl): RaceRepository

    @Binds
    fun bindLeaderboardRepository(leaderboardRepositoryImpl: LeaderboardRepositoryImpl): LeaderboardRepository

    @Binds
    fun bindRaceLeaderboardRepository(raceLeaderboardRepositoryImpl: RaceLeaderboardRepositoryImpl): RaceLeaderboardRepository

    @Binds
    fun bindUserRepository(userRepositoryImpl: UserRepositoryImpl): UserRepository

    @Binds
    fun bindVehicleRepository(fakeVehicleRepositoryImpl: FakeVehicleRepositoryImpl): VehicleRepository

    @Binds
    fun bindWorldRepository(worldRepositoryImpl: WorldRepositoryImpl): WorldRepository
}