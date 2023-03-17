package com.vljx.hawkspeed.data.database.mapper

import com.vljx.hawkspeed.data.database.entity.RaceOutcomeEntity
import com.vljx.hawkspeed.data.models.race.RaceOutcomeModel
import javax.inject.Inject

class RaceOutcomeEntityMapper @Inject constructor(
    private val userEntityMapper: UserEntityMapper
): EntityMapper<RaceOutcomeEntity, RaceOutcomeModel> {
    override fun mapFromEntity(entity: RaceOutcomeEntity): RaceOutcomeModel {
        return RaceOutcomeModel(
            entity.raceUid,
            entity.started,
            entity.finished,
            entity.stopwatch,
            userEntityMapper.mapFromEntity(entity.player),
            entity.trackUid
        )
    }

    override fun mapToEntity(model: RaceOutcomeModel): RaceOutcomeEntity {
        return RaceOutcomeEntity(
            model.raceUid,
            model.started,
            model.finished,
            model.stopwatch,
            userEntityMapper.mapToEntity(model.player),
            model.trackUid
        )
    }
}