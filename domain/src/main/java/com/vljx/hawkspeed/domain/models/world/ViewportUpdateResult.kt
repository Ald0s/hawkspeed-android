package com.vljx.hawkspeed.domain.models.world

import com.vljx.hawkspeed.domain.models.track.Track

data class ViewportUpdateResult(
    val tracks: List<Track>
)