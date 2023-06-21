package com.vljx.hawkspeed.domain.usecase.track

import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.di.Bridged
import com.vljx.hawkspeed.domain.usecase.BaseUseCase
import com.vljx.hawkspeed.domain.models.track.Track
import com.vljx.hawkspeed.domain.repository.TrackRepository
import com.vljx.hawkspeed.domain.requestmodels.track.RequestSubmitTrack
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SubmitTrackUseCase @Inject constructor(
    @Bridged
    private val trackRepository: TrackRepository
): BaseUseCase<RequestSubmitTrack, Flow<Resource<Track>>> {
    override fun invoke(params: RequestSubmitTrack): Flow<Resource<Track>> =
        trackRepository.submitNewTrack(params)
}