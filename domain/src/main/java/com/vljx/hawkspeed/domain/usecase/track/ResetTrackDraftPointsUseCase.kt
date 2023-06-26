package com.vljx.hawkspeed.domain.usecase.track

import com.vljx.hawkspeed.domain.models.track.TrackDraftWithPoints
import com.vljx.hawkspeed.domain.usecase.BaseSuspendingUseCase
import javax.inject.Inject

class ResetTrackDraftPointsUseCase @Inject constructor(

): BaseSuspendingUseCase<Long, TrackDraftWithPoints> {
    override suspend fun invoke(params: Long): TrackDraftWithPoints {
        TODO("Not yet implemented")
    }
}