package com.vljx.hawkspeed.domain.usecase.user

import androidx.paging.PagingData
import com.vljx.hawkspeed.domain.models.track.Track
import com.vljx.hawkspeed.domain.requestmodels.user.RequestPageTracksForUser
import com.vljx.hawkspeed.domain.usecase.BaseUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PageUserTracksUseCase @Inject constructor(

): BaseUseCase<RequestPageTracksForUser, Flow<PagingData<Track>>> {
    override fun invoke(params: RequestPageTracksForUser): Flow<PagingData<Track>> =
        throw NotImplementedError()
}