package com.vljx.hawkspeed.data.source.race

import com.vljx.hawkspeed.data.models.race.RaceLeaderboardModel
import com.vljx.hawkspeed.data.models.race.RaceLeaderboardPageModel
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.requestmodels.race.RequestGetRace
import com.vljx.hawkspeed.domain.requestmodels.track.RequestPageTrackLeaderboard
import com.vljx.hawkspeed.domain.requestmodels.user.RequestPageRaceHistory

interface RaceLeaderboardRemoteData {
    /**
     * Query the requested page of leaderboard entries for the given Track.
     */
    suspend fun queryLeaderboardPage(requestPageTrackLeaderboard: RequestPageTrackLeaderboard, page: Int): Resource<RaceLeaderboardPageModel>

    /**
     * Query the latest version of race leaderboard for the given race's UID.
     */
    suspend fun queryLeaderboardEntry(requestGetRace: RequestGetRace): Resource<RaceLeaderboardModel>
}