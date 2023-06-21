package com.vljx.hawkspeed.domain.usecase.socket

import com.vljx.hawkspeed.domain.repository.WorldSocketRepository
import com.vljx.hawkspeed.domain.states.socket.WorldSocketState
import com.vljx.hawkspeed.domain.usecase.BaseUseCase
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject

class GetWorldSocketStateUseCase @Inject constructor(
    private val worldSocketRepository: WorldSocketRepository
): BaseUseCase<Unit, SharedFlow<WorldSocketState>> {
    override fun invoke(params: Unit): SharedFlow<WorldSocketState> =
        worldSocketRepository.worldSocketState
}