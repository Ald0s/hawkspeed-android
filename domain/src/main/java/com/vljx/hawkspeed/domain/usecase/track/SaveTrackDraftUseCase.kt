package com.vljx.hawkspeed.domain.usecase.track

import com.vljx.hawkspeed.domain.models.track.TrackDraftWithPoints
import com.vljx.hawkspeed.domain.usecase.BaseSuspendingUseCase
import javax.inject.Inject

class SaveTrackDraftUseCase @Inject constructor(

): BaseSuspendingUseCase<TrackDraftWithPoints, TrackDraftWithPoints> {
    override suspend fun invoke(params: TrackDraftWithPoints): TrackDraftWithPoints {
        TODO("Not yet implemented")
    }
}