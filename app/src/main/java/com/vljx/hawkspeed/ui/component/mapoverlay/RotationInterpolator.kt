package com.vljx.hawkspeed.ui.component.mapoverlay

/**
 * A linear interpolator for adjusting a rotation, in degrees, from one value to another given a fraction.
 */
interface RotationInterpolator {
    fun interpolate(fraction: Float, rotationFrom: Float, rotationTo: Float): Float

    class Linear {
        companion object: RotationInterpolator {
            override fun interpolate(
                fraction: Float,
                rotationFrom: Float,
                rotationTo: Float
            ): Float =
                rotationFrom * (1 - fraction) + rotationTo * fraction
        }
    }
}