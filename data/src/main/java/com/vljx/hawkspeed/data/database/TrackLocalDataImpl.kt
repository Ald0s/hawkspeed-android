package com.vljx.hawkspeed.data.database

import androidx.room.Transaction
import com.vljx.hawkspeed.data.database.dao.TrackDao
import com.vljx.hawkspeed.data.database.dao.TrackPointDao
import com.vljx.hawkspeed.data.database.entity.TrackEntity
import com.vljx.hawkspeed.data.database.entity.TrackPointEntity
import com.vljx.hawkspeed.data.database.mapper.TrackEntityMapper
import com.vljx.hawkspeed.data.database.mapper.TrackPointEntityMapper
import com.vljx.hawkspeed.data.models.track.TrackModel
import com.vljx.hawkspeed.data.models.track.TrackPathModel
import com.vljx.hawkspeed.data.source.TrackLocalData
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.requests.track.GetTrackRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TrackLocalDataImpl @Inject constructor(
    private val trackDao: TrackDao,

    private val trackEntityMapper: TrackEntityMapper,
): TrackLocalData {
    override fun selectTrack(getTrackRequest: GetTrackRequest): Flow<TrackModel?> {
        // Get a flow for the nullable track entity.
        val trackEntityFlow: Flow<TrackEntity?> = trackDao.selectTrackByUid(getTrackRequest.trackUid)
        // Return the flow also mapped to the track model.
        return trackEntityFlow.map { trackEntity ->
            trackEntity?.run {  trackEntityMapper.mapFromEntity(trackEntity) }
        }
    }

    override suspend fun upsertTrack(trackModel: TrackModel) {
        // Map the model to an entity.
        val trackEntity: TrackEntity = trackEntityMapper.mapToEntity(trackModel)
        // Upsert the track.
        trackDao.upsert(trackEntity)
    }
}