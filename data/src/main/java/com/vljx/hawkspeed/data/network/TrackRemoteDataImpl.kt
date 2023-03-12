package com.vljx.hawkspeed.data.network

import com.vljx.hawkspeed.data.models.track.TrackModel
import com.vljx.hawkspeed.data.network.api.TrackService
import com.vljx.hawkspeed.data.network.mapper.track.TrackDtoMapper
import com.vljx.hawkspeed.data.network.requests.track.SubmitTrackRequestDto
import com.vljx.hawkspeed.data.source.TrackRemoteData
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.requests.SubmitTrackRequest
import com.vljx.hawkspeed.domain.requests.track.GetTrackRequest
import javax.inject.Inject

class TrackRemoteDataImpl @Inject constructor(
    private val trackService: TrackService,

    private val trackDtoMapper: TrackDtoMapper
): BaseRemoteData(), TrackRemoteData {
    override suspend fun queryTrack(getTrackRequest: GetTrackRequest): Resource<TrackModel> = getResult({
        trackService.queryTrackByUid(getTrackRequest.trackUid)
    }, trackDtoMapper)

    override suspend fun createNewTrack(submitTrackRequest: SubmitTrackRequest): Resource<TrackModel> = getResult({
        trackService.submitNewTrack(
            SubmitTrackRequestDto(
                submitTrackRequest
            )
        )
    }, trackDtoMapper)
}