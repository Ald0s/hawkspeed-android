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
    fun raceRepository(): RaceRepository
    fun raceOutcomeRepository(): RaceOutcomeRepository
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
    ): RaceOutcomeRepository =
        EntryPoints
            .get(authenticationComponentManager, AuthenticationEntryPoint::class.java)
            .raceOutcomeRepository()

    @Bridged
    @Provides
    fun provideWorldRepository(
        authenticationComponentManager: AuthenticationComponentManager
    ): WorldRepository =
        EntryPoints
            .get(authenticationComponentManager, AuthenticationEntryPoint::class.java)
            .worldRepository()
}