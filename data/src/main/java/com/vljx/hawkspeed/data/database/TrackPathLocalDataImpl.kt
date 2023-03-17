package com.vljx.hawkspeed.data.database

import androidx.room.Transaction
import com.vljx.hawkspeed.data.database.dao.TrackPointDao
import com.vljx.hawkspeed.data.database.entity.TrackPointEntity
import com.vljx.hawkspeed.data.database.mapper.TrackEntityMapper
import com.vljx.hawkspeed.data.database.mapper.TrackPointEntityMapper
import com.vljx.hawkspeed.data.database.relationships.TrackWithPoints
import com.vljx.hawkspeed.data.models.track.TrackModel
import com.vljx.hawkspeed.data.models.track.TrackPathModel
import com.vljx.hawkspeed.data.models.track.TrackWithPathModel
import com.vljx.hawkspeed.data.source.TrackPathLocalData
import com.vljx.hawkspeed.domain.requests.track.GetTrackPathRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TrackPathLocalDataImpl @Inject constructor(
    private val trackPointDao: TrackPointDao,

    private val trackEntityMapper: TrackEntityMapper,
    private val trackPointEntityMapper: TrackPointEntityMapper
): TrackPathLocalData {
    override fun selectTrackPath(getTrackPathRequest: GetTrackPathRequest): Flow<TrackPathModel?> {
        // Get a flow for a list of all points for the given track uid.
        val trackPointsListFlow: Flow<List<TrackPointEntity>?> = trackPointDao.selectPointsForTrackUid(getTrackPathRequest.trackUid)
        // Return the mapping of this flow to a track path model, if the list is present.
        return trackPointsListFlow.map { trackPointsEntityList ->
            trackPointsEntityList?.run {
                TrackPathModel(
                    getTrackPathRequest.trackUid,
                    trackPointsEntityList.map { trackPointEntityMapper.mapFromEntity(it) }
                )
            }
        }
    }

    override fun selectTracksWithPath(): Flow<List<TrackWithPathModel>> {
        // Get a flow for all tracks with points relationship.
        val tracksWithPointsFlow: Flow<List<TrackWithPoints>> = trackPointDao.selectTracksWithPoints()
        // Return a map of this flow, where (if track with points isn't null) we map the track entity to a track model, map the list of points their data equivalent,
        // or null if there are 0 points. Create a TrackWithPathModel from this combination, and return that finally.
        return tracksWithPointsFlow.map { tracksWithPoints ->
            tracksWithPoints.map { trackWithPoints ->
                // Map the track itself.
                val track: TrackModel = trackEntityMapper.mapFromEntity(trackWithPoints.track)
                // Map the points to model equivalents and create a track path model, or null if 0 points.
                val trackPathModel: TrackPathModel? = trackWithPoints.points?.let { trackPointEntities ->
                    if(trackPointEntities.isEmpty()) {
                        return@let null
                    } else {
                        return@let TrackPathModel(
                            track.trackUid,
                            trackPointEntities.map { trackPointEntity ->
                                trackPointEntityMapper.mapFromEntity(trackPointEntity)
                            }
                        )
                    }
                }
                // Return the track with path model.
                TrackWithPathModel(track, trackPathModel)
            }
        }
    }

    @Transaction
    override suspend fun upsertTrackPath(trackPathModel: TrackPathModel) {
        // TODO: we don't need to run this every time. For example, implement some checks to determine whether changes NEED to be made, prior to actually doing it.
        // TODO: for now, we'll just be deleting all track points from the track.
        // Delete all points for the track.
        trackPointDao.clearAllPointsFor(trackPathModel.trackUid)
        // Map all points to entities.
        val pointEntities: List<TrackPointEntity> = trackPathModel.points.map {
            trackPointEntityMapper.mapToEntity(it)
        }
        // Upsert all points.
        trackPointDao.upsert(pointEntities)
    }
}