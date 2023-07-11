package com.vljx.hawkspeed.data.database.mapper

import com.vljx.hawkspeed.data.database.entity.GameSettingsEntity
import com.vljx.hawkspeed.data.models.account.GameSettingsModel
import javax.inject.Inject

class GameSettingsEntityMapper @Inject constructor(

): EntityMapper<GameSettingsEntity, GameSettingsModel> {
    override fun mapFromEntity(entity: GameSettingsEntity): GameSettingsModel {
        return GameSettingsModel(
            entity.gameEntryToken,
            entity.gameServerInfo,
            entity.canConnectToGame
        )
    }

    override fun mapToEntity(model: GameSettingsModel): GameSettingsEntity {
        return GameSettingsEntity(
            null,
            model.gameEntryToken,
            model.gameServerInfo,
            model.canConnectToGame
        )
    }
}