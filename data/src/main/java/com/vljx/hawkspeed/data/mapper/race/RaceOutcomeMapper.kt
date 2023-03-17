package com.vljx.hawkspeed.data.mapper.race

import com.vljx.hawkspeed.data.mapper.Mapper
import com.vljx.hawkspeed.data.mapper.user.UserMapper
import com.vljx.hawkspeed.data.models.race.RaceOutcomeModel
import com.vljx.hawkspeed.domain.models.race.RaceOutcome
import javax.inject.Inject

class RaceOutcomeMapper @Inject constructor(
    private val userMapper: UserMapper
): Mapper<RaceOutcomeModel, RaceOutcome> {
    override fun mapFromData(model: RaceOutcomeModel): RaceOutcome {
        return RaceOutcome(
            model.raceUid,
            model.started,
            model.finished,
            model.stopwatch,
            userMapper.mapFromData(model.player),
            model.trackUid
        )
    }

    override fun mapToData(domain: RaceOutcome): RaceOutcomeModel {
        return RaceOutcomeModel(
            domain.raceUid,
            domain.started,
            domain.finished,
            domain.stopwatch,
            userMapper.mapToData(domain.player),
            domain.trackUid
        )
    }
}