package com.vljx.hawkspeed.domain.interactor.track

import com.vljx.hawkspeed.domain.di.Bridged
import com.vljx.hawkspeed.domain.interactor.BaseUseCase
import com.vljx.hawkspeed.domain.models.track.Track
import com.vljx.hawkspeed.domain.repository.TrackRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTracksUseCase @Inject constructor(
    @Bridged
    private val trackRepository: TrackRepository
): BaseUseCase<Unit, Flow<List<Track>>> {
    override fun invoke(params: Unit): Flow<List<Track>> =
        trackRepository.getCachedTracks()
}