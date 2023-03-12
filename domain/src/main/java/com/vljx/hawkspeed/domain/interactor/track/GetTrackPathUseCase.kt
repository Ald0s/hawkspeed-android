package com.vljx.hawkspeed.domain.interactor.track

import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.di.Bridged
import com.vljx.hawkspeed.domain.interactor.BaseUseCase
import com.vljx.hawkspeed.domain.models.track.TrackPath
import com.vljx.hawkspeed.domain.repository.TrackPathRepository
import com.vljx.hawkspeed.domain.repository.TrackRepository
import com.vljx.hawkspeed.domain.requests.track.GetTrackPathRequest
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTrackPathUseCase @Inject constructor(
    @Bridged
    private val trackPathRepository: TrackPathRepository
): BaseUseCase<GetTrackPathRequest, Flow<Resource<TrackPath>>> {
    override fun invoke(params: GetTrackPathRequest): Flow<Resource<TrackPath>> =
        trackPathRepository.getTrackPath(params)
}