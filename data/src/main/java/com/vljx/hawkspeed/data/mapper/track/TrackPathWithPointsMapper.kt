package com.vljx.hawkspeed.data.mapper.track

import com.vljx.hawkspeed.data.mapper.Mapper
import com.vljx.hawkspeed.data.models.track.TrackPathWithPointsModel
import com.vljx.hawkspeed.domain.models.track.TrackPath
import javax.inject.Inject

class TrackPathWithPointsMapper @Inject constructor(
    private val trackPointMapper: TrackPointMapper
): Mapper<TrackPathWithPointsModel, TrackPath> {
    override fun mapFromData(model: TrackPathWithPointsModel): TrackPath {
        return TrackPath(
            model.trackPathUid,
            trackPointMapper.mapFromDataList(model.points)
        )
    }

    override fun mapToData(domain: TrackPath): TrackPathWithPointsModel {
        return TrackPathWithPointsModel(
            domain.trackPathUid,
            trackPointMapper.mapToDataList(domain.points)
        )
    }
}