package com.vljx.hawkspeed.domain.usecase.track

import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import com.vljx.hawkspeed.domain.di.Bridged
import com.vljx.hawkspeed.domain.models.comment.Comment
import com.vljx.hawkspeed.domain.repository.TrackCommentRepository
import com.vljx.hawkspeed.domain.requestmodels.track.RequestPageTrackComments
import com.vljx.hawkspeed.domain.usecase.BaseUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PageTrackCommentsUseCase @Inject constructor(
    @Bridged
    private val trackCommentRepository: TrackCommentRepository
): BaseUseCase<RequestPageTrackComments, Flow<PagingData<Comment>>> {
    @OptIn(ExperimentalPagingApi::class)
    override fun invoke(params: RequestPageTrackComments): Flow<PagingData<Comment>> =
        trackCommentRepository.pageCommentsForTrack(params)
}