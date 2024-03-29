package com.vljx.hawkspeed.data.models.track

import com.vljx.hawkspeed.domain.base.Paged

data class TrackCommentsPageModel(
    val track: TrackModel,
    val comments: List<TrackCommentModel>,
    override val thisPage: Int,
    override val nextPage: Int?
): Paged