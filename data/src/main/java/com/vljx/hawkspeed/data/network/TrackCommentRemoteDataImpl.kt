package com.vljx.hawkspeed.data.network

import com.vljx.hawkspeed.data.models.track.TrackCommentsPageModel
import com.vljx.hawkspeed.data.network.api.TrackService
import com.vljx.hawkspeed.data.network.mapper.track.TrackCommentsPageDtoMapper
import com.vljx.hawkspeed.data.source.track.TrackCommentRemoteData
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.requestmodels.track.RequestPageTrackComments
import javax.inject.Inject

class TrackCommentRemoteDataImpl @Inject constructor(
    private val trackService: TrackService,

    private val trackCommentsPageDtoMapper: TrackCommentsPageDtoMapper
): BaseRemoteData(), TrackCommentRemoteData {
    override suspend fun queryCommentPage(
        requestPageTrackComments: RequestPageTrackComments,
        page: Int
    ): Resource<TrackCommentsPageModel> = getResult({
        trackService.queryCommentsPage(
            requestPageTrackComments.trackUid,
            page
        )
    }, trackCommentsPageDtoMapper)
}