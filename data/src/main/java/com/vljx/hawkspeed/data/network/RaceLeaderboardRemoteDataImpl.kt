package com.vljx.hawkspeed.data.network

import com.vljx.hawkspeed.data.models.race.RaceLeaderboardModel
import com.vljx.hawkspeed.data.models.race.RaceLeaderboardPageModel
import com.vljx.hawkspeed.data.network.api.RaceService
import com.vljx.hawkspeed.data.network.api.TrackService
import com.vljx.hawkspeed.data.network.mapper.race.RaceLeaderboardDtoMapper
import com.vljx.hawkspeed.data.network.mapper.race.RaceLeaderboardPageDtoMapper
import com.vljx.hawkspeed.data.source.race.RaceLeaderboardRemoteData
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.requestmodels.race.RequestGetRace
import com.vljx.hawkspeed.domain.requestmodels.track.RequestPageTrackLeaderboard
import javax.inject.Inject

class RaceLeaderboardRemoteDataImpl @Inject constructor(
    private val raceService: RaceService,

    private val raceLeaderboardDtoMapper: RaceLeaderboardDtoMapper,
    private val raceLeaderboardPageDtoMapper: RaceLeaderboardPageDtoMapper
): BaseRemoteData(), RaceLeaderboardRemoteData {
    override suspend fun queryLeaderboardPage(
        requestPageTrackLeaderboard: RequestPageTrackLeaderboard,
        page: Int
    ): Resource<RaceLeaderboardPageModel> = getResult({
        raceService.queryLeaderboardPage(requestPageTrackLeaderboard.trackUid, page)
    }, raceLeaderboardPageDtoMapper)

    override suspend fun queryLeaderboardEntry(requestGetRace: RequestGetRace): Resource<RaceLeaderboardModel> = getResult({
        raceService.queryRaceLeaderboardEntry(
            requestGetRace.raceUid
        )
    }, raceLeaderboardDtoMapper)
}