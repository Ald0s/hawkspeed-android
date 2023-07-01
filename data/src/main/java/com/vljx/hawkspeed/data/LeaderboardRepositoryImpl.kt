package com.vljx.hawkspeed.data

import androidx.paging.*
import com.vljx.hawkspeed.data.database.AppDatabase
import com.vljx.hawkspeed.data.database.entity.race.RaceLeaderboardEntity
import com.vljx.hawkspeed.data.database.mapper.RaceLeaderboardEntityMapper
import com.vljx.hawkspeed.data.mapper.race.RaceLeaderboardMapper
import com.vljx.hawkspeed.data.models.race.RaceLeaderboardPageModel
import com.vljx.hawkspeed.data.remotemediator.BaseRemoteMediator
import com.vljx.hawkspeed.data.source.race.RaceLeaderboardLocalData
import com.vljx.hawkspeed.data.source.race.RaceLeaderboardRemoteData
import com.vljx.hawkspeed.data.source.track.TrackLocalData
import com.vljx.hawkspeed.domain.models.race.RaceLeaderboard
import com.vljx.hawkspeed.domain.repository.LeaderboardRepository
import com.vljx.hawkspeed.domain.requestmodels.track.RequestPageTrackLeaderboard
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LeaderboardRepositoryImpl @Inject constructor(
    private val appDatabase: AppDatabase,

    private val trackLocalData: TrackLocalData,

    private val raceLeaderboardRemoteData: RaceLeaderboardRemoteData,
    private val raceLeaderboardLocalData: RaceLeaderboardLocalData,

    private val raceLeaderboardEntityMapper: RaceLeaderboardEntityMapper,
    private val raceLeaderboardMapper: RaceLeaderboardMapper
): BaseRepository(), LeaderboardRepository {
    @ExperimentalPagingApi
    override fun pageLeaderboardForTrack(requestPageTrackLeaderboard: RequestPageTrackLeaderboard): Flow<PagingData<RaceLeaderboard>> =
        Pager(
            config = PagingConfig(pageSize = 20),
            remoteMediator = object: BaseRemoteMediator<RaceLeaderboardEntity, RaceLeaderboardPageModel>(
                appDatabase,
                remoteQuery = { loadKey -> raceLeaderboardRemoteData.queryLeaderboardPage(requestPageTrackLeaderboard, loadKey) },
                upsertQuery = { dataModel: RaceLeaderboardPageModel ->
                    // Upsert the Track object itself, to both update the Track if we have it, or add it if we don't; in case the track itself is required.
                    trackLocalData.upsertTrack(dataModel.trackModel)
                    // Now, upsert all race outcomes.
                    raceLeaderboardLocalData.upsertRaceLeaderboard(dataModel)
                },
                clearAllQuery = { raceLeaderboardLocalData.clearLeaderboardFor(requestPageTrackLeaderboard.trackUid) }
            ) { },
            pagingSourceFactory = { raceLeaderboardLocalData.pageRaceLeaderboardFromTrack(requestPageTrackLeaderboard) }
        ).flow.map { raceOutcomeEntityPagingData ->
            raceOutcomeEntityPagingData.map { raceOutcomeEntity ->
                raceLeaderboardMapper.mapFromData(
                    raceLeaderboardEntityMapper.mapFromEntity(
                        raceOutcomeEntity
                    )
                )
            }
        }
}