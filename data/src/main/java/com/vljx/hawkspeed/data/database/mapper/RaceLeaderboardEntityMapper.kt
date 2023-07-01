package com.vljx.hawkspeed.data.database.mapper

import com.vljx.hawkspeed.data.database.entity.race.RaceLeaderboardEntity
import com.vljx.hawkspeed.data.models.race.RaceLeaderboardModel
import javax.inject.Inject

class RaceLeaderboardEntityMapper @Inject constructor(
    private val userEntityMapper: UserEntityMapper
): EntityMapper<RaceLeaderboardEntity, RaceLeaderboardModel> {
    override fun mapFromEntity(entity: RaceLeaderboardEntity): RaceLeaderboardModel {
        return RaceLeaderboardModel(
            entity.raceUid,
            entity.finishingPlace,
            entity.started,
            entity.finished,
            entity.stopwatch,
            userEntityMapper.mapFromEntity(entity.player),
            entity.trackUid
        )
    }

    override fun mapToEntity(model: RaceLeaderboardModel): RaceLeaderboardEntity {
        return RaceLeaderboardEntity(
            model.raceUid,
            model.finishingPlace,
            model.started,
            model.finished,
            model.stopwatch,
            userEntityMapper.mapToEntity(model.player),
            model.trackUid
        )
    }
}