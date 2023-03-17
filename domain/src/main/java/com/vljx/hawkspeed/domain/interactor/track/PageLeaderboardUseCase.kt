package com.vljx.hawkspeed.domain.interactor.track

import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import com.vljx.hawkspeed.domain.di.Bridged
import com.vljx.hawkspeed.domain.interactor.BaseUseCase
import com.vljx.hawkspeed.domain.models.race.RaceOutcome
import com.vljx.hawkspeed.domain.repository.RaceOutcomeRepository
import com.vljx.hawkspeed.domain.requests.track.PageLeaderboardRequest
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PageLeaderboardUseCase @Inject constructor(
    @Bridged
    private val raceOutcomeRepository: RaceOutcomeRepository
): BaseUseCase<PageLeaderboardRequest, Flow<PagingData<RaceOutcome>>> {
    @OptIn(ExperimentalPagingApi::class)
    override fun invoke(params: PageLeaderboardRequest): Flow<PagingData<RaceOutcome>> =
        raceOutcomeRepository.pageLeaderboardForTrack(params)
}