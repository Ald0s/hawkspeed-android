package com.vljx.hawkspeed.data

import androidx.paging.*
import com.vljx.hawkspeed.data.database.AppDatabase
import com.vljx.hawkspeed.data.database.entity.RaceOutcomeEntity
import com.vljx.hawkspeed.data.database.mapper.RaceOutcomeEntityMapper
import com.vljx.hawkspeed.data.mapper.race.RaceOutcomeMapper
import com.vljx.hawkspeed.data.models.race.RaceLeaderboardPageModel
import com.vljx.hawkspeed.data.remotemediator.BaseRemoteMediator
import com.vljx.hawkspeed.data.source.RaceOutcomeLocalData
import com.vljx.hawkspeed.data.source.RaceOutcomeRemoteData
import com.vljx.hawkspeed.data.source.TrackLocalData
import com.vljx.hawkspeed.domain.models.race.RaceOutcome
import com.vljx.hawkspeed.domain.repository.RaceOutcomeRepository
import com.vljx.hawkspeed.domain.requestmodels.track.RequestPageLeaderboard
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RaceOutcomeRepositoryImpl @Inject constructor(
    private val appDatabase: AppDatabase,

    private val trackLocalData: TrackLocalData,

    private val raceOutcomeRemoteData: RaceOutcomeRemoteData,
    private val raceOutcomeLocalData: RaceOutcomeLocalData,

    private val raceOutcomeEntityMapper: RaceOutcomeEntityMapper,
    private val raceOutcomeMapper: RaceOutcomeMapper
): BaseRepository(), RaceOutcomeRepository {
    @ExperimentalPagingApi
    override fun pageLeaderboardForTrack(requestPageLeaderboard: RequestPageLeaderboard): Flow<PagingData<RaceOutcome>> =
        Pager(
            config = PagingConfig(pageSize = 20),
            remoteMediator = object: BaseRemoteMediator<RaceOutcomeEntity, RaceLeaderboardPageModel>(
                appDatabase,
                remoteQuery = { loadKey -> raceOutcomeRemoteData.queryLeaderboardPage(requestPageLeaderboard, loadKey) },
                upsertQuery = { dataModel: RaceLeaderboardPageModel ->
                    // Upsert the Track object itself, to both update the Track if we have it, or add it if we don't; in case the track itself is required.
                    trackLocalData.upsertTrack(dataModel.trackModel)
                    // Now, upsert all race outcomes.
                    raceOutcomeLocalData.upsertRaceLeaderboard(dataModel)
                },
                clearAllQuery = { raceOutcomeLocalData.clearLeaderboardFor(requestPageLeaderboard.trackUid) }
            ) { },
            pagingSourceFactory = { raceOutcomeLocalData.pageRaceOutcomesFromTrack(requestPageLeaderboard) }
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