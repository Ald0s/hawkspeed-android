package com.vljx.hawkspeed.data.network

import com.vljx.hawkspeed.data.models.track.TrackModel
import com.vljx.hawkspeed.data.network.api.TrackService
import com.vljx.hawkspeed.data.network.mapper.track.TrackDtoMapper
import com.vljx.hawkspeed.data.network.requestmodels.track.SubmitTrackRequestDto
import com.vljx.hawkspeed.data.source.TrackRemoteData
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.requestmodels.track.RequestSubmitTrack
import com.vljx.hawkspeed.domain.requestmodels.track.RequestGetTrack
import javax.inject.Inject

class TrackRemoteDataImpl @Inject constructor(
    private val trackService: TrackService,

    private val trackDtoMapper: TrackDtoMapper
): BaseRemoteData(), TrackRemoteData {
    override suspend fun queryTrack(requestGetTrack: RequestGetTrack): Resource<TrackModel> = getResult({
        trackService.queryTrackByUid(requestGetTrack.trackUid)
    }, trackDtoMapper)

    override suspend fun createNewTrack(requestSubmitTrack: RequestSubmitTrack): Resource<TrackModel> = getResult({
        trackService.submitNewTrack(
            SubmitTrackRequestDto(
                requestSubmitTrack
            )
        )
    }, trackDtoMapper)
}