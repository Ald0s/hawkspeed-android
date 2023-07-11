package com.vljx.hawkspeed.domain.requestmodels.socket

data class RequestUpdateMagnetometerReadings(
    val latestReadings: FloatArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RequestUpdateMagnetometerReadings

        if (!latestReadings.contentEquals(other.latestReadings)) return false

        return true
    }

    override fun hashCode(): Int {
        return latestReadings.contentHashCode()
    }
}