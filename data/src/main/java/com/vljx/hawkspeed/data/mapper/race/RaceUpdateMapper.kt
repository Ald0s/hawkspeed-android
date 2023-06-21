package com.vljx.hawkspeed.data.mapper.race

import com.vljx.hawkspeed.data.mapper.Mapper
import com.vljx.hawkspeed.data.models.race.RaceUpdateModel
import com.vljx.hawkspeed.domain.models.world.RaceUpdate
import javax.inject.Inject

class RaceUpdateMapper @Inject constructor(

): Mapper<RaceUpdateModel, RaceUpdate> {
    override fun mapFromData(model: RaceUpdateModel): RaceUpdate {
        return RaceUpdate(
            model.raceUid,
            model.trackUid,
            model.started,
            model.finished,
            model.isDisqualified,
            model.disqualificationReason,
            model.isCancelled
        )
    }

    override fun mapToData(domain: RaceUpdate): RaceUpdateModel {
        return RaceUpdateModel(
            domain.raceUid,
            domain.trackUid,
            domain.started,
            domain.finished,
            domain.isDisqualified,
            domain.disqualificationReason,
            domain.isCancelled
        )
    }
}