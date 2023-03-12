package com.vljx.hawkspeed.data.database

import androidx.room.Transaction
import com.vljx.hawkspeed.data.database.dao.TrackPointDao
import com.vljx.hawkspeed.data.database.entity.TrackPointEntity
import com.vljx.hawkspeed.data.database.mapper.TrackPointEntityMapper
import com.vljx.hawkspeed.data.models.track.TrackPathModel
import com.vljx.hawkspeed.data.source.TrackPathLocalData
import com.vljx.hawkspeed.domain.requests.track.GetTrackPathRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TrackPathLocalDataImpl @Inject constructor(
    private val trackPointDao: TrackPointDao,

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