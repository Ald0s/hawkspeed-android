package com.vljx.hawkspeed.domain.models.world

import com.vljx.hawkspeed.domain.models.track.Track

data class WorldObjectUpdateResult(
    val tracks: List<Track>
)