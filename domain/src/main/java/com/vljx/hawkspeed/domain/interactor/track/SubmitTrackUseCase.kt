package com.vljx.hawkspeed.domain.interactor.track

import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.di.Bridged
import com.vljx.hawkspeed.domain.interactor.BaseSuspendingUseCase
import com.vljx.hawkspeed.domain.models.track.Track
import com.vljx.hawkspeed.domain.repository.TrackRepository
import com.vljx.hawkspeed.domain.requests.SubmitTrackRequest
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SubmitTrackUseCase @Inject constructor(
    @Bridged
    private val trackRepository: TrackRepository
): BaseSuspendingUseCase<SubmitTrackRequest, Flow<Resource<Track>>> {
    override suspend fun invoke(params: SubmitTrackRequest): Flow<Resource<Track>> =
        trackRepository.submitNewTrack(params)
}