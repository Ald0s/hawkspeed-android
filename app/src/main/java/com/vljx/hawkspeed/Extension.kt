package com.vljx.hawkspeed

import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.SphericalUtil
import com.vljx.hawkspeed.domain.models.world.BoundingBox
import com.vljx.hawkspeed.domain.models.world.PlayerPosition
import com.vljx.hawkspeed.domain.models.world.PlayerPositionWithOrientation
import kotlin.math.roundToInt
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

object Extension {
    const val TRACK_OVERVIEW_PADDING = 100

    const val FOLLOW_PLAYER_ZOOM = 20f
    const val FOLLOW_PLAYER_TILT = 85f

    /**
     * An extension function that will generate a target repositioning of the map's camera such that the subject player position is being followed.
     */
    fun PlayerPositionWithOrientation.toFollowCameraUpdate(
        zoom: Float = FOLLOW_PLAYER_ZOOM,
        tilt: Float = FOLLOW_PLAYER_TILT
    ): CameraUpdate =
        CameraUpdateFactory.newCameraPosition(
            CameraPosition.builder()
                .target(LatLng(position.latitude, position.longitude))
                .tilt(tilt)
                .bearing(orientation.rotation)
                .zoom(zoom)
                .build()
        )

    /**
     * An extension function that will generate a target repositioning of the map's camera such that the subject bounding box is the focus of the viewport.
     */
    fun BoundingBox.toOverviewCameraUpdate(
        padding: Int = TRACK_OVERVIEW_PADDING
    ): CameraUpdate =
        CameraUpdateFactory.newLatLngBounds(
            LatLngBounds(
                LatLng(southWest.latitude, southWest.longitude),
                LatLng(northEast.latitude, northEast.longitude)
            ),
            padding
        )

    /**
     * An extension function that will generate a camera update based on the subject camera position, but with no tilt.
     */
    fun CameraPosition.noTilt(): CameraUpdate =
        CameraUpdateFactory.newCameraPosition(
            CameraPosition(
                this.target,
                this.zoom,
                0f,
                this.bearing
            )
        )

    /**
     * An extension function to a list of coordinates that will return the total length, prettified like; 2.4km / 183m.
     */
    fun List<LatLng>.prettyLength(): String =
        let { latLngs -> SphericalUtil.computeLength(latLngs).roundToInt() }
        .let { totalLength ->
            if((totalLength / 1000).toInt() > 0) {
                return "${String.format("%.2f", totalLength / 1000)}km"
            }
            return "${totalLength}m"
        }
}