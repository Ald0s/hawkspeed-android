package com.vljx.hawkspeed.data

import com.vljx.hawkspeed.data.mapper.track.TrackPathMapper
import com.vljx.hawkspeed.data.source.TrackPathLocalData
import com.vljx.hawkspeed.data.source.TrackPathRemoteData
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.models.track.TrackPath
import com.vljx.hawkspeed.domain.repository.TrackPathRepository
import com.vljx.hawkspeed.domain.requests.track.GetTrackPathRequest
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TrackPathRepositoryImpl @Inject constructor(
    private val trackPathLocalData: TrackPathLocalData,
    private val trackPathRemoteData: TrackPathRemoteData,

    private val trackPathMapper: TrackPathMapper
): BaseRepository(), TrackPathRepository {
    override fun getTrackPath(getTrackPathRequest: GetTrackPathRequest): Flow<Resource<TrackPath>> =
        queryWithCacheFlow(
            trackPathMapper,
            databaseQuery = { trackPathLocalData.selectTrackPath(getTrackPathRequest) },
            networkQuery = { trackPathRemoteData.queryTrackPath(getTrackPathRequest) },
            cacheResult = { trackPathModel -> trackPathLocalData.upsertTrackPath(trackPathModel) }
        )
}