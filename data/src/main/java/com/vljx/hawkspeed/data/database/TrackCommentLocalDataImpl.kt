package com.vljx.hawkspeed.data.database

import androidx.paging.PagingSource
import androidx.room.Transaction
import com.vljx.hawkspeed.data.database.dao.TrackCommentDao
import com.vljx.hawkspeed.data.database.dao.TrackDao
import com.vljx.hawkspeed.data.database.entity.track.TrackCommentEntity
import com.vljx.hawkspeed.data.database.mapper.TrackCommentEntityMapper
import com.vljx.hawkspeed.data.database.mapper.TrackEntityMapper
import com.vljx.hawkspeed.data.models.track.TrackCommentsPageModel
import com.vljx.hawkspeed.data.source.track.TrackCommentLocalData
import com.vljx.hawkspeed.domain.requestmodels.track.RequestPageTrackComments
import javax.inject.Inject

class TrackCommentLocalDataImpl @Inject constructor(
    private val trackDao: TrackDao,
    private val trackCommentDao: TrackCommentDao,

    private val trackEntityMapper: TrackEntityMapper,
    private val trackCommentEntityMapper: TrackCommentEntityMapper
): TrackCommentLocalData {
    override fun pageCommentsForTrack(requestPageTrackComments: RequestPageTrackComments): PagingSource<Int, TrackCommentEntity> =
        trackCommentDao.pageCommentsFromTrack(requestPageTrackComments.trackUid)

    @Transaction
    override suspend fun upsertTrackCommentsPage(trackCommentsPage: TrackCommentsPageModel) {
        val trackEntity = trackEntityMapper.mapToEntity(trackCommentsPage.track)
        val trackCommentsEntities = trackCommentEntityMapper.mapToEntityList(trackCommentsPage.comments)
        // Upsert all comments.
        trackCommentDao.upsert(trackCommentsEntities)
        // Upsert the track.
        trackDao.upsert(trackEntity)
    }

    override suspend fun clearCommentsFor(trackUid: String) =
        trackCommentDao.clearCommentsFor(trackUid)
}