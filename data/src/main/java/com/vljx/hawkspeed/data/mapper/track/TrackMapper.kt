package com.vljx.hawkspeed.data.mapper.track

import com.vljx.hawkspeed.data.mapper.Mapper
import com.vljx.hawkspeed.data.mapper.user.UserMapper
import com.vljx.hawkspeed.data.models.track.TrackModel
import com.vljx.hawkspeed.domain.models.track.Track
import javax.inject.Inject

class TrackMapper @Inject constructor(
    private val trackPointMapper: TrackPointMapper,
    private val userMapper: UserMapper
): Mapper<TrackModel, Track> {
    override fun mapFromData(model: TrackModel): Track {
        return Track(
            model.trackUid,
            model.name,
            model.description,
            userMapper.mapFromData(model.owner),
            trackPointMapper.mapFromData(model.startPoint),
            model.isVerified,
            model.canRace,
            model.canEdit,
            model.canDelete
            //model.points.map { trackPointMapper.mapFromData(it) }
        )
    }

    override fun mapToData(domain: Track): TrackModel {
        return TrackModel(
            domain.trackUid,
            domain.name,
            domain.description,
            userMapper.mapToData(domain.owner),
            trackPointMapper.mapToData(domain.startPoint),
            domain.isVerified,
            domain.canRace,
            domain.canEdit,
            domain.canDelete,
            //domain.points.map { trackPointMapper.mapToData(it) }
        )
    }
}