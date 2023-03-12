package com.vljx.hawkspeed.domain.interactor.track

import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.di.Bridged
import com.vljx.hawkspeed.domain.interactor.BaseUseCase
import com.vljx.hawkspeed.domain.models.track.Track
import com.vljx.hawkspeed.domain.repository.TrackRepository
import com.vljx.hawkspeed.domain.requests.track.GetTrackRequest
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTrackUseCase @Inject constructor(
    @Bridged
    private val trackRepository: TrackRepository
): BaseUseCase<GetTrackRequest, Flow<Resource<Track>>> {
    override fun invoke(params: GetTrackRequest): Flow<Resource<Track>> =
        trackRepository.getTrack(params)
}