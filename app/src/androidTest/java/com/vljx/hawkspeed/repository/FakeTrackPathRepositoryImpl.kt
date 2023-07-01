package com.vljx.hawkspeed.repository

import com.vljx.hawkspeed.data.BaseRepository
import com.vljx.hawkspeed.data.mapper.track.TrackWithPathMapper
import com.vljx.hawkspeed.data.source.track.TrackPathLocalData
import com.vljx.hawkspeed.data.source.track.TrackPathRemoteData
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.ResourceImpl
import com.vljx.hawkspeed.domain.models.track.TrackWithPath
import com.vljx.hawkspeed.domain.repository.TrackPathRepository
import com.vljx.hawkspeed.domain.requestmodels.track.RequestDeleteTrackAndPath
import com.vljx.hawkspeed.domain.requestmodels.track.RequestGetTrackWithPath
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FakeTrackPathRepositoryImpl @Inject constructor(
    private val trackPathLocalData: TrackPathLocalData,
    private val trackPathRemoteData: TrackPathRemoteData,

    private val trackWithPathMapper: TrackWithPathMapper
): BaseRepository(), TrackPathRepository {
    override fun getTrackWithPath(requestGetTrackWithPath: RequestGetTrackWithPath): Flow<Resource<TrackWithPath>> =
        flowFromCache(
            trackWithPathMapper,
            databaseQuery = { trackPathLocalData.selectTrackWithPath(requestGetTrackWithPath) }
        ).map { trackWithPath ->
            if(trackWithPath == null) {
                throw NotImplementedError()
            }
            ResourceImpl.success(trackWithPath)
        }

    override fun getTracksWithPathsFromCache(): Flow<List<TrackWithPath>> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteTrackAndPath(requestDeleteTrackAndPath: RequestDeleteTrackAndPath) {
        TODO("Not yet implemented")
    }
}