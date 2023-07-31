package com.vljx.hawkspeed.domain.usecase.socket

import com.vljx.hawkspeed.domain.repository.WorldSocketRepository
import com.vljx.hawkspeed.domain.models.world.ActivityTransitionUpdates
import com.vljx.hawkspeed.domain.usecase.BaseUseCase
import javax.inject.Inject

class SetActivityTransitionUpdatesUseCase @Inject constructor(
    private val worldSocketRepository: WorldSocketRepository
): BaseUseCase<ActivityTransitionUpdates, Unit> {
    override fun invoke(params: ActivityTransitionUpdates) =
        worldSocketRepository.setActivityTransitionUpdate(params)
}