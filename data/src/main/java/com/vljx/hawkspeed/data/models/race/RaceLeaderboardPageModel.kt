package com.vljx.hawkspeed.data.models.race

import com.vljx.hawkspeed.data.models.track.TrackModel
import com.vljx.hawkspeed.domain.base.BasePaged

data class RaceLeaderboardPageModel(
    val trackModel: TrackModel,
    val raceOutcomes: List<RaceOutcomeModel>,
    override val thisPage: Int,
    override val nextPage: Int?
): BasePaged