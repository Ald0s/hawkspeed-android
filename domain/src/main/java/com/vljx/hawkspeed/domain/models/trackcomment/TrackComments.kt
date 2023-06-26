package com.vljx.hawkspeed.domain.models.trackcomment

import com.vljx.hawkspeed.domain.models.comment.Comment
import com.vljx.hawkspeed.domain.models.track.Track

data class TrackComments(
    val track: Track,
    val comments: List<Comment>
)