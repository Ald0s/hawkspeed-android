package com.vljx.hawkspeed.domain.repository

import com.vljx.hawkspeed.domain.models.world.WorldObjects
import com.vljx.hawkspeed.domain.requestmodels.world.RequestGetWorldObjects
import kotlinx.coroutines.flow.Flow

/**
 * A repository that represents the single source of truth for the world as an entire unit. This repository therefore refers to many other
 * repositories. This repository is entered into the authentication scope.
 */
interface WorldRepository {
    /**
     * A flow for all world objects that should be drawn to the map.
     */
    fun getWorldObjects(requestGetWorldObjects: RequestGetWorldObjects): Flow<WorldObjects>
}