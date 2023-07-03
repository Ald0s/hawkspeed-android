package com.vljx.hawkspeed.data.di.module

import com.vljx.hawkspeed.data.*
import com.vljx.hawkspeed.domain.di.component.AuthenticationComponent
import com.vljx.hawkspeed.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn

@Module
@InstallIn(AuthenticationComponent::class)
interface DomainModule {
    @Binds
    fun bindAccountRepository(accountRepositoryImpl: AccountRepositoryImpl): AccountRepository

    @Binds
    fun bindTrackRepository(trackRepositoryImpl: TrackRepositoryImpl): TrackRepository

    @Binds
    fun bindTrackPathRepository(trackPathRepositoryImpl: TrackPathRepositoryImpl): TrackPathRepository

    @Binds
    fun bindTrackCommentRepository(trackCommentRepositoryImpl: TrackCommentRepositoryImpl): TrackCommentRepository

    @Binds
    fun bindTrackDraftRepository(trackDraftRepositoryImpl: TrackDraftRepositoryImpl): TrackDraftRepository

    @Binds
    fun bindRaceRepository(raceRepositoryImpl: RaceRepositoryImpl): RaceRepository

    @Binds
    fun bindRaceOutcomeRepository(raceOutcomeRepositoryImpl: LeaderboardRepositoryImpl): LeaderboardRepository

    @Binds
    fun bindUserRepository(userRepositoryImpl: UserRepositoryImpl): UserRepository

    @Binds
    fun bindVehicleRepository(vehicleRepositoryImpl: VehicleRepositoryImpl): VehicleRepository

    @Binds
    fun bindWorldRepository(worldRepositoryImpl: WorldRepositoryImpl): WorldRepository
}