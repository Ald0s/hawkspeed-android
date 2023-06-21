package com.vljx.hawkspeed.data

import com.vljx.hawkspeed.domain.models.track.TrackWithPath
import com.vljx.hawkspeed.domain.models.world.WorldObjects
import com.vljx.hawkspeed.domain.repository.TrackPathRepository
import com.vljx.hawkspeed.domain.repository.WorldRepository
import com.vljx.hawkspeed.domain.requestmodels.world.RequestGetWorldObjects
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class WorldRepositoryImpl @Inject constructor(
    private val trackPathRepository: TrackPathRepository
): BaseRepository(), WorldRepository {
    override fun getWorldObjects(requestGetWorldObjects: RequestGetWorldObjects): Flow<WorldObjects> {
        // TODO: we should eventually adjust all calls performed here to adhere to settings, esp. viewport & proximity.
        // Retrieve a flow for all tracks with paths, where applicable.
        val tracksWithPaths: Flow<List<TrackWithPath>> = trackPathRepository.getTracksWithPathsFromCache()
        // TODO: Retrieve a flow for all Users.
        //val users: Flow<List<User>> = userRepository.getUsers()
        // TODO: when we have more than one flow, we'll return the combine() result of them all, but for now, we'll simply map tracks with path.
        return tracksWithPaths.map { tracks ->
            WorldObjects(
                tracks,
                tracks.isEmpty()
            )
        }
    }
}