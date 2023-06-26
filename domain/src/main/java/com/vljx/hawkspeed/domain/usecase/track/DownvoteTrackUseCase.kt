package com.vljx.hawkspeed.domain.usecase.track

import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.di.Bridged
import com.vljx.hawkspeed.domain.models.track.Track
import com.vljx.hawkspeed.domain.repository.TrackRepository
import com.vljx.hawkspeed.domain.requestmodels.track.RequestSetTrackRating
import com.vljx.hawkspeed.domain.usecase.BaseSuspendingUseCase
import javax.inject.Inject

class DownvoteTrackUseCase @Inject constructor(
    @Bridged
    private val trackRepository: TrackRepository
): BaseSuspendingUseCase<String, Resource<Track>> {
    override suspend fun invoke(params: String): Resource<Track> =
        trackRepository.setTrackRating(
            RequestSetTrackRating(
                params,
                false
            )
        )
}