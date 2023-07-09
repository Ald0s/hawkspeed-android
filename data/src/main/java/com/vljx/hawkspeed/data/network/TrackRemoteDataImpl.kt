package com.vljx.hawkspeed.data.network

import com.vljx.hawkspeed.data.models.track.TrackModel
import com.vljx.hawkspeed.data.network.api.TrackService
import com.vljx.hawkspeed.data.network.mapper.track.TrackDtoMapper
import com.vljx.hawkspeed.data.network.requestmodels.track.RequestSetTrackRatingDto
import com.vljx.hawkspeed.data.source.track.TrackRemoteData
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.requestmodels.track.RequestClearTrackRating
import com.vljx.hawkspeed.domain.requestmodels.track.RequestGetTrack
import com.vljx.hawkspeed.domain.requestmodels.track.RequestSetTrackRating
import javax.inject.Inject

class TrackRemoteDataImpl @Inject constructor(
    private val trackService: TrackService,

    private val trackDtoMapper: TrackDtoMapper
): BaseRemoteData(), TrackRemoteData {
    override suspend fun queryTrack(requestGetTrack: RequestGetTrack): Resource<TrackModel> = getResult({
        trackService.queryTrackByUid(requestGetTrack.trackUid)
    }, trackDtoMapper)

    override suspend fun setTrackRating(requestSetTrackRating: RequestSetTrackRating): Resource<TrackModel> = getResult({
        trackService.setTrackRating(
            requestSetTrackRating.trackUid,
            RequestSetTrackRatingDto(
                requestSetTrackRating.rating
            )
        )
    }, trackDtoMapper)

    override suspend fun clearTrackRating(requestClearTrackRating: RequestClearTrackRating): Resource<TrackModel> = getResult({
        trackService.clearTrackRating(requestClearTrackRating.trackUid)
    }, trackDtoMapper)
}