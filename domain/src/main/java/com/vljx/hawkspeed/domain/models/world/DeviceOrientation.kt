package com.vljx.hawkspeed.domain.models.world

import kotlin.math.abs

data class DeviceOrientation(
    val orientationAngles: FloatArray
) {
    /**
     * Returns the azimuth angle from these orientation angles, as degrees.
     */
    val rotation: Float
        get() = abs(azimuth)

    /**
     * Returns the azimuth angle from these orientation angles.
     */
    val azimuth: Float
        get() = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DeviceOrientation

        if (!orientationAngles.contentEquals(other.orientationAngles)) return false

        return true
    }

    override fun hashCode(): Int {
        return orientationAngles.contentHashCode()
    }
}