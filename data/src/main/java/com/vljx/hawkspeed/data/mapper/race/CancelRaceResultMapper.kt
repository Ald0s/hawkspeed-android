package com.vljx.hawkspeed.data.mapper.race

import com.vljx.hawkspeed.data.mapper.Mapper
import com.vljx.hawkspeed.data.models.race.CancelRaceResultModel
import com.vljx.hawkspeed.domain.models.race.CancelRaceResult
import javax.inject.Inject

class CancelRaceResultMapper @Inject constructor(
    private val raceMapper: RaceMapper
): Mapper<CancelRaceResultModel, CancelRaceResult> {
    override fun mapFromData(model: CancelRaceResultModel): CancelRaceResult {
        return CancelRaceResult(
            model.race?.let { raceMapper.mapFromData(it) },
            model.cancellationReason
        )
    }

    override fun mapToData(domain: CancelRaceResult): CancelRaceResultModel {
        return CancelRaceResultModel(
            domain.race?.let { raceMapper.mapToData(it) },
            domain.cancellationReason
        )
    }
}