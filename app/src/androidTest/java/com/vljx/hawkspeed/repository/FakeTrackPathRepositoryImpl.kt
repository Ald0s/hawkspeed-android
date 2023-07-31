package com.vljx.hawkspeed.repository

import com.vljx.hawkspeed.data.BaseRepository
import com.vljx.hawkspeed.data.mapper.track.TrackWithPathMapper
import com.vljx.hawkspeed.data.models.track.TrackModel
import com.vljx.hawkspeed.data.models.track.TrackPointModel
import com.vljx.hawkspeed.data.models.track.TrackWithPathModel
import com.vljx.hawkspeed.data.models.user.UserModel
import com.vljx.hawkspeed.data.source.track.TrackPathLocalData
import com.vljx.hawkspeed.data.source.track.TrackPathRemoteData
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.ResourceImpl
import com.vljx.hawkspeed.domain.enums.TrackType
import com.vljx.hawkspeed.domain.models.track.TrackWithPath
import com.vljx.hawkspeed.domain.repository.TrackPathRepository
import com.vljx.hawkspeed.domain.requestmodels.track.RequestDeleteTrackAndPath
import com.vljx.hawkspeed.domain.requestmodels.track.RequestGetTrackWithPath
import com.vljx.hawkspeed.domain.requestmodels.track.RequestSubmitTrack
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
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

    override fun submitNewTrack(requestSubmitTrack: RequestSubmitTrack): Flow<Resource<TrackWithPath>> =
        flowQueryNetworkAndCache(
        trackWithPathMapper,
            networkQuery = {
                // We won't provide the path, we'll report that the track is not yet verified.
                val trackUid = UUID.randomUUID().toString()
                val trackWithPath = TrackWithPathModel(
                    TrackModel(
                        trackUid,
                        requestSubmitTrack.name,
                        requestSubmitTrack.description,
                        UserModel("USER01", "aldos", "bio", 0, false, true),
                        listOf(),
                        TrackPointModel(0.0, 0.0, trackUid),
                        100f,
                        false,
                        1893,
                        false,
                        requestSubmitTrack.trackType,
                        null,
                        0,
                        0,
                        null,
                        0,
                        false,
                        false,
                        false,
                        false
                    ),
                    null
                )

                ResourceImpl.success(trackWithPath)
            },
            cacheResult = { trackWithPath ->
                // Upsert this track with path.
                trackPathLocalData.upsertTrackWithPath(trackWithPath)
            }
        )

    override fun getTracksWithPathsFromCache(): Flow<List<TrackWithPath>> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteTrackAndPath(requestDeleteTrackAndPath: RequestDeleteTrackAndPath) {
        TODO("Not yet implemented")
    }
}