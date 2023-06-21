package com.vljx.hawkspeed.domain.usecase.world

import com.vljx.hawkspeed.domain.di.Bridged
import com.vljx.hawkspeed.domain.models.world.WorldObjects
import com.vljx.hawkspeed.domain.repository.WorldRepository
import com.vljx.hawkspeed.domain.requestmodels.world.RequestGetWorldObjects
import com.vljx.hawkspeed.domain.usecase.BaseUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * A use case for retrieving all world objects currently applicable to the User. Essentially, all these world objects will be drawn to the
 * map at the end of the line.
 */
class GetWorldObjectsUseCase @Inject constructor(
    @Bridged
    private val worldRepository: WorldRepository
): BaseUseCase<RequestGetWorldObjects, Flow<WorldObjects>> {
    override fun invoke(params: RequestGetWorldObjects): Flow<WorldObjects> =
        worldRepository.getWorldObjects(params)
}