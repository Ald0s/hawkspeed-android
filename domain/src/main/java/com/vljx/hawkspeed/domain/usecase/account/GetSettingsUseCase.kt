package com.vljx.hawkspeed.domain.usecase.account

import com.vljx.hawkspeed.domain.di.Bridged
import com.vljx.hawkspeed.domain.models.world.GameSettings
import com.vljx.hawkspeed.domain.repository.AccountRepository
import com.vljx.hawkspeed.domain.usecase.BaseUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSettingsUseCase @Inject constructor(
    @Bridged
    private val accountRepository: AccountRepository
): BaseUseCase<Unit, Flow<GameSettings?>> {
    override fun invoke(params: Unit): Flow<GameSettings?> =
        accountRepository.getCurrentSettings()
}