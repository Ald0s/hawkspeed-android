package com.vljx.hawkspeed.data.mapper.track

import com.vljx.hawkspeed.data.mapper.Mapper
import com.vljx.hawkspeed.data.models.track.TrackWithPathModel
import com.vljx.hawkspeed.domain.models.track.TrackWithPath
import javax.inject.Inject

class TrackWithPathMapper @Inject constructor(
    private val trackMapper: TrackMapper,
    private val trackPathWithPointsMapper: TrackPathWithPointsMapper
): Mapper<TrackWithPathModel, TrackWithPath> {
    override fun mapFromData(model: TrackWithPathModel): TrackWithPath {
        return TrackWithPath(
            trackMapper.mapFromData(model.track),
            model.trackPathWithPoints?.let { trackPathWithPointsMapper.mapFromData(it) }
        )
    }

    override fun mapToData(domain: TrackWithPath): TrackWithPathModel {
        return TrackWithPathModel(
            trackMapper.mapToData(domain.track),
            domain.path?.let { trackPathWithPointsMapper.mapToData(it) }
        )
    }
}