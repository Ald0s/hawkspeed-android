package com.vljx.hawkspeed.data.models.race

import com.vljx.hawkspeed.data.models.track.TrackModel
import com.vljx.hawkspeed.domain.base.Paged

data class RaceLeaderboardPageModel(
    val trackModel: TrackModel,
    val raceOutcomes: List<RaceLeaderboardModel>,
    override val thisPage: Int,
    override val nextPage: Int?
): Paged