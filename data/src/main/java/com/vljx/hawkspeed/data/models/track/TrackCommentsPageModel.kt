package com.vljx.hawkspeed.data.models.track

import com.vljx.hawkspeed.data.models.comment.CommentModel
import com.vljx.hawkspeed.domain.base.Paged

data class TrackCommentsPageModel(
    val track: TrackModel,
    val comments: List<CommentModel>,
    override val thisPage: Int,
    override val nextPage: Int?
): Paged