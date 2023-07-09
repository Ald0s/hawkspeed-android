package com.vljx.hawkspeed.domain.usecase.track.draft

import com.vljx.hawkspeed.domain.di.Bridged
import com.vljx.hawkspeed.domain.models.track.TrackDraftWithPoints
import com.vljx.hawkspeed.domain.repository.TrackDraftRepository
import com.vljx.hawkspeed.domain.requestmodels.track.draft.RequestNewTrackDraft
import com.vljx.hawkspeed.domain.usecase.BaseUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import javax.inject.Inject

class NewTrackDraftUseCase @Inject constructor(
    @Bridged
    private val trackDraftRepository: TrackDraftRepository
): BaseUseCase<RequestNewTrackDraft, Flow<TrackDraftWithPoints>> {
    override fun invoke(params: RequestNewTrackDraft): Flow<TrackDraftWithPoints> =
        trackDraftRepository.newTrackDraftWithPoints(params)
            .filterNotNull()
}