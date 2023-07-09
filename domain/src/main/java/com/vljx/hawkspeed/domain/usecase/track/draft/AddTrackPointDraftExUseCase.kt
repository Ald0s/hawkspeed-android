package com.vljx.hawkspeed.domain.usecase.track.draft

import com.vljx.hawkspeed.domain.di.Bridged
import com.vljx.hawkspeed.domain.models.track.TrackPointDraft
import com.vljx.hawkspeed.domain.repository.TrackDraftRepository
import com.vljx.hawkspeed.domain.requestmodels.track.draft.RequestAddTrackPointDraft
import com.vljx.hawkspeed.domain.usecase.BaseSuspendingUseCase
import javax.inject.Inject

/**
 * Very similar to other add track point draft, except this version returns just the saved track point draft.
 */
class AddTrackPointDraftExUseCase @Inject constructor(
    @Bridged
    private val trackDraftRepository: TrackDraftRepository
): BaseSuspendingUseCase<RequestAddTrackPointDraft, TrackPointDraft> {
    override suspend fun invoke(params: RequestAddTrackPointDraft): TrackPointDraft =
        trackDraftRepository.addPointToTrackDraftEx(params)
}