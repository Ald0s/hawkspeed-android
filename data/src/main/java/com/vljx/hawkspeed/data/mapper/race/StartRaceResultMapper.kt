package com.vljx.hawkspeed.data.mapper.race

import com.vljx.hawkspeed.data.mapper.Mapper
import com.vljx.hawkspeed.data.models.race.StartRaceResultModel
import com.vljx.hawkspeed.domain.models.race.StartRaceResult
import javax.inject.Inject

class StartRaceResultMapper @Inject constructor(
    private val raceUpdateMapper: RaceUpdateMapper
): Mapper<StartRaceResultModel, StartRaceResult> {
    override fun mapFromData(model: StartRaceResultModel): StartRaceResult {
        return StartRaceResult(
            model.isStarted,
            model.race?.let { raceUpdateMapper.mapFromData(it) },
            model.errorCode
        )
    }

    override fun mapToData(domain: StartRaceResult): StartRaceResultModel {
        return StartRaceResultModel(
            domain.isStarted,
            domain.race?.let { raceUpdateMapper.mapToData(it) },
            domain.errorCode
        )
    }
}