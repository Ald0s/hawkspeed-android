package com.vljx.hawkspeed.data.database.mapper

import com.vljx.hawkspeed.data.database.entity.track.TrackEntity
import com.vljx.hawkspeed.data.models.track.TrackModel
import javax.inject.Inject

class TrackEntityMapper @Inject constructor(
    private val userEntityMapper: UserEntityMapper,
    private val raceOutcomeEntityMapper: RaceOutcomeEntityMapper,
    private val trackPointEntityMapper: TrackPointEntityMapper
): EntityMapper<TrackEntity, TrackModel> {
    override fun mapFromEntity(entity: TrackEntity): TrackModel {
        return TrackModel(
            entity.trackUid,
            entity.name,
            entity.description,
            userEntityMapper.mapFromEntity(entity.owner),
            raceOutcomeEntityMapper.mapFromEntityList(entity.topLeaderboard),
            trackPointEntityMapper.mapFromEntity(entity.startPoint),
            entity.isVerified,
            entity.numPositiveVotes,
            entity.numNegativeVotes,
            entity.yourRating,
            entity.numComments,
            entity.canRace,
            entity.canEdit,
            entity.canDelete,
            entity.canComment
        )
    }

    override fun mapToEntity(model: TrackModel): TrackEntity {
        return TrackEntity(
            model.trackUid,
            model.name,
            model.description,
            userEntityMapper.mapToEntity(model.owner),
            raceOutcomeEntityMapper.mapToEntityList(model.topLeaderboard),
            trackPointEntityMapper.mapToEntity(model.startPoint),
            model.isVerified,
            model.numPositiveVotes,
            model.numNegativeVotes,
            model.yourRating,
            model.numComments,
            model.canRace,
            model.canEdit,
            model.canDelete,
            model.canComment
        )
    }
}