package com.vljx.hawkspeed.data.database

import com.vljx.hawkspeed.data.database.dao.TrackDao
import com.vljx.hawkspeed.data.database.entity.TrackEntity
import com.vljx.hawkspeed.data.database.mapper.TrackEntityMapper
import com.vljx.hawkspeed.data.models.track.TrackModel
import com.vljx.hawkspeed.data.source.TrackLocalData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TrackLocalDataImpl @Inject constructor(
    private val trackDao: TrackDao,

    private val trackEntityMapper: TrackEntityMapper,
): TrackLocalData {
    override fun selectTrackByUid(trackUid: String): Flow<TrackModel?> =
        trackDao.selectTrackByUid(trackUid)
            .map { value: TrackEntity? ->
                value?.let { trackEntityMapper.mapFromEntity(it) }
            }

    override suspend fun upsertTrack(track: TrackModel) {
        // Map the model to an entity.
        val trackEntity: TrackEntity = trackEntityMapper.mapToEntity(track)
        // Upsert the track.
        trackDao.upsert(trackEntity)
    }

    override suspend fun upsertTracks(tracks: List<TrackModel>) {
        // Map all models to entities.
        val trackEntities: List<TrackEntity> = tracks.map { trackEntityMapper.mapToEntity(it) }
        // Upsert all tracks.
        trackDao.upsert(trackEntities)
    }
}