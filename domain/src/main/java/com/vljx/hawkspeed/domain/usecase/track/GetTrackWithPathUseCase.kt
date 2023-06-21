package com.vljx.hawkspeed.domain.usecase.track

import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.di.Bridged
import com.vljx.hawkspeed.domain.models.track.TrackWithPath
import com.vljx.hawkspeed.domain.repository.TrackPathRepository
import com.vljx.hawkspeed.domain.requestmodels.track.RequestGetTrackWithPath
import com.vljx.hawkspeed.domain.usecase.BaseUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTrackWithPathUseCase @Inject constructor(
    @Bridged
    private val trackPathRepository: TrackPathRepository
): BaseUseCase<RequestGetTrackWithPath, Flow<Resource<TrackWithPath>>> {
    override fun invoke(params: RequestGetTrackWithPath): Flow<Resource<TrackWithPath>> =
        trackPathRepository.getTrackWithPath(params)
}