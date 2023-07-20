package com.vljx.hawkspeed.data

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.vljx.hawkspeed.data.database.AppDatabase
import com.vljx.hawkspeed.data.database.entity.track.TrackCommentEntity
import com.vljx.hawkspeed.data.database.mapper.TrackCommentEntityMapper
import com.vljx.hawkspeed.data.mapper.track.TrackCommentMapper
import com.vljx.hawkspeed.data.mapper.track.TrackCommentsMapper
import com.vljx.hawkspeed.data.models.track.TrackCommentsPageModel
import com.vljx.hawkspeed.data.remotemediator.BaseRemoteMediator
import com.vljx.hawkspeed.data.source.track.TrackCommentLocalData
import com.vljx.hawkspeed.data.source.track.TrackCommentRemoteData
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.models.track.TrackComment
import com.vljx.hawkspeed.domain.models.track.TrackComments
import com.vljx.hawkspeed.domain.repository.TrackCommentRepository
import com.vljx.hawkspeed.domain.requestmodels.track.RequestPageTrackComments
import com.vljx.hawkspeed.domain.requestmodels.track.RequestTrackLatestComments
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TrackCommentRepositoryImpl @Inject constructor(
    private val appDatabase: AppDatabase,

    private val trackCommentRemoteData: TrackCommentRemoteData,
    private val trackCommentLocalData: TrackCommentLocalData,

    private val trackCommentsMapper: TrackCommentsMapper,

    private val trackCommentEntityMapper: TrackCommentEntityMapper,
    private val trackCommentMapper: TrackCommentMapper
): BaseRepository(), TrackCommentRepository {
    @ExperimentalPagingApi
    override fun pageCommentsForTrack(requestPageTrackComments: RequestPageTrackComments): Flow<PagingData<TrackComment>> =
        Pager(
            config = PagingConfig(pageSize = 20),
            remoteMediator = object: BaseRemoteMediator<TrackCommentEntity, TrackCommentsPageModel>(
                appDatabase,
                remoteQuery = { loadKey -> trackCommentRemoteData.queryCommentPage(requestPageTrackComments, loadKey) },
                upsertQuery = { dataModel: TrackCommentsPageModel ->
                    // Upsert the track and comments together, in same transaction.
                    // TODO: we should change leaderboard to do this as well.
                    trackCommentLocalData.upsertTrackCommentsPage(dataModel)
                },
                clearAllQuery = { trackCommentLocalData.clearCommentsFor(requestPageTrackComments.trackUid) }
            ) { },
            pagingSourceFactory = { trackCommentLocalData.pageCommentsForTrack(requestPageTrackComments) }
        ).flow.map { trackCommentEntityPagingData ->
            trackCommentEntityPagingData.map { trackCommentEntity ->
                trackCommentMapper.mapFromData(
                    trackCommentEntityMapper.mapFromEntity(
                        trackCommentEntity
                    )
                )
            }
        }

    override fun getLatestCommentsForTrack(requestTrackLatestComments: RequestTrackLatestComments): Flow<Resource<TrackComments>> =
        flowQueryNetworkAndCache(
            trackCommentsMapper,
            networkQuery = {
                trackCommentRemoteData.queryCommentPage(
                    RequestPageTrackComments(requestTrackLatestComments.trackUid),
                    1
                )
            },
            cacheResult = { trackCommentsPage ->
                trackCommentLocalData.upsertTrackCommentsPage(trackCommentsPage)
            }
        )
}