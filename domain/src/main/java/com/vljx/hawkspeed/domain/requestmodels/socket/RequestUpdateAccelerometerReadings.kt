package com.vljx.hawkspeed.domain.requestmodels.socket

data class RequestUpdateAccelerometerReadings(
    val latestReadings: FloatArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RequestUpdateAccelerometerReadings

        if (!latestReadings.contentEquals(other.latestReadings)) return false

        return true
    }

    override fun hashCode(): Int {
        return latestReadings.contentHashCode()
    }
}