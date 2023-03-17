package com.vljx.hawkspeed.domain.interactor.track

import com.vljx.hawkspeed.domain.di.Bridged
import com.vljx.hawkspeed.domain.interactor.BaseUseCase
import com.vljx.hawkspeed.domain.models.track.TrackWithPath
import com.vljx.hawkspeed.domain.repository.TrackPathRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTracksWithPathsUseCase @Inject constructor(
    @Bridged
    private val trackPathRepository: TrackPathRepository
): BaseUseCase<Unit, Flow<List<TrackWithPath>>> {
    override fun invoke(params: Unit): Flow<List<TrackWithPath>> =
        trackPathRepository.getTracksWithPaths()
}