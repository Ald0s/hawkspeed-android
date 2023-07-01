package com.vljx.hawkspeed.domain.usecase.track.draft

import com.vljx.hawkspeed.domain.di.Bridged
import com.vljx.hawkspeed.domain.models.track.TrackDraftWithPoints
import com.vljx.hawkspeed.domain.repository.TrackDraftRepository
import com.vljx.hawkspeed.domain.usecase.BaseSuspendingUseCase
import javax.inject.Inject

class SaveTrackDraftUseCase @Inject constructor(
    @Bridged
    private val trackDraftRepository: TrackDraftRepository
): BaseSuspendingUseCase<TrackDraftWithPoints, Unit> {
    override suspend fun invoke(params: TrackDraftWithPoints) =
        trackDraftRepository.saveTrackDraft(params)
}