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
import com.vljx.hawkspeed.domain.requests.track.PageLeaderboardRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RaceOutcomeRepositoryImpl @Inject constructor(
    // TODO: the Paging library itself seems to compress all layers into a single one. This is by design so yes, I will be breaking clean architecture for this.
    private val appDatabase: AppDatabase,

    private val raceOutcomeRemoteData: RaceOutcomeRemoteData,
    private val raceOutcomeLocalData: RaceOutcomeLocalData,
    private val raceOutcomeMapper: RaceOutcomeMapper,

    // TODO: this probably breaks Clean Architecture principles as we're mixing interfaces, but for now, I'll just get the job done.
    private val trackLocalData: TrackLocalData,
    private val raceOutcomeEntityMapper: RaceOutcomeEntityMapper
): BaseRepository(), RaceOutcomeRepository {
    @ExperimentalPagingApi
    override fun pageLeaderboardForTrack(pageLeaderboardRequest: PageLeaderboardRequest): Flow<PagingData<RaceOutcome>> =
        Pager(
            config = PagingConfig(pageSize = 5), /* TODO: this should be in sync with the server's configuration. */
            remoteMediator = object: BaseRemoteMediator<RaceOutcomeEntity, RaceLeaderboardPageModel>(
                appDatabase,
                remoteQuery = { loadKey -> raceOutcomeRemoteData.queryLeaderboardPage(pageLeaderboardRequest, loadKey) },
                upsertQuery = { dataModel: RaceLeaderboardPageModel ->
                    // Upsert the Track object itself, to both update the Track if we have it, or add it if we don't; in case the track itself is required.
                    trackLocalData.upsertTrack(dataModel.trackModel)
                    // Now, upsert all race outcomes.
                    raceOutcomeLocalData.upsertRaceLeaderboard(dataModel)
                },
                clearAllQuery = { raceOutcomeLocalData.clearLeaderboardFor(pageLeaderboardRequest.trackUid) }
            ) { },
            pagingSourceFactory = { raceOutcomeLocalData.pageRaceOutcomesFromTrack(pageLeaderboardRequest) }
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