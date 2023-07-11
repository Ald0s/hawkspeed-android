package com.vljx.hawkspeed.data.mapper.account

import com.vljx.hawkspeed.data.mapper.Mapper
import com.vljx.hawkspeed.data.models.account.GameSettingsModel
import com.vljx.hawkspeed.domain.models.world.GameSettings
import javax.inject.Inject

class GameSettingsMapper @Inject constructor(

): Mapper<GameSettingsModel, GameSettings> {
    override fun mapFromData(model: GameSettingsModel): GameSettings {
        return GameSettings(
            model.canConnectToGame,
            model.gameEntryToken,
            model.gameServerInfo
        )
    }

    override fun mapToData(domain: GameSettings): GameSettingsModel {
        return GameSettingsModel(
            domain.entryToken,
            domain.gameServerInfo,
            domain.canConnectGame
        )
    }
}