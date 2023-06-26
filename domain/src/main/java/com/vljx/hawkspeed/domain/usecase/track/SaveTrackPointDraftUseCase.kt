package com.vljx.hawkspeed.domain.usecase.track

import com.vljx.hawkspeed.domain.models.track.TrackDraftWithPoints
import com.vljx.hawkspeed.domain.requestmodels.track.RequestSaveTrackPointDraft
import com.vljx.hawkspeed.domain.usecase.BaseSuspendingUseCase
import javax.inject.Inject

class SaveTrackPointDraftUseCase @Inject constructor(

): BaseSuspendingUseCase<RequestSaveTrackPointDraft, TrackDraftWithPoints> {
    override suspend fun invoke(params: RequestSaveTrackPointDraft): TrackDraftWithPoints {
        TODO("Not yet implemented")
    }
}