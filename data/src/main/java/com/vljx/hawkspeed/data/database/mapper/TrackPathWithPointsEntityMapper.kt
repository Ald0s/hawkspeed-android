package com.vljx.hawkspeed.data.database.mapper

import com.vljx.hawkspeed.data.database.entity.track.TrackPathEntity
import com.vljx.hawkspeed.data.database.entity.track.TrackPathWithPointsEntity
import com.vljx.hawkspeed.data.models.track.TrackPathWithPointsModel
import javax.inject.Inject

class TrackPathWithPointsEntityMapper @Inject constructor(
    private val trackPointEntityMapper: TrackPointEntityMapper
): EntityMapper<TrackPathWithPointsEntity, TrackPathWithPointsModel> {
    override fun mapFromEntity(entity: TrackPathWithPointsEntity): TrackPathWithPointsModel {
        return TrackPathWithPointsModel(
            entity.trackPath.trackPathUid,
            trackPointEntityMapper.mapFromEntityList(entity.trackPoints)
        )
    }

    override fun mapToEntity(model: TrackPathWithPointsModel): TrackPathWithPointsEntity {
        return TrackPathWithPointsEntity(
            TrackPathEntity(model.trackPathUid),
            trackPointEntityMapper.mapToEntityList(model.points)
        )
    }
}