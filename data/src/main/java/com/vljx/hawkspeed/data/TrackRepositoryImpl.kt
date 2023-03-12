package com.vljx.hawkspeed.data

import com.vljx.hawkspeed.data.mapper.track.TrackMapper
import com.vljx.hawkspeed.data.source.TrackLocalData
import com.vljx.hawkspeed.data.source.TrackRemoteData
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.models.track.Track
import com.vljx.hawkspeed.domain.repository.TrackRepository
import com.vljx.hawkspeed.domain.requests.SubmitTrackRequest
import com.vljx.hawkspeed.domain.requests.track.GetTrackRequest
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TrackRepositoryImpl @Inject constructor(
    private val trackLocalData: TrackLocalData,
    private val trackRemoteData: TrackRemoteData,

    private val trackMapper: TrackMapper
): BaseRepository(), TrackRepository {
    override fun getTrack(getTrackRequest: GetTrackRequest): Flow<Resource<Track>> =
        queryWithCacheFlow(
            trackMapper,
            databaseQuery = { trackLocalData.selectTrack(getTrackRequest) },
            networkQuery = { trackRemoteData.queryTrack(getTrackRequest) },
            cacheResult = { trackModel -> trackLocalData.upsertTrack(trackModel) }
        )

    override suspend fun submitNewTrack(submitTrackRequest: SubmitTrackRequest): Flow<Resource<Track>> =
        queryAndCache(
            trackMapper,
            networkQuery = { trackRemoteData.createNewTrack(submitTrackRequest) },
            cacheResult = { trackSummaryModel -> trackLocalData.upsertTrack(trackSummaryModel) }
        )
}