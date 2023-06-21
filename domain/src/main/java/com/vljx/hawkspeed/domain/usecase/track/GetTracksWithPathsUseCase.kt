package com.vljx.hawkspeed.domain.usecase.track

import com.vljx.hawkspeed.domain.di.Bridged
import com.vljx.hawkspeed.domain.usecase.BaseUseCase
import com.vljx.hawkspeed.domain.models.track.TrackWithPath
import com.vljx.hawkspeed.domain.repository.TrackPathRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTracksWithPathsUseCase @Inject constructor(
    @Bridged
    private val trackPathRepository: TrackPathRepository
): BaseUseCase<Unit, Flow<List<TrackWithPath>>> {
    override fun invoke(params: Unit): Flow<List<TrackWithPath>> =
        throw NotImplementedError()
}