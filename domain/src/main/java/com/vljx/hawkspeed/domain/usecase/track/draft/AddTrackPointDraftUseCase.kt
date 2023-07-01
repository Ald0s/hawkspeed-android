package com.vljx.hawkspeed.domain.usecase.track.draft

import com.vljx.hawkspeed.domain.di.Bridged
import com.vljx.hawkspeed.domain.models.track.TrackDraftWithPoints
import com.vljx.hawkspeed.domain.repository.TrackDraftRepository
import com.vljx.hawkspeed.domain.requestmodels.track.draft.RequestAddTrackPointDraft
import com.vljx.hawkspeed.domain.usecase.BaseSuspendingUseCase
import javax.inject.Inject

class AddTrackPointDraftUseCase @Inject constructor(
    @Bridged
    private val trackDraftRepository: TrackDraftRepository
): BaseSuspendingUseCase<RequestAddTrackPointDraft, TrackDraftWithPoints> {
    override suspend fun invoke(params: RequestAddTrackPointDraft): TrackDraftWithPoints =
        trackDraftRepository.addPointToTrackDraft(params)
}