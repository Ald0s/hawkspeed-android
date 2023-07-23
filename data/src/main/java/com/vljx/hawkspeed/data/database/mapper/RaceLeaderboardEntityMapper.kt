package com.vljx.hawkspeed.data.database.mapper

import com.vljx.hawkspeed.data.database.entity.race.RaceLeaderboardEntity
import com.vljx.hawkspeed.data.database.mapper.vehicle.VehicleEntityMapper
import com.vljx.hawkspeed.data.models.race.RaceLeaderboardModel
import javax.inject.Inject

class RaceLeaderboardEntityMapper @Inject constructor(
    private val vehicleEntityMapper: VehicleEntityMapper,
    private val userEntityMapper: UserEntityMapper
): EntityMapper<RaceLeaderboardEntity, RaceLeaderboardModel> {
    override fun mapFromEntity(entity: RaceLeaderboardEntity): RaceLeaderboardModel {
        return RaceLeaderboardModel(
            entity.raceUid,
            entity.finishingPlace,
            entity.started,
            entity.finished,
            entity.stopwatch,
            entity.averageSpeed,
            entity.percentMissed,
            userEntityMapper.mapFromEntity(entity.player),
            vehicleEntityMapper.mapFromEntity(entity.vehicle),
            entity.trackUid,
            entity.trackName,
            entity.trackType
        )
    }

    override fun mapToEntity(model: RaceLeaderboardModel): RaceLeaderboardEntity {
        return RaceLeaderboardEntity(
            model.raceUid,
            model.finishingPlace,
            model.started,
            model.finished,
            model.stopwatch,
            model.averageSpeed,
            model.percentMissed,
            userEntityMapper.mapToEntity(model.player),
            vehicleEntityMapper.mapToEntity(model.vehicle),
            model.trackUid,
            model.trackName,
            model.trackType
        )
    }
}