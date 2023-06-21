package com.vljx.hawkspeed.data.source

import com.vljx.hawkspeed.data.models.race.RaceLeaderboardPageModel
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.requestmodels.track.RequestPageLeaderboard

interface RaceOutcomeRemoteData {
    suspend fun queryLeaderboardPage(requestPageLeaderboard: RequestPageLeaderboard, page: Int): Resource<RaceLeaderboardPageModel>
}