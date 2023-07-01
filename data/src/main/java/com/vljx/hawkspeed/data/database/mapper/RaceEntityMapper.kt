package com.vljx.hawkspeed.data.database.mapper

import com.vljx.hawkspeed.data.database.entity.race.RaceEntity
import com.vljx.hawkspeed.data.models.race.RaceModel
import javax.inject.Inject

class RaceEntityMapper @Inject constructor(

): EntityMapper<RaceEntity, RaceModel> {
    override fun mapFromEntity(entity: RaceEntity): RaceModel {
        return RaceModel(
            entity.raceUid,
            entity.trackUid,
            entity.started,
            entity.finished,
            entity.isDisqualified,
            entity.disqualificationReason,
            entity.isCancelled
        )
    }

    override fun mapToEntity(model: RaceModel): RaceEntity {
        return RaceEntity(
            model.raceUid,
            model.trackUid,
            model.started,
            model.finished,
            model.isDisqualified,
            model.disqualificationReason,
            model.isCancelled
        )
    }
}