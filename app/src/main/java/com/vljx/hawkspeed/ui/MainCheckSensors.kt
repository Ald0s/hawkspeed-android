package com.vljx.hawkspeed.ui

interface MainCheckSensors {
    data class SensorReport(
        val type: Int,
        val gotSensor: Boolean
    )

    /**
     * Check to ensure the current device is equipped with adequate sensor capability to use HawkSpeed.
     */
    fun checkSensors(typesToCheck: List<Int>): Map<Int, SensorReport>
}