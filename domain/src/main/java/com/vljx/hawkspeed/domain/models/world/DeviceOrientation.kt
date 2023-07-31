package com.vljx.hawkspeed.domain.models.world

import kotlin.math.abs

/**
 * A data class for storing the latest orientation for the device.
 * Rotation is stored as Degrees. Constructor expects orientation angles to be radians.
 */
data class DeviceOrientation(
    val rotation: Float
) {
    constructor(
        orientationAngles: FloatArray
    ): this(
        abs(
            Math.toDegrees(
                orientationAngles[0].toDouble()
            )
        ).toFloat()
    )
}