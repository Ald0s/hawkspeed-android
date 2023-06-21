package com.vljx.hawkspeed.data.mapper.world

import com.vljx.hawkspeed.data.mapper.Mapper
import com.vljx.hawkspeed.data.mapper.track.TrackMapper
import com.vljx.hawkspeed.data.models.world.WorldObjectUpdateResultModel
import com.vljx.hawkspeed.domain.models.world.WorldObjectUpdateResult
import javax.inject.Inject

class WorldObjectUpdateResultMapper @Inject constructor(
    private val trackMapper: TrackMapper
): Mapper<WorldObjectUpdateResultModel, WorldObjectUpdateResult> {
    override fun mapFromData(model: WorldObjectUpdateResultModel): WorldObjectUpdateResult {
        return WorldObjectUpdateResult(
            trackMapper.mapFromDataList(model.tracks)
        )
    }

    override fun mapToData(domain: WorldObjectUpdateResult): WorldObjectUpdateResultModel {
        return WorldObjectUpdateResultModel(
            trackMapper.mapToDataList(domain.tracks)
        )
    }
}