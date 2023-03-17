package com.vljx.hawkspeed.data.source

import com.vljx.hawkspeed.data.models.race.RaceLeaderboardPageModel
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.requests.track.PageLeaderboardRequest

interface RaceOutcomeRemoteData {
    suspend fun queryLeaderboardPage(pageLeaderboardRequest: PageLeaderboardRequest, page: Int): Resource<RaceLeaderboardPageModel>
}