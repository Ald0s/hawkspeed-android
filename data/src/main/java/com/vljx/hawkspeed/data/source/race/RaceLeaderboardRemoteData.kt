package com.vljx.hawkspeed.data.source.race

import com.vljx.hawkspeed.data.models.race.RaceLeaderboardPageModel
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.requestmodels.track.RequestPageTrackLeaderboard

interface RaceLeaderboardRemoteData {
    /**
     * Query the requested page of leaderboard entries for the given Track.
     */
    suspend fun queryLeaderboardPage(requestPageTrackLeaderboard: RequestPageTrackLeaderboard, page: Int): Resource<RaceLeaderboardPageModel>
}