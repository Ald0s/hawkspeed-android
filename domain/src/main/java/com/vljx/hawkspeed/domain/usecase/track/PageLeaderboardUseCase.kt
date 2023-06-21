package com.vljx.hawkspeed.domain.usecase.track

import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import com.vljx.hawkspeed.domain.di.Bridged
import com.vljx.hawkspeed.domain.usecase.BaseUseCase
import com.vljx.hawkspeed.domain.models.race.RaceOutcome
import com.vljx.hawkspeed.domain.repository.RaceOutcomeRepository
import com.vljx.hawkspeed.domain.requestmodels.track.RequestPageLeaderboard
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PageLeaderboardUseCase @Inject constructor(
    @Bridged
    private val raceOutcomeRepository: RaceOutcomeRepository
): BaseUseCase<RequestPageLeaderboard, Flow<PagingData<RaceOutcome>>> {
    @OptIn(ExperimentalPagingApi::class)
    override fun invoke(params: RequestPageLeaderboard): Flow<PagingData<RaceOutcome>> =
        raceOutcomeRepository.pageLeaderboardForTrack(params)
}