package com.vljx.hawkspeed.domain.usecase.track

import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.di.Bridged
import com.vljx.hawkspeed.domain.models.track.Track
import com.vljx.hawkspeed.domain.repository.TrackRepository
import com.vljx.hawkspeed.domain.requestmodels.track.RequestGetTrack
import com.vljx.hawkspeed.domain.usecase.BaseUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTrackUseCase @Inject constructor(
    @Bridged
    private val trackRepository: TrackRepository
): BaseUseCase<RequestGetTrack, Flow<Resource<Track>>> {
    override fun invoke(params: RequestGetTrack): Flow<Resource<Track>> =
        trackRepository.getTrack(params)
}