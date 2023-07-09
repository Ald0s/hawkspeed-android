package com.vljx.hawkspeed.data.database

import androidx.room.Transaction
import com.vljx.hawkspeed.data.database.dao.TrackDraftDao
import com.vljx.hawkspeed.data.database.dao.TrackPointDraftDao
import com.vljx.hawkspeed.data.database.entity.track.TrackDraftEntity
import com.vljx.hawkspeed.data.database.entity.track.TrackDraftWithPointsEntity
import com.vljx.hawkspeed.data.database.entity.track.TrackPointDraftEntity
import com.vljx.hawkspeed.data.database.mapper.TrackDraftEntityMapper
import com.vljx.hawkspeed.data.database.mapper.TrackDraftWithPointsEntityMapper
import com.vljx.hawkspeed.data.database.mapper.TrackPointDraftEntityMapper
import com.vljx.hawkspeed.data.models.track.TrackDraftModel
import com.vljx.hawkspeed.data.models.track.TrackDraftWithPointsModel
import com.vljx.hawkspeed.data.models.track.TrackPointDraftModel
import com.vljx.hawkspeed.data.source.track.TrackDraftLocalData
import com.vljx.hawkspeed.domain.models.track.TrackPointDraft
import com.vljx.hawkspeed.domain.requestmodels.track.draft.RequestAddTrackPointDraft
import com.vljx.hawkspeed.domain.requestmodels.track.draft.RequestNewTrackDraft
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TrackDraftLocalDataImpl @Inject constructor(
    private val trackDraftDao: TrackDraftDao,
    private val trackPointDraftDao: TrackPointDraftDao,

    private val trackDraftWithPointsEntityMapper: TrackDraftWithPointsEntityMapper,
    private val trackDraftEntityMapper: TrackDraftEntityMapper,
    private val trackPointDraftEntityMapper: TrackPointDraftEntityMapper
): TrackDraftLocalData {
    override fun selectTrackDraftById(trackDraftId: Long): Flow<TrackDraftWithPointsModel?> =
        trackDraftDao.selectTrackDraftWithPoints(trackDraftId).map { value: TrackDraftWithPointsEntity? ->
            value?.let { trackDraftWithPointsEntityMapper.mapFromEntity(it) }
        }

    override fun newTrackDraftWithPoints(requestNewTrackDraft: RequestNewTrackDraft): Flow<TrackDraftWithPointsModel> = flow {
        // Open a new flow, and immediately emit all from the result of selecting the result of inserting a blank track draft into cache.
        // Filter not null since this should never be null.
        emitAll(
            selectTrackDraftById(
                insertTrackDraftWithPoints(
                    TrackDraftWithPointsModel(
                        TrackDraftModel(
                            trackDraftId = null,
                            trackType = requestNewTrackDraft.trackType,
                            name = null,
                            description = null
                        ),
                        listOf()
                    )
                )
            ).filterNotNull()
        )
    }

    override suspend fun addPointToTrack(requestAddTrackPointDraft: RequestAddTrackPointDraft): TrackDraftWithPointsModel {
        // Call ex function to create the point, insert it into cache and receive back the actual track point draft model; but we won't use that.
        addPointToTrackEx(requestAddTrackPointDraft)
        // Now, return the first result of collecting the selection of that track draft.
        return selectTrackDraftById(requestAddTrackPointDraft.trackDraftId)
            .filterNotNull()
            .first()
    }

    override suspend fun addPointToTrackEx(requestAddTrackPointDraft: RequestAddTrackPointDraft): TrackPointDraftModel {
        // TODO: First, check to ensure this track draft actually exists.
        // Next, create a track point draft entity for the new point.
        val trackPointDraft = TrackPointDraftEntity(
            null,
            requestAddTrackPointDraft.requestTrackPointDraft.latitude,
            requestAddTrackPointDraft.requestTrackPointDraft.longitude,
            requestAddTrackPointDraft.requestTrackPointDraft.loggedAt,
            requestAddTrackPointDraft.requestTrackPointDraft.speed,
            requestAddTrackPointDraft.requestTrackPointDraft.rotation,
            requestAddTrackPointDraft.trackDraftId
        )
        // Insert this point into cache, receiving back the track point draft's Id.
        val trackPointDraftId = trackPointDraftDao.insert(trackPointDraft)
        // Set the Id on this instance.
        trackPointDraft.trackPointDraftId = trackPointDraftId
        // Now return the created track point draft entity, but mapped to model.
        return trackPointDraftEntityMapper.mapFromEntity(
            trackPointDraft
        )
    }

    @Transaction
    override suspend fun insertTrackDraftWithPoints(trackDraftWithPoints: TrackDraftWithPointsModel): Long {
        // Build a track draft and a list of track point draft entities.
        val trackDraftEntity = trackDraftEntityMapper.mapToEntity(trackDraftWithPoints.trackDraft)
        // Now, insert the track draft entity, to retrieve its new Id.
        val trackDraftId: Long = trackDraftDao.insert(trackDraftEntity)
        // Set this as the track draft Id on all track point draft entities.
        val trackPointDraftEntities: List<TrackPointDraftEntity> = trackPointDraftEntityMapper.mapToEntityList(trackDraftWithPoints.trackPoints).map {
            it.trackDraftId = trackDraftId
            it
        }
        // Upsert all track point draft entities.
        trackPointDraftDao.insert(trackPointDraftEntities)
        // Return the track draft's Id.
        return trackDraftId
    }

    @Transaction
    override suspend fun upsertTrackDraftWithPoints(trackDraftWithPoints: TrackDraftWithPointsModel) {
        // Convert both track draft and points drafts into entities.
        val trackDraftEntity = trackDraftEntityMapper.mapToEntity(trackDraftWithPoints.trackDraft)
        val trackPointDraftEntities: List<TrackPointDraftEntity> = trackPointDraftEntityMapper.mapToEntityList(trackDraftWithPoints.trackPoints)
        // Upsert the track draft first.
        trackDraftDao.upsert(trackDraftEntity)
        // Upsert all points.
        trackPointDraftDao.upsert(trackPointDraftEntities)
    }

    @Transaction
    override suspend fun clearPointsForDraft(trackDraftId: Long): TrackDraftWithPointsModel {
        // Clear all points associated with the given track draft Id, then return a collection of the track draft with points.
        trackPointDraftDao.clearPointsFor(trackDraftId)
        return selectTrackDraftById(trackDraftId)
            .filterNotNull()
            .first()
    }

    @Transaction
    override suspend fun deleteTrackDraftWithPointsById(trackDraftId: Long) {
        // First, clear all points for the track.
        trackPointDraftDao.clearPointsFor(trackDraftId)
        // Then clear the track.
        trackDraftDao.deleteTrackDraft(trackDraftId)
    }
}