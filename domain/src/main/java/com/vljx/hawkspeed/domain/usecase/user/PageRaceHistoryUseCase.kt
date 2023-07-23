package com.vljx.hawkspeed.domain.usecase.user

import androidx.paging.PagingData
import com.vljx.hawkspeed.domain.di.Bridged
import com.vljx.hawkspeed.domain.models.race.RaceLeaderboard
import com.vljx.hawkspeed.domain.requestmodels.user.RequestPageRaceHistory
import com.vljx.hawkspeed.domain.usecase.BaseUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PageRaceHistoryUseCase @Inject constructor(

): BaseUseCase<RequestPageRaceHistory, Flow<PagingData<RaceLeaderboard>>> {
    override fun invoke(params: RequestPageRaceHistory): Flow<PagingData<RaceLeaderboard>> =
        throw NotImplementedError()
}