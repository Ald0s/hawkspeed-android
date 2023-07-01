package com.vljx.hawkspeed.data.mapper.race

import com.vljx.hawkspeed.data.mapper.Mapper
import com.vljx.hawkspeed.data.mapper.user.UserMapper
import com.vljx.hawkspeed.data.models.race.RaceLeaderboardModel
import com.vljx.hawkspeed.domain.models.race.RaceLeaderboard
import javax.inject.Inject

class RaceLeaderboardMapper @Inject constructor(
    private val userMapper: UserMapper
): Mapper<RaceLeaderboardModel, RaceLeaderboard> {
    override fun mapFromData(model: RaceLeaderboardModel): RaceLeaderboard {
        return RaceLeaderboard(
            model.raceUid,
            model.finishingPlace,
            model.started,
            model.finished,
            model.stopwatch,
            userMapper.mapFromData(model.player),
            model.trackUid
        )
    }

    override fun mapToData(domain: RaceLeaderboard): RaceLeaderboardModel {
        return RaceLeaderboardModel(
            domain.raceUid,
            domain.finishingPlace,
            domain.started,
            domain.finished,
            domain.stopwatch,
            userMapper.mapToData(domain.player),
            domain.trackUid
        )
    }
}