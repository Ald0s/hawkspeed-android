package com.vljx.hawkspeed.domain.usecase.track

import com.vljx.hawkspeed.domain.models.track.TrackDraftWithPoints
import com.vljx.hawkspeed.domain.usecase.BaseUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTrackDraftUseCase @Inject constructor(

): BaseUseCase<Long, Flow<TrackDraftWithPoints?>> {
    override fun invoke(params: Long): Flow<TrackDraftWithPoints?> {
        TODO("Not yet implemented")
    }
}