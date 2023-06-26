package com.vljx.hawkspeed.data.database

import androidx.room.Transaction
import com.vljx.hawkspeed.data.database.dao.TrackDao
import com.vljx.hawkspeed.data.database.dao.TrackPathDao
import com.vljx.hawkspeed.data.database.dao.TrackPointDao
import com.vljx.hawkspeed.data.database.entity.track.TrackWithPathEntity
import com.vljx.hawkspeed.data.database.mapper.TrackWithPathEntityMapper
import com.vljx.hawkspeed.data.models.track.TrackWithPathModel
import com.vljx.hawkspeed.data.source.track.TrackPathLocalData
import com.vljx.hawkspeed.domain.requestmodels.track.RequestDeleteTrackAndPath
import com.vljx.hawkspeed.domain.requestmodels.track.RequestGetTrackWithPath
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TrackPathLocalDataImpl @Inject constructor(
    private val trackDao: TrackDao,
    private val trackPathDao: TrackPathDao,
    private val trackPointDao: TrackPointDao,

    private val trackWithPathEntityMapper: TrackWithPathEntityMapper
): TrackPathLocalData {
    override fun selectTrackWithPath(requestGetTrackWithPath: RequestGetTrackWithPath): Flow<TrackWithPathModel?> {
        val trackWithPathEntityFlow: Flow<TrackWithPathEntity?> = trackPathDao.selectTrackWithPath(
            requestGetTrackWithPath.trackUid
        )
        return trackWithPathEntityFlow.map { value: TrackWithPathEntity? ->
            value?.let { trackWithPathEntityMapper.mapFromEntity(value) }
        }
    }

    override fun selectTracksWithPaths(): Flow<List<TrackWithPathModel>> =
        trackPathDao.selectTracksWithPaths()
            .map { value: List<TrackWithPathEntity> ->
                trackWithPathEntityMapper.mapFromEntityList(value)
            }

    @Transaction
    override suspend fun upsertTrackWithPath(trackWithPath: TrackWithPathModel) {
        // Map to entity equivalent.
        val trackWithPathEntity: TrackWithPathEntity = trackWithPathEntityMapper.mapToEntity(trackWithPath)
        // Now, first insert all track points, then track path, then finally the track itself.
        trackWithPathEntity.trackPathWithPoints?.let { trackPathWithPoints ->
            // TODO: improve this. For now, whenever we insert a new track path, we'll do so by clearing all points currently associated with that path, then inserting the latest.
            // TODO: this may be inefficient, so discover a better way of handling this.
            // Clear all existing points.
            trackPointDao.clearAllPointsFor(trackPathWithPoints.trackPath.trackPathUid)
            // Insert all points.
            trackPointDao.insert(trackPathWithPoints.trackPoints)
            // Upsert the track path.
            trackPathDao.upsert(trackPathWithPoints.trackPath)
        }
        // Upsert the track.
        trackDao.upsert(trackWithPathEntity.track)
    }

    override suspend fun deleteTrackAndPath(requestDeleteTrackAndPath: RequestDeleteTrackAndPath) {
        // First clear all points.
        trackPointDao.clearAllPointsFor(requestDeleteTrackAndPath.trackUid)
        // Then, clear the track's path.
        trackPathDao.deletePathByUid(requestDeleteTrackAndPath.trackUid)
        // Finally, delete the track itself.
        trackDao.deleteByUid(requestDeleteTrackAndPath.trackUid)
    }
}