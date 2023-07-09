package com.vljx.hawkspeed.data.mapper.race

import com.vljx.hawkspeed.data.mapper.Mapper
import com.vljx.hawkspeed.data.models.race.RaceModel
import com.vljx.hawkspeed.domain.models.race.Race
import javax.inject.Inject

class RaceMapper @Inject constructor(

): Mapper<RaceModel, Race> {
    override fun mapFromData(model: RaceModel): Race {
        return Race(
            model.raceUid,
            model.trackUid,
            model.started,
            model.finished,
            model.isDisqualified,
            model.disqualificationReason,
            model.isCancelled,
            model.averageSpeed,
            model.numLapsComplete,
            model.percentComplete
        )
    }

    override fun mapToData(domain: Race): RaceModel {
        return RaceModel(
            domain.raceUid,
            domain.trackUid,
            domain.started,
            domain.finished,
            domain.isDisqualified,
            domain.disqualificationReason,
            domain.isCancelled,
            domain.averageSpeed,
            domain.numLapsComplete,
            domain.percentComplete
        )
    }
}