package com.vljx.hawkspeed.domain.usecase.track.draft

import com.vljx.hawkspeed.domain.di.Bridged
import com.vljx.hawkspeed.domain.models.track.TrackDraftWithPoints
import com.vljx.hawkspeed.domain.repository.TrackDraftRepository
import com.vljx.hawkspeed.domain.usecase.BaseUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTrackDraftUseCase @Inject constructor(
    @Bridged
    private val trackDraftRepository: TrackDraftRepository
): BaseUseCase<Long, Flow<TrackDraftWithPoints?>> {
    override fun invoke(params: Long): Flow<TrackDraftWithPoints?> =
        trackDraftRepository.getTrackDraftWithPointsById(params)
}