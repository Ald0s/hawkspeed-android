package com.vljx.hawkspeed.data.mapper.track

import com.vljx.hawkspeed.data.mapper.Mapper
import com.vljx.hawkspeed.data.models.track.TrackPointModel
import com.vljx.hawkspeed.domain.models.track.TrackPoint
import javax.inject.Inject

class TrackPointMapper @Inject constructor(

): Mapper<TrackPointModel, TrackPoint> {
    override fun mapFromData(model: TrackPointModel): TrackPoint {
        return TrackPoint(
            model.latitude,
            model.longitude,
            model.trackUid
        )
    }

    override fun mapToData(domain: TrackPoint): TrackPointModel {
        return TrackPointModel(
            domain.latitude,
            domain.longitude,
            domain.trackUid
        )
    }
}