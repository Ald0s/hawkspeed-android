package com.vljx.hawkspeed.data.mapper.world

import com.vljx.hawkspeed.data.mapper.Mapper
import com.vljx.hawkspeed.data.models.world.PlayerUpdateResultModel
import com.vljx.hawkspeed.domain.models.world.PlayerUpdateResult
import javax.inject.Inject

class PlayerUpdateResultMapper @Inject constructor(
    private val worldObjectUpdateResultMapper: WorldObjectUpdateResultMapper
): Mapper<PlayerUpdateResultModel, PlayerUpdateResult> {
    override fun mapFromData(model: PlayerUpdateResultModel): PlayerUpdateResult {
        return PlayerUpdateResult(
            model.latitude,
            model.longitude,
            model.bearing,
            model.worldObjectUpdateResult?.let { worldObjectUpdateResultMapper.mapFromData(it) }
        )
    }

    override fun mapToData(domain: PlayerUpdateResult): PlayerUpdateResultModel {
        return PlayerUpdateResultModel(
            domain.latitude,
            domain.longitude,
            domain.bearing,
            domain.worldObjectUpdateResult?.let { worldObjectUpdateResultMapper.mapToData(it) }
        )
    }
}