package com.vljx.hawkspeed.data.socket

sealed class WorldRequirementsState {
    object Satisfied: WorldRequirementsState()
    object NotSatisfied: WorldRequirementsState()
}