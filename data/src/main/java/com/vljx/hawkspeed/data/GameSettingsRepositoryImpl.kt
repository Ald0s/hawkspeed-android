package com.vljx.hawkspeed.data

import com.vljx.hawkspeed.data.mapper.account.GameSettingsMapper
import com.vljx.hawkspeed.data.source.account.GameSettingsLocalData
import com.vljx.hawkspeed.domain.repository.GameSettingsRepository
import javax.inject.Inject

class GameSettingsRepositoryImpl @Inject constructor(
    private val gameSettingsLocalData: GameSettingsLocalData,

    private val gameSettingsMapper: GameSettingsMapper
): BaseRepository(), GameSettingsRepository {
}