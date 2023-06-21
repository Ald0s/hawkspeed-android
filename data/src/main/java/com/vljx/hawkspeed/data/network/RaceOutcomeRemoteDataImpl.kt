package com.vljx.hawkspeed.data.network

import com.vljx.hawkspeed.data.models.race.RaceLeaderboardPageModel
import com.vljx.hawkspeed.data.network.api.TrackService
import com.vljx.hawkspeed.data.network.mapper.race.RaceLeaderboardPageDtoMapper
import com.vljx.hawkspeed.data.source.RaceOutcomeRemoteData
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.requestmodels.track.RequestPageLeaderboard
import javax.inject.Inject

class RaceOutcomeRemoteDataImpl @Inject constructor(
    private val trackService: TrackService,

    private val raceLeaderboardPageDtoMapper: RaceLeaderboardPageDtoMapper
): BaseRemoteData(), RaceOutcomeRemoteData {
    override suspend fun queryLeaderboardPage(
        requestPageLeaderboard: RequestPageLeaderboard,
        page: Int
    ): Resource<RaceLeaderboardPageModel> = getResult({
        trackService.queryLeaderboardPage(requestPageLeaderboard.trackUid, page)
    }, raceLeaderboardPageDtoMapper)
}