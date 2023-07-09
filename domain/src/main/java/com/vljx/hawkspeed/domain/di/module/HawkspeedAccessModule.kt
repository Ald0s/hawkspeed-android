package com.vljx.hawkspeed.domain.di.module

import com.vljx.hawkspeed.domain.di.Bridged
import com.vljx.hawkspeed.domain.di.component.AuthenticationComponent
import com.vljx.hawkspeed.domain.di.component.AuthenticationComponentManager
import com.vljx.hawkspeed.domain.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(AuthenticationComponent::class)
@EntryPoint
interface AuthenticationEntryPoint {
    fun accountRepository(): AccountRepository
    fun trackRepository(): TrackRepository
    fun trackPathRepository(): TrackPathRepository
    fun trackCommentRepository(): TrackCommentRepository
    fun trackDraftRepository(): TrackDraftRepository
    fun userRepository(): UserRepository
    fun vehicleRepository(): VehicleRepository
    fun raceRepository(): RaceRepository
    fun leaderboardRepository(): LeaderboardRepository
    fun raceLeaderboardRepository(): RaceLeaderboardRepository
    fun worldRepository(): WorldRepository
}

@Module
@InstallIn(SingletonComponent::class)
class AuthenticationAccessModule {
    @Bridged
    @Provides
    fun provideAccountRepository(
        authenticationComponentManager: AuthenticationComponentManager
    ): AccountRepository =
        EntryPoints
            .get(authenticationComponentManager, AuthenticationEntryPoint::class.java)
            .accountRepository()

    @Bridged
    @Provides
    fun provideTrackRepository(
        authenticationComponentManager: AuthenticationComponentManager
    ): TrackRepository =
        EntryPoints
            .get(authenticationComponentManager, AuthenticationEntryPoint::class.java)
            .trackRepository()

    @Bridged
    @Provides
    fun provideTrackPathRepository(
        authenticationComponentManager: AuthenticationComponentManager
    ): TrackPathRepository =
        EntryPoints
            .get(authenticationComponentManager, AuthenticationEntryPoint::class.java)
            .trackPathRepository()

    @Bridged
    @Provides
    fun provideTrackCommentRepository(
        authenticationComponentManager: AuthenticationComponentManager
    ): TrackCommentRepository =
        EntryPoints
            .get(authenticationComponentManager, AuthenticationEntryPoint::class.java)
            .trackCommentRepository()

    @Bridged
    @Provides
    fun provideTrackDraftRepository(
        authenticationComponentManager: AuthenticationComponentManager
    ): TrackDraftRepository =
        EntryPoints
            .get(authenticationComponentManager, AuthenticationEntryPoint::class.java)
            .trackDraftRepository()

    @Bridged
    @Provides
    fun provideUserRepository(
        authenticationComponentManager: AuthenticationComponentManager
    ): UserRepository =
        EntryPoints
            .get(authenticationComponentManager, AuthenticationEntryPoint::class.java)
            .userRepository()

    @Bridged
    @Provides
    fun provideVehicleRepository(
        authenticationComponentManager: AuthenticationComponentManager
    ): VehicleRepository =
        EntryPoints
            .get(authenticationComponentManager, AuthenticationEntryPoint::class.java)
            .vehicleRepository()

    @Bridged
    @Provides
    fun provideRaceRepository(
        authenticationComponentManager: AuthenticationComponentManager
    ): RaceRepository =
        EntryPoints
            .get(authenticationComponentManager, AuthenticationEntryPoint::class.java)
            .raceRepository()

    @Bridged
    @Provides
    fun provideRaceOutcomeRepository(
        authenticationComponentManager: AuthenticationComponentManager
    ): LeaderboardRepository =
        EntryPoints
            .get(authenticationComponentManager, AuthenticationEntryPoint::class.java)
            .leaderboardRepository()

    @Bridged
    @Provides
    fun provideRaceLeaderboardRepository(
        authenticationComponentManager: AuthenticationComponentManager
    ): RaceLeaderboardRepository =
        EntryPoints
            .get(authenticationComponentManager, AuthenticationEntryPoint::class.java)
            .raceLeaderboardRepository()

    @Bridged
    @Provides
    fun provideWorldRepository(
        authenticationComponentManager: AuthenticationComponentManager
    ): WorldRepository =
        EntryPoints
            .get(authenticationComponentManager, AuthenticationEntryPoint::class.java)
            .worldRepository()
}