package com.vljx.hawkspeed.data.di.module

import com.vljx.hawkspeed.data.AccountRepositoryImpl
import com.vljx.hawkspeed.data.TrackPathRepositoryImpl
import com.vljx.hawkspeed.data.TrackRepositoryImpl
import com.vljx.hawkspeed.domain.di.component.AuthenticationComponent
import com.vljx.hawkspeed.domain.repository.AccountRepository
import com.vljx.hawkspeed.domain.repository.TrackPathRepository
import com.vljx.hawkspeed.domain.repository.TrackRepository
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
}