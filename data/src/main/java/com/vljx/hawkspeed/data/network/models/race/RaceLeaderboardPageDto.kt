package com.vljx.hawkspeed.data.network.models.race

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.vljx.hawkspeed.data.network.models.track.TrackDto
import com.vljx.hawkspeed.domain.base.Paged

data class RaceLeaderboardPageDto(
    @Expose
    @SerializedName("track")
    val trackDto: TrackDto,

    @Expose
    @SerializedName("items")
    val raceOutcomes: List<RaceOutcomeDto>,

    @Expose
    @SerializedName("this_page")
    override val thisPage: Int,

    @Expose
    @SerializedName("next_page")
    override val nextPage: Int?
): Paged