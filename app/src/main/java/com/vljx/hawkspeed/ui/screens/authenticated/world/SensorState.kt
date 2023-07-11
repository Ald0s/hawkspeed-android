package com.vljx.hawkspeed.ui.screens.authenticated.world

import com.vljx.hawkspeed.ui.MainCheckSensors

sealed class SensorState {
    /**
     * A state that indicates all sensors are present and ready for use.
     */
    object AllSensorsPresent: SensorState()

    /**
     * A state that communicates the list of sensors not available, if any.
     */
    data class MissingSensors(
        val reports: List<MainCheckSensors.SensorReport>
    ): SensorState()
}