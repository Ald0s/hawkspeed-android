package com.vljx.hawkspeed.domain.usecase.track

import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import com.vljx.hawkspeed.domain.di.Bridged
import com.vljx.hawkspeed.domain.models.track.TrackComment
import com.vljx.hawkspeed.domain.repository.TrackCommentRepository
import com.vljx.hawkspeed.domain.requestmodels.track.RequestPageTrackComments
import com.vljx.hawkspeed.domain.usecase.BaseUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PageTrackCommentsUseCase @Inject constructor(
    @Bridged
    private val trackCommentRepository: TrackCommentRepository
): BaseUseCase<RequestPageTrackComments, Flow<PagingData<TrackComment>>> {
    @OptIn(ExperimentalPagingApi::class)
    override fun invoke(params: RequestPageTrackComments): Flow<PagingData<TrackComment>> =
        trackCommentRepository.pageCommentsForTrack(params)
}