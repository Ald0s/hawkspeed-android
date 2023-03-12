package com.vljx.hawkspeed.data.network

import com.vljx.hawkspeed.data.models.track.TrackPathModel
import com.vljx.hawkspeed.data.network.api.TrackService
import com.vljx.hawkspeed.data.network.mapper.track.TrackPathDtoMapper
import com.vljx.hawkspeed.data.source.TrackPathRemoteData
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.requests.track.GetTrackPathRequest
import javax.inject.Inject

class TrackPathRemoteDataImpl @Inject constructor(
    private val trackService: TrackService,

    private val trackPathDtoMapper: TrackPathDtoMapper
): BaseRemoteData(), TrackPathRemoteData {
    override suspend fun queryTrackPath(getTrackPathRequest: GetTrackPathRequest): Resource<TrackPathModel> = getResult({
        trackService.queryTrackPathByUid(getTrackPathRequest.trackUid)
    }, trackPathDtoMapper)
}