package com.vljx.hawkspeed.domain.usecase.track

import com.vljx.hawkspeed.domain.models.track.TrackDraftWithPoints
import com.vljx.hawkspeed.domain.usecase.BaseUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NewTrackDraftUseCase @Inject constructor(

): BaseUseCase<Unit, Flow<TrackDraftWithPoints>> {
    override fun invoke(params: Unit): Flow<TrackDraftWithPoints> {
        TODO("Not yet implemented")
    }
}