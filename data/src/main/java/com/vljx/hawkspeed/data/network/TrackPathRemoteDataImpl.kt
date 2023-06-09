package com.vljx.hawkspeed.data.network

import com.vljx.hawkspeed.data.models.track.TrackModel
import com.vljx.hawkspeed.data.models.track.TrackWithPathModel
import com.vljx.hawkspeed.data.network.api.TrackService
import com.vljx.hawkspeed.data.network.mapper.track.TrackWithPathDtoMapper
import com.vljx.hawkspeed.data.network.requestmodels.track.RequestSubmitTrackDto
import com.vljx.hawkspeed.data.source.track.TrackPathRemoteData
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.requestmodels.track.RequestGetTrackWithPath
import com.vljx.hawkspeed.domain.requestmodels.track.RequestSubmitTrack
import javax.inject.Inject

class TrackPathRemoteDataImpl @Inject constructor(
    private val trackService: TrackService,

    private val trackWithPathDtoMapper: TrackWithPathDtoMapper
): BaseRemoteData(), TrackPathRemoteData {
    override suspend fun queryTrackWithPath(requestGetTrackWithPath: RequestGetTrackWithPath): Resource<TrackWithPathModel> = getResult({
        trackService.queryTrackWithPathByUid(
            requestGetTrackWithPath.trackUid
        )
    }, trackWithPathDtoMapper)

    override suspend fun createNewTrack(requestSubmitTrack: RequestSubmitTrack): Resource<TrackWithPathModel> = getResult({
        trackService.submitNewTrack(
            RequestSubmitTrackDto(
                requestSubmitTrack
            )
        )
    }, trackWithPathDtoMapper)
}