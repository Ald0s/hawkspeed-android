package com.vljx.hawkspeed.domain.usecase.track.draft

import com.vljx.hawkspeed.domain.di.Bridged
import com.vljx.hawkspeed.domain.repository.TrackDraftRepository
import com.vljx.hawkspeed.domain.usecase.BaseSuspendingUseCase
import javax.inject.Inject

class DeleteTrackDraftUseCase @Inject constructor(
    @Bridged
    private val trackDraftRepository: TrackDraftRepository
): BaseSuspendingUseCase<Long, Unit> {
    override suspend fun invoke(params: Long) =
        trackDraftRepository.deleteTrackDraft(params)
}