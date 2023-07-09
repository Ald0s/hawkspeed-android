package com.vljx.hawkspeed.data

import com.vljx.hawkspeed.data.mapper.track.TrackDraftWithPointsMapper
import com.vljx.hawkspeed.data.mapper.track.TrackPointDraftMapper
import com.vljx.hawkspeed.data.source.track.TrackDraftLocalData
import com.vljx.hawkspeed.domain.models.track.TrackDraftWithPoints
import com.vljx.hawkspeed.domain.models.track.TrackPointDraft
import com.vljx.hawkspeed.domain.repository.TrackDraftRepository
import com.vljx.hawkspeed.domain.requestmodels.track.draft.RequestAddTrackPointDraft
import com.vljx.hawkspeed.domain.requestmodels.track.draft.RequestNewTrackDraft
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TrackDraftRepositoryImpl @Inject constructor(
    private val trackDraftLocalData: TrackDraftLocalData,

    private val trackPointDraftMapper: TrackPointDraftMapper,
    private val trackDraftWithPointsMapper: TrackDraftWithPointsMapper
): BaseRepository(), TrackDraftRepository {
    override fun getTrackDraftWithPointsById(trackDraftId: Long): Flow<TrackDraftWithPoints?> =
        flowFromCache(
            trackDraftWithPointsMapper,
            databaseQuery = { trackDraftLocalData.selectTrackDraftById(trackDraftId) }
        )

    override fun newTrackDraftWithPoints(requestNewTrackDraft: RequestNewTrackDraft): Flow<TrackDraftWithPoints?> =
        flowFromCache(
            trackDraftWithPointsMapper,
            databaseQuery = { trackDraftLocalData.newTrackDraftWithPoints(requestNewTrackDraft) }
        )

    override suspend fun saveTrackDraft(trackDraftWithPoints: TrackDraftWithPoints) =
        trackDraftLocalData.upsertTrackDraftWithPoints(
            trackDraftWithPointsMapper.mapToData(trackDraftWithPoints)
        )

    override suspend fun addPointToTrackDraft(requestAddTrackPointDraft: RequestAddTrackPointDraft): TrackDraftWithPoints =
        trackDraftWithPointsMapper.mapFromData(
            trackDraftLocalData.addPointToTrack(requestAddTrackPointDraft)
        )

    override suspend fun addPointToTrackDraftEx(requestAddTrackPointDraft: RequestAddTrackPointDraft): TrackPointDraft =
        trackPointDraftMapper.mapFromData(
            trackDraftLocalData.addPointToTrackEx(requestAddTrackPointDraft)
        )

    override suspend fun clearPointsForTrackDraft(trackDraftId: Long): TrackDraftWithPoints =
        trackDraftWithPointsMapper.mapFromData(
            trackDraftLocalData.clearPointsForDraft(trackDraftId)
        )

    override suspend fun deleteTrackDraft(trackDraftId: Long) =
        trackDraftLocalData.deleteTrackDraftWithPointsById(trackDraftId)
}