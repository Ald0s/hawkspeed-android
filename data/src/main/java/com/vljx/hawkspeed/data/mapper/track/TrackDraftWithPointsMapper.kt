package com.vljx.hawkspeed.data.mapper.track

import com.vljx.hawkspeed.data.mapper.Mapper
import com.vljx.hawkspeed.data.models.track.TrackDraftModel
import com.vljx.hawkspeed.data.models.track.TrackDraftWithPointsModel
import com.vljx.hawkspeed.domain.models.track.TrackDraftWithPoints
import javax.inject.Inject

class TrackDraftWithPointsMapper @Inject constructor(
    private val trackPointDraftMapper: TrackPointDraftMapper
): Mapper<TrackDraftWithPointsModel, TrackDraftWithPoints> {
    override fun mapFromData(model: TrackDraftWithPointsModel): TrackDraftWithPoints {
        return TrackDraftWithPoints(
            model.trackDraft.trackDraftId!!,
            model.trackDraft.trackType,
            model.trackDraft.name,
            model.trackDraft.description,
            trackPointDraftMapper.mapFromDataList(model.trackPoints)
        )
    }

    override fun mapToData(domain: TrackDraftWithPoints): TrackDraftWithPointsModel {
        return TrackDraftWithPointsModel(
            TrackDraftModel(
                domain.trackDraftId,
                domain.trackType,
                domain.trackName,
                domain.trackDescription
            ),
            trackPointDraftMapper.mapToDataList(domain.pointDrafts)
        )
    }
}