package com.vljx.hawkspeed.data.mapper.race

import com.vljx.hawkspeed.data.mapper.Mapper
import com.vljx.hawkspeed.data.models.race.CancelRaceResultModel
import com.vljx.hawkspeed.domain.models.race.CancelRaceResult
import javax.inject.Inject

class CancelRaceResultMapper @Inject constructor(
    private val raceUpdateMapper: RaceUpdateMapper
): Mapper<CancelRaceResultModel, CancelRaceResult> {
    override fun mapFromData(model: CancelRaceResultModel): CancelRaceResult {
        return CancelRaceResult(
            model.race?.let { raceUpdateMapper.mapFromData(it) },
            model.reasonCode
        )
    }

    override fun mapToData(domain: CancelRaceResult): CancelRaceResultModel {
        return CancelRaceResultModel(
            domain.race?.let { raceUpdateMapper.mapToData(it) },
            domain.reasonCode
        )
    }
}