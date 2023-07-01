package com.vljx.hawkspeed.data.database.mapper

import com.vljx.hawkspeed.data.database.entity.track.TrackDraftWithPointsEntity
import com.vljx.hawkspeed.data.models.track.TrackDraftWithPointsModel
import javax.inject.Inject

class TrackDraftWithPointsEntityMapper @Inject constructor(
    private val trackDraftEntityMapper: TrackDraftEntityMapper,
    private val trackPointDraftEntityMapper: TrackPointDraftEntityMapper
): EntityMapper<TrackDraftWithPointsEntity, TrackDraftWithPointsModel> {
    override fun mapFromEntity(entity: TrackDraftWithPointsEntity): TrackDraftWithPointsModel {
        return TrackDraftWithPointsModel(
            trackDraftEntityMapper.mapFromEntity(entity.trackDraft),
            trackPointDraftEntityMapper.mapFromEntityList(entity.draftPoints)
        )
    }

    override fun mapToEntity(model: TrackDraftWithPointsModel): TrackDraftWithPointsEntity {
        return TrackDraftWithPointsEntity(
            trackDraftEntityMapper.mapToEntity(model.trackDraft),
            trackPointDraftEntityMapper.mapToEntityList(model.trackPoints)
        )
    }
}