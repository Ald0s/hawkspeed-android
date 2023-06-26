package com.vljx.hawkspeed.domain.usecase.track

import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.di.Bridged
import com.vljx.hawkspeed.domain.models.trackcomment.TrackComments
import com.vljx.hawkspeed.domain.repository.TrackCommentRepository
import com.vljx.hawkspeed.domain.requestmodels.track.RequestTrackLatestComments
import com.vljx.hawkspeed.domain.usecase.BaseUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTrackLatestCommentsUseCase @Inject constructor(
    @Bridged
    private val trackCommentRepository: TrackCommentRepository
): BaseUseCase<RequestTrackLatestComments, Flow<Resource<TrackComments>>> {
    override fun invoke(params: RequestTrackLatestComments): Flow<Resource<TrackComments>> =
        trackCommentRepository.getLatestCommentsForTrack(params)
}