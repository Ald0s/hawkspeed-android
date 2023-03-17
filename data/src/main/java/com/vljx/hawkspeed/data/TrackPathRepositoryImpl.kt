package com.vljx.hawkspeed.data

import com.vljx.hawkspeed.data.mapper.track.TrackPathMapper
import com.vljx.hawkspeed.data.mapper.track.TrackWithPathMapper
import com.vljx.hawkspeed.data.source.TrackPathLocalData
import com.vljx.hawkspeed.data.source.TrackPathRemoteData
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.models.track.TrackPath
import com.vljx.hawkspeed.domain.models.track.TrackWithPath
import com.vljx.hawkspeed.domain.repository.TrackPathRepository
import com.vljx.hawkspeed.domain.requests.track.GetTrackPathRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TrackPathRepositoryImpl @Inject constructor(
    private val trackPathLocalData: TrackPathLocalData,
    private val trackPathRemoteData: TrackPathRemoteData,

    private val trackPathMapper: TrackPathMapper,
    private val trackWithPathMapper: TrackWithPathMapper
): BaseRepository(), TrackPathRepository {
    override fun getTrackPath(getTrackPathRequest: GetTrackPathRequest): Flow<Resource<TrackPath>> =
        queryWithCacheFlow(
            trackPathMapper,
            databaseQuery = { trackPathLocalData.selectTrackPath(getTrackPathRequest) },
            networkQuery = { trackPathRemoteData.queryTrackPath(getTrackPathRequest) },
            cacheResult = { trackPathModel -> trackPathLocalData.upsertTrackPath(trackPathModel) }
        )

    override fun getTracksWithPaths(): Flow<List<TrackWithPath>> =
        trackPathLocalData.selectTracksWithPath()
            .map { tracksWithPathModels ->
                tracksWithPathModels.map { trackWithPathModel ->
                    trackWithPathMapper.mapFromData(trackWithPathModel)
                }
            }
}