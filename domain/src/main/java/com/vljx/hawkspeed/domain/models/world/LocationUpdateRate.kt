package com.vljx.hawkspeed.domain.models.world

import com.google.android.gms.location.Priority

/**
 * The location update rate.
 */
sealed class LocationUpdateRate(
    val updateIntervalMillis: Long,
    val priority: Int,
    val minUpdateMeters: Float = 0f,
    val minUpdateIntervalMillis: Long = 0L
) {
    /**
     * The default, slowest location update rate. Location updates are expected at 10s intervals, unless provided faster. This location update
     * rate should be used when the player's device is currently still/not being moved.
     */
    object Default: LocationUpdateRate(
        updateIntervalMillis = 10000L,
        priority = Priority.PRIORITY_HIGH_ACCURACY
    )

    /**
     * A medium location update rate. Location updates are expected at 5s intervals, unless provided faster. This location update
     * rate should be used when the player's device is moving slow such as walking or running.
     */
    object SlowMoving: LocationUpdateRate(
        updateIntervalMillis = 5000L,
        priority = Priority.PRIORITY_HIGH_ACCURACY
    )

    /**
     * The fasted location update rate. Location updates are expected at 3s intervals, unless provided faster. This location update
     * rate should be used when the player's device is currently in/on any kind of vehicle.
     */
    object Fast: LocationUpdateRate(
        updateIntervalMillis = 3000L,
        priority = Priority.PRIORITY_HIGH_ACCURACY
    )

    /**
     * A special location update rate for when monitoring activity transitions is not available.
     */
    object Static: LocationUpdateRate(
        updateIntervalMillis = 4000L,
        priority = Priority.PRIORITY_HIGH_ACCURACY
    )
}