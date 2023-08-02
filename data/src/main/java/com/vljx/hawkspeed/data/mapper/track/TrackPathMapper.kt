package com.vljx.hawkspeed.data.mapper.track

import com.vljx.hawkspeed.data.mapper.Mapper
import com.vljx.hawkspeed.data.models.track.TrackPathModel
import com.vljx.hawkspeed.domain.models.track.TrackPath
import javax.inject.Inject

class TrackPathMapper @Inject constructor(
    private val trackPointMapper: TrackPointMapper
): Mapper<TrackPathModel, TrackPath> {
    override fun mapFromData(model: TrackPathModel): TrackPath {
        return TrackPath(
            model.trackUid,
            model.hash,
            trackPointMapper.mapFromDataList(model.points)
        )
    }

    override fun mapToData(domain: TrackPath): TrackPathModel {
        return TrackPathModel(
            domain.trackPathUid,
            domain.hash,
            trackPointMapper.mapToDataList(domain.points)
        )
    }
}