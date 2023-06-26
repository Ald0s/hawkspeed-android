package com.vljx.hawkspeed.data

import androidx.paging.*
import com.vljx.hawkspeed.data.database.AppDatabase
import com.vljx.hawkspeed.data.database.entity.RaceOutcomeEntity
import com.vljx.hawkspeed.data.database.mapper.RaceOutcomeEntityMapper
import com.vljx.hawkspeed.data.mapper.race.RaceOutcomeMapper
import com.vljx.hawkspeed.data.models.race.RaceLeaderboardPageModel
import com.vljx.hawkspeed.data.remotemediator.BaseRemoteMediator
import com.vljx.hawkspeed.data.source.race.RaceOutcomeLocalData
import com.vljx.hawkspeed.data.source.race.RaceOutcomeRemoteData
import com.vljx.hawkspeed.data.source.track.TrackLocalData
import com.vljx.hawkspeed.domain.models.race.RaceOutcome
import com.vljx.hawkspeed.domain.repository.LeaderboardRepository
import com.vljx.hawkspeed.domain.requestmodels.track.RequestPageTrackLeaderboard
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LeaderboardRepositoryImpl @Inject constructor(
    private val appDatabase: AppDatabase,

    private val trackLocalData: TrackLocalData,

    private val raceOutcomeRemoteData: RaceOutcomeRemoteData,
    private val raceOutcomeLocalData: RaceOutcomeLocalData,

    private val raceOutcomeEntityMapper: RaceOutcomeEntityMapper,
    private val raceOutcomeMapper: RaceOutcomeMapper
): BaseRepository(), LeaderboardRepository {
    @ExperimentalPagingApi
    override fun pageLeaderboardForTrack(requestPageTrackLeaderboard: RequestPageTrackLeaderboard): Flow<PagingData<RaceOutcome>> =
        Pager(
            config = PagingConfig(pageSize = 20),
            remoteMediator = object: BaseRemoteMediator<RaceOutcomeEntity, RaceLeaderboardPageModel>(
                appDatabase,
                remoteQuery = { loadKey -> raceOutcomeRemoteData.queryLeaderboardPage(requestPageTrackLeaderboard, loadKey) },
                upsertQuery = { dataModel: RaceLeaderboardPageModel ->
                    // Upsert the Track object itself, to both update the Track if we have it, or add it if we don't; in case the track itself is required.
                    trackLocalData.upsertTrack(dataModel.trackModel)
                    // Now, upsert all race outcomes.
                    raceOutcomeLocalData.upsertRaceLeaderboard(dataModel)
                },
                clearAllQuery = { raceOutcomeLocalData.clearLeaderboardFor(requestPageTrackLeaderboard.trackUid) }
            ) { },
            pagingSourceFactory = { raceOutcomeLocalData.pageRaceOutcomesFromTrack(requestPageTrackLeaderboard) }
        ).flow.map { raceOutcomeEntityPagingData ->
            raceOutcomeEntityPagingData.map { raceOutcomeEntity ->
                raceOutcomeMapper.mapFromData(
                    raceOutcomeEntityMapper.mapFromEntity(
                        raceOutcomeEntity
                    )
                )
            }
        }
}