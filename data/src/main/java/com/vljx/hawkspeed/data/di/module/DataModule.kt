package com.vljx.hawkspeed.data.di.module

import com.vljx.hawkspeed.data.WorldSocketRepositoryImpl
import com.vljx.hawkspeed.data.database.*
import com.vljx.hawkspeed.data.network.AccountRemoteDataImpl
import com.vljx.hawkspeed.data.network.RaceLeaderboardRemoteDataImpl
import com.vljx.hawkspeed.data.network.TrackCommentRemoteDataImpl
import com.vljx.hawkspeed.data.network.TrackPathRemoteDataImpl
import com.vljx.hawkspeed.data.network.TrackRemoteDataImpl
import com.vljx.hawkspeed.data.network.UserRemoteDataImpl
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
import com.vljx.hawkspeed.domain.repository.WorldSocketRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
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
    fun bindTrackCommentLocalData(trackCommentLocalDataImpl: TrackCommentLocalDataImpl): TrackCommentLocalData

    @Binds
    fun bindTrackCommentRemoteData(trackCommentRemoteDataImpl: TrackCommentRemoteDataImpl): TrackCommentRemoteData

    @Binds
    fun bindWorldSocketRepository(worldSocketRepositoryImpl: WorldSocketRepositoryImpl): WorldSocketRepository
}