package com.vljx.hawkspeed.data.network

import com.vljx.hawkspeed.data.models.race.RaceLeaderboardPageModel
import com.vljx.hawkspeed.data.network.api.TrackService
import com.vljx.hawkspeed.data.network.mapper.race.RaceLeaderboardPageDtoMapper
import com.vljx.hawkspeed.data.source.race.RaceOutcomeRemoteData
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.requestmodels.track.RequestPageTrackLeaderboard
import javax.inject.Inject

class RaceOutcomeRemoteDataImpl @Inject constructor(
    private val trackService: TrackService,

    private val raceLeaderboardPageDtoMapper: RaceLeaderboardPageDtoMapper
): BaseRemoteData(), RaceOutcomeRemoteData {
    override suspend fun queryLeaderboardPage(
        requestPageTrackLeaderboard: RequestPageTrackLeaderboard,
        page: Int
    ): Resource<RaceLeaderboardPageModel> = getResult({
        trackService.queryLeaderboardPage(requestPageTrackLeaderboard.trackUid, page)
    }, raceLeaderboardPageDtoMapper)
}