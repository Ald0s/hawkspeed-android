package com.vljx.hawkspeed.data.mapper.world

import com.vljx.hawkspeed.data.mapper.Mapper
import com.vljx.hawkspeed.data.mapper.track.TrackMapper
import com.vljx.hawkspeed.data.models.world.ViewportUpdateResultModel
import com.vljx.hawkspeed.domain.models.world.ViewportUpdateResult
import javax.inject.Inject

class ViewportUpdateResultMapper @Inject constructor(
    private val trackMapper: TrackMapper
): Mapper<ViewportUpdateResultModel, ViewportUpdateResult> {
    override fun mapFromData(model: ViewportUpdateResultModel): ViewportUpdateResult {
        return ViewportUpdateResult(
            trackMapper.mapFromDataList(model.tracks)
        )
    }

    override fun mapToData(domain: ViewportUpdateResult): ViewportUpdateResultModel {
        return ViewportUpdateResultModel(
            trackMapper.mapToDataList(domain.tracks)
        )
    }
}