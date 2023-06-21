package com.vljx.hawkspeed.domain.usecase.track

import com.vljx.hawkspeed.domain.di.Bridged
import com.vljx.hawkspeed.domain.repository.TrackPathRepository
import com.vljx.hawkspeed.domain.requestmodels.track.RequestDeleteTrackAndPath
import com.vljx.hawkspeed.domain.usecase.BaseSuspendingUseCase
import javax.inject.Inject

class DeleteTrackAndPathUseCase @Inject constructor(
    @Bridged
    private val trackPathRepository: TrackPathRepository
): BaseSuspendingUseCase<RequestDeleteTrackAndPath, Unit> {
    override suspend fun invoke(params: RequestDeleteTrackAndPath) =
        trackPathRepository.deleteTrackAndPath(params)
}