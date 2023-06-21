package com.vljx.hawkspeed.data

import com.vljx.hawkspeed.data.mapper.track.TrackMapper
import com.vljx.hawkspeed.data.source.TrackLocalData
import com.vljx.hawkspeed.data.source.TrackRemoteData
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.models.track.Track
import com.vljx.hawkspeed.domain.repository.TrackRepository
import com.vljx.hawkspeed.domain.requestmodels.track.RequestGetTrack
import com.vljx.hawkspeed.domain.requestmodels.track.RequestSubmitTrack
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TrackRepositoryImpl @Inject constructor(
    private val trackLocalData: TrackLocalData,
    private val trackRemoteData: TrackRemoteData,

    private val trackMapper: TrackMapper
): BaseRepository(), TrackRepository {
    override fun getTrack(getTrack: RequestGetTrack): Flow<Resource<Track>> =
        flowQueryFromCacheNetworkAndCache(
            trackMapper,
            databaseQuery = { trackLocalData.selectTrackByUid(getTrack.trackUid) },
            networkQuery = { trackRemoteData.queryTrack(getTrack) },
            cacheResult = { track ->
                trackLocalData.upsertTrack(track)
            }
        )

    override fun submitNewTrack(requestSubmitTrack: RequestSubmitTrack): Flow<Resource<Track>> =
        flowQueryNetworkAndCache(
            trackMapper,
            networkQuery = { trackRemoteData.createNewTrack(requestSubmitTrack) },
            cacheResult = { trackSummaryModel -> trackLocalData.upsertTrack(trackSummaryModel) }
        )
}