package com.vljx.hawkspeed.data.mapper.race

import com.vljx.hawkspeed.data.mapper.Mapper
import com.vljx.hawkspeed.data.mapper.SocketErrorWrapperMapper
import com.vljx.hawkspeed.data.models.SocketErrorWrapperModel
import com.vljx.hawkspeed.data.models.race.StartRaceResultModel
import com.vljx.hawkspeed.domain.ResourceError
import com.vljx.hawkspeed.domain.models.race.StartRaceResult
import javax.inject.Inject

class StartRaceResultMapper @Inject constructor(
    private val raceMapper: RaceMapper,
    private val socketErrorWrapperMapper: SocketErrorWrapperMapper
): Mapper<StartRaceResultModel, StartRaceResult> {
    override fun mapFromData(model: StartRaceResultModel): StartRaceResult {
        return StartRaceResult(
            model.isStarted,
            model.race?.let { raceMapper.mapFromData(it) },
            model.error?.let { socketErrorWrapperMapper.mapFromData(it) }
        )
    }

    override fun mapToData(domain: StartRaceResult): StartRaceResultModel {
        return StartRaceResultModel(
            domain.isStarted,
            domain.race?.let { raceMapper.mapToData(it) },
            domain.socketError?.let { socketErrorWrapperMapper.mapToData(it) }
        )
    }
}