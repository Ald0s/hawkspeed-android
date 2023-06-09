package com.vljx.hawkspeed.data

import com.vljx.hawkspeed.data.mapper.track.TrackWithPathMapper
import com.vljx.hawkspeed.data.source.track.TrackPathLocalData
import com.vljx.hawkspeed.data.source.track.TrackPathRemoteData
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.models.track.Track
import com.vljx.hawkspeed.domain.models.track.TrackWithPath
import com.vljx.hawkspeed.domain.repository.TrackPathRepository
import com.vljx.hawkspeed.domain.requestmodels.track.RequestDeleteTrackAndPath
import com.vljx.hawkspeed.domain.requestmodels.track.RequestGetTrackWithPath
import com.vljx.hawkspeed.domain.requestmodels.track.RequestSubmitTrack
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TrackPathRepositoryImpl @Inject constructor(
    private val trackPathLocalData: TrackPathLocalData,
    private val trackPathRemoteData: TrackPathRemoteData,

    private val trackWithPathMapper: TrackWithPathMapper
): BaseRepository(), TrackPathRepository {
    override fun getTrackWithPath(requestGetTrackWithPath: RequestGetTrackWithPath): Flow<Resource<TrackWithPath>> =
        flowQueryFromCacheNetworkAndCache(
            trackWithPathMapper,
            databaseQuery = { trackPathLocalData.selectTrackWithPath(requestGetTrackWithPath) },
            networkQuery = { trackPathRemoteData.queryTrackWithPath(requestGetTrackWithPath) },
            cacheResult = { trackWithPath ->
                trackPathLocalData.upsertTrackWithPath(trackWithPath)
            }
        )

    override fun submitNewTrack(requestSubmitTrack: RequestSubmitTrack): Flow<Resource<TrackWithPath>> =
        flowQueryNetworkAndCache(
            trackWithPathMapper,
            networkQuery = { trackPathRemoteData.createNewTrack(requestSubmitTrack) },
            cacheResult = { trackSummaryModel -> trackPathLocalData.upsertTrackWithPath(trackSummaryModel) }
        )

    override fun getTracksWithPathsFromCache(): Flow<List<TrackWithPath>> =
        trackPathLocalData.selectTracksWithPaths()
            .map {
                trackWithPathMapper.mapFromDataList(it)
            }

    override suspend fun deleteTrackAndPath(requestDeleteTrackAndPath: RequestDeleteTrackAndPath) =
        trackPathLocalData.deleteTrackAndPath(requestDeleteTrackAndPath)
}