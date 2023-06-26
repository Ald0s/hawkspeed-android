package com.vljx.hawkspeed.data.database.mapper

import com.vljx.hawkspeed.data.database.entity.track.TrackWithPathEntity
import com.vljx.hawkspeed.data.models.track.TrackWithPathModel
import javax.inject.Inject

class TrackWithPathEntityMapper @Inject constructor(
    private val trackEntityMapper: TrackEntityMapper,
    private val trackPathWithPointsEntityMapper: TrackPathWithPointsEntityMapper
): EntityMapper<TrackWithPathEntity, TrackWithPathModel> {
    override fun mapFromEntity(entity: TrackWithPathEntity): TrackWithPathModel {
        return TrackWithPathModel(
            trackEntityMapper.mapFromEntity(entity.track),
            entity.trackPathWithPoints?.let {
                trackPathWithPointsEntityMapper.mapFromEntity(it)
            }
        )
    }

    override fun mapToEntity(model: TrackWithPathModel): TrackWithPathEntity {
        return TrackWithPathEntity(
            trackEntityMapper.mapToEntity(model.track),
            model.trackPathWithPoints?.let { trackPathWithPointsEntityMapper.mapToEntity(it) }
        )
    }
}