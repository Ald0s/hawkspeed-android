package com.vljx.hawkspeed.domain.usecase.track

import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import com.vljx.hawkspeed.domain.di.Bridged
import com.vljx.hawkspeed.domain.usecase.BaseUseCase
import com.vljx.hawkspeed.domain.models.race.RaceLeaderboard
import com.vljx.hawkspeed.domain.repository.LeaderboardRepository
import com.vljx.hawkspeed.domain.requestmodels.track.RequestPageTrackLeaderboard
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PageTrackLeaderboardUseCase @Inject constructor(
    @Bridged
    private val leaderboardRepository: LeaderboardRepository
): BaseUseCase<RequestPageTrackLeaderboard, Flow<PagingData<RaceLeaderboard>>> {
    @OptIn(ExperimentalPagingApi::class)
    override fun invoke(params: RequestPageTrackLeaderboard): Flow<PagingData<RaceLeaderboard>> =
        leaderboardRepository.pageLeaderboardForTrack(params)
}