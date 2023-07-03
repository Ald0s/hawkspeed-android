package com.vljx.hawkspeed

import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.vljx.hawkspeed.domain.models.world.BoundingBox
import com.vljx.hawkspeed.domain.models.world.PlayerPosition

object Extension {
    const val TRACK_OVERVIEW_PADDING = 25

    const val FOLLOW_PLAYER_ZOOM = 20f
    const val FOLLOW_PLAYER_TILT = 45f

    /**
     * An extension function that will generate a target repositioning of the map's camera such that the subject player position is being followed.
     */
    fun PlayerPosition.toFollowCameraUpdate(
        zoom: Float = FOLLOW_PLAYER_ZOOM,
        tilt: Float = FOLLOW_PLAYER_TILT
    ): CameraUpdate =
        CameraUpdateFactory.newCameraPosition(
            CameraPosition.builder()
                .target(LatLng(latitude, longitude))
                .tilt(tilt)
                .bearing(rotation)
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
}